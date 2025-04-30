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
import com.example.testapp.features.homescreen.Home
import com.example.testapp.features.login.Login
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.presentation.country.CountryNav
import com.example.testapp.presentation.country.CountryScreen
import com.example.testapp.presentation.country.CountryViewModel
import com.example.testapp.ui.theme.TestAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Splash.SplashScreenRoute) {
                composable(Splash.SplashScreenRoute) { Splash.SplashScreen(navController) }
                composable(Login.LoginScreenRoute) { Login.LoginScreen(navController) }
                composable(Register.RegisterScreenRoute) { Register.RegisterScreen(navController) }
                composable(Home.homeScreenRoute) { Home.homeScreen(navController) }
                composable(CountryNav.CountryScreenRoute) {
                    val viewModel: CountryViewModel = viewModel()
                    CountryScreen(viewModel = viewModel, selectedInterests = listOf("Nature", "Culture"))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
    }
}
