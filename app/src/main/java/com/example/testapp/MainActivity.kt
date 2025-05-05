package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.features.login.Login
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.presentation.country.CountryNav
import com.example.testapp.presentation.country.CountryScreen
import com.example.testapp.presentation.country.CountryViewModel
import com.example.testapp.repository.UserRepository
import com.example.testapp.screens.HomeScreen
import com.example.testapp.ui.theme.TestAppTheme
import com.example.testapp.viewmodels.HomeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Set the content view
        setContent {
            TestAppTheme {
                val navController = rememberNavController()

                // Set up navigation
                NavHost(navController = navController, startDestination = Splash.SplashScreenRoute) {
                    composable(Splash.SplashScreenRoute) {
                        Splash.SplashScreen(navController)
                    }
                    composable(Login.LoginScreenRoute) {
                        Login.LoginScreen(navController)
                    }
                    composable(Register.RegisterScreenRoute) {
                        Register.RegisterScreen(navController)
                    }
                    composable("home") {
                        // Get the current user's email
                        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

                        // Get Firestore instance and UserRepository
                        val firestore = Firebase.firestore
                        val repository = UserRepository(firestore)

                        // Instantiate HomeViewModel
                        val viewModel: HomeViewModel = viewModel()

                        // Pass the navController, email, and viewModel to HomeScreen
                        HomeScreen(
                            navController = navController,
                            currentUserEmail = email,
                            viewModel = viewModel
                        )
                    }
                    composable(CountryNav.CountryScreenRoute) {
                        val viewModel: CountryViewModel = viewModel()
                        CountryScreen(
                            viewModel = viewModel,
                            selectedInterests = listOf("Nature", "Culture")
                        )
                    }
                    // New route for displaying the country map
                    composable("countryMap/{countryCode}") { backStackEntry ->
                        val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "france"
                        MapScreen(countryCode = countryCode)
                    }
                }
            }
        }
    }
}

@Composable
fun MapScreen(countryCode: String) {
    // Affichage du code du pays sélectionné
    Text(text = "Carte du pays: $countryCode")

    // WebView pour afficher la carte
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            WebView(it).apply {
                webViewClient = WebViewClient()
                loadUrl("https://fr.mappy.com/plan/pays/$countryCode")  // Charge la carte du pays
            }
        }
    )
}
