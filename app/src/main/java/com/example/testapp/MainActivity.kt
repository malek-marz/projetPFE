package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.features.login.Login
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.presentation.country.CountryViewModel
import com.example.testapp.presentation.country.CountryScreen
import com.example.testapp.ui.theme.TestAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            TestAppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Splash.SplashScreenRoute
                ) {
                    composable(Splash.SplashScreenRoute) {
                        Splash.SplashScreen(navController)
                    }
                    composable(Login.LoginScreenRoute) {
                        Login.LoginScreen(navController)
                    }
                    composable(Register.RegisterScreenRoute) {
                        Register.RegisterScreen(navController)
                    }
                    composable("country/{selectedInterests}") { backStackEntry ->
                        val selectedInterests = backStackEntry.arguments?.getString("selectedInterests")
                        val interestsList = selectedInterests?.split(",") ?: emptyList() // Convertir la chaîne en liste

                        val viewModel: CountryViewModel = viewModel()
                        CountryScreen(viewModel = viewModel, selectedInterests = interestsList)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
        val viewModel: CountryViewModel = viewModel()
        CountryScreen(viewModel = viewModel, selectedInterests = listOf("Adventure", "Nature"))
    }
}
