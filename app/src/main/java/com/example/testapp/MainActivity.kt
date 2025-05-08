package com.example.testapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.journeybuddy.ui.screens.ProfileScreen
import com.example.testapp.features.login.Login
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.presentation.country.CountryNav
import com.example.testapp.presentation.country.CountryScreen
import com.example.testapp.presentation.country.CountryViewModel
import com.example.testapp.repository.UserRepository
import com.example.testapp.screens.Home
import com.example.testapp.ui.theme.TestAppTheme
import com.example.testapp.viewmodels.HomeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            TestAppTheme {
                val navController = rememberNavController()

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
                    composable(Home.homeScreenRoute) {
                        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                        val firestore = Firebase.firestore
                        val repository = UserRepository(firestore)
                        val viewModel: HomeViewModel = viewModel()

                        Home.HomeScreen(
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
                    composable("countryMap/{countryCode}") { backStackEntry ->
                        val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "france"
                        MapScreen(countryCode = countryCode)
                    }

                    // ✅ Nouvelle route vers l'écran de profil
                    composable("profile") {
                        ProfileScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun MapScreen(countryCode: String) {
    Text(text = "Carte du pays: $countryCode")

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            WebView(it).apply {
                webViewClient = WebViewClient()
                loadUrl("https://fr.mappy.com/plan/pays/$countryCode")
            }
        }
    )
}
