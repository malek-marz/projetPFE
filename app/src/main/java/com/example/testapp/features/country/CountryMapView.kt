package com.example.testapp.presentation.country

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testapp.features.country.CountryData

@Composable
fun CountryMapView(countryData: CountryData?) {
    val mapUrl = countryData?.mapUrl ?: "https://www.google.com/maps" // URL par défaut si vide

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true  // Activer JavaScript
                    domStorageEnabled = true  // Activer le stockage local
                    // Pas besoin de setAppCacheEnabled ou setAppCachePath ici
                }

                webViewClient = WebViewClient()
                loadUrl(mapUrl) // Charge l'URL de la carte
            }
        }
    )
}

@Preview
@Composable
fun PreviewCountryMapView() {
    val countryData = CountryData(
        name = "Tunisie",
        capital = "Tunis",
        language = "Arabe",
        currency = "Dinar Tunisien",
        population = "11 million",
        timezone = "CET",
        flagUrl = "https://www.countryflags.com/tn.png",
        mapUrl = "https://fr.mappy.com/plan/pays/tn", // Exemple d'URL
        description = "Un pays magnifique en Afrique du Nord.",
        landmarkUrl = "https://example.com/landmark"
    )
    CountryMapView(countryData)
}
