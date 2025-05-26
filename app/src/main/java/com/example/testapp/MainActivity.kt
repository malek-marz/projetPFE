package com.example.testapp

import HomeScreen
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.journeybuddy.ui.screens.ProfileScreen
import com.example.testapp.features.USER.user
import com.example.testapp.features.login.Login
import com.example.testapp.features.login.Password
import com.example.testapp.features.profileUser.ProfileUserScreen
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.presentation.country.CountryNav
import com.example.testapp.presentation.country.CountryScreen
import com.example.testapp.presentation.country.CountryViewModel
import com.example.testapp.repository.UserRepository
import com.example.testapp.ui.theme.TestAppTheme
import com.example.testapp.viewmodels.Review
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
                    composable(ProfileUserScreen.profileUserScreenRoute) {
                        ProfileUserScreen.profileUser(navController)
                    }


                    composable(HomeScreen.HomeScreenRoute) {
                        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                        val firestore = Firebase.firestore
                        val viewModel: Review.HomeViewModel = viewModel()

                        HomeScreen.HomeScreen(
                            navController = navController,
                            currentUserEmail = email,
                            viewModel = viewModel
                        )
                    }
                    composable(ProfileScreen.ProfileScreenRoute) {
                        ProfileScreen(navController)
                    }



                    composable(
                        route = CountryNav.CountryScreenRoute + "/{interests}",
                        arguments = listOf(navArgument("interests") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val interestsString = backStackEntry.arguments?.getString("interests") ?: ""
                        val interests = if (interestsString.isNotEmpty()) interestsString.split(",") else emptyList()
                        val viewModel: CountryViewModel = viewModel()

                        CountryScreen(viewModel = viewModel, Interests = interests)
                    }


                    composable("countryMap/{countryCode}") { backStackEntry ->
                        val countryCode =
                            backStackEntry.arguments?.getString("countryCode") ?: "france"
                        MapScreen(countryCode = countryCode)
                    }
                    composable(user.ROUTE) {
                        user.user(navController)
                    }
                    composable("Password"){ Password.ForgotPasswordScreen(navController)}






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
}