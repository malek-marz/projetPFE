package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.features.Buddys.Buddy
import com.example.testapp.features.homescreen.Home
import com.example.testapp.features.login.Login
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.ui.theme.TestAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Splash.SplashScreenRoute) {
                composable(Splash.SplashScreenRoute) { Splash.SplashScreen(navController) }
                composable(Login.LoginScreenRoute) { Login.LoginScreen(navController) }
                composable(Register.RegisterScreenRoute) { Register.RegisterScreen(navController) }
                composable(Home.homeScreenRoute) { Home.homeScreen(navController) }
                composable(Buddy.buddyRoute) { Buddy.buddy(navController) }
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