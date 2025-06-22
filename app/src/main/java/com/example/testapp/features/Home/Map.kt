import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.*

@Composable
fun SelectCountryMapScreen(
    navController: NavController,
    onCountrySelected: (String) -> Unit
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Initialise OSMDroid configuration (important)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )
    }

    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(2.0)
                        controller.setCenter(GeoPoint(20.0, 0.0))

                        val mapView = this

                        val tapOverlay = object : Overlay(ctx) {
                            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                                val proj = mapView.projection
                                val geoPoint = proj.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint

                                selectedGeoPoint = geoPoint

                                val country = getCountryFromLatLng(ctx, geoPoint.latitude, geoPoint.longitude)
                                selectedCountry = country
                                Log.d("MapScreen", "Pays sélectionné : $country")

                                // Supprime uniquement les markers existants
                                val markersToRemove = mapView.overlays.filterIsInstance<Marker>()
                                markersToRemove.forEach { mapView.overlays.remove(it) }

                                // Ajoute un nouveau marker
                                val marker = Marker(mapView)
                                marker.position = geoPoint
                                marker.title = country ?: "Pays inconnu"
                                mapView.overlays.add(marker)
                                mapView.invalidate()

                                return true
                            }
                        }

                        this.overlays.add(tapOverlay)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedCountry?.let { country ->
                    isSaving = true
                    saveCountryToFirestore(userId, country) { success ->
                        isSaving = false
                        if (success) {
                            onCountrySelected(country)
                            navController.navigate("HomeScreen") {
                                popUpTo("selectCountry") { inclusive = true }
                            }
                        } else {
                            Log.e("Firestore", "Erreur lors de l'enregistrement du pays")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = selectedCountry != null && !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3), // Bleu clair
                contentColor = Color.White
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Confirmer le pays sélectionné")
            }
        }

    }
}

fun getCountryFromLatLng(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].countryName ?: "Pays inconnu"
        } else {
            "Pays inconnu"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Pays inconnu"
    }
}

fun saveCountryToFirestore(userId: String, country: String, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("users")
        .document(userId)
        .collection("savedCountries")
        .document("selected_country")

    val newEntry = hashMapOf(
        "name" to country,
        "timestamp" to Timestamp.now()
    )

    docRef.update("countries", com.google.firebase.firestore.FieldValue.arrayUnion(newEntry))
        .addOnSuccessListener {
            Log.d("Firestore", "Pays ajouté au tableau avec succès")
            onComplete(true)
        }
        .addOnFailureListener { e ->
            docRef.set(hashMapOf("countries" to listOf(newEntry)))
                .addOnSuccessListener {
                    Log.d("Firestore", "Document créé avec le premier pays")
                    onComplete(true)
                }
                .addOnFailureListener { ex ->
                    Log.w("Firestore", "Erreur lors de la création du document", ex)
                    onComplete(false)
                }
        }
}
