package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.journeybuddy.ui.screens.ProfileScreen
import com.example.testapp.features.Buddys.Buddy
import com.example.testapp.features.chs.Chs
import com.example.testapp.features.homescreen.Home
import com.example.testapp.features.login.Login
import com.example.testapp.features.profileUser.ProfileUserScreen
import com.example.testapp.features.USER.User
import com.example.testapp.features.chat.ChatScreen
import com.example.testapp.features.login.Password
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.ui.theme.TestAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Set correct region-specific URL for your Realtime Database
        FirebaseDatabase.getInstance("https://journeybuddy-83c5e-default-rtdb.europe-west1.firebasedatabase.app")

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "SplashScreen") {
                composable("SplashScreen") { Splash.SplashScreen(navController) }
                composable(Login.LoginScreenRoute) { Login.LoginScreen(navController) }
                composable(Register.RegisterScreenRoute) { Register.RegisterScreen(navController) }
                composable(Home.homeScreenRoute) { Home.homeScreen(navController) }
                composable(Buddy.buddyRoute) { Buddy.buddy(navController) }
                composable("Chs") { Chs.ChsScreen(navController) }
                composable("ProfileUserScreen") { ProfileUserScreen.profileUser(navController) }
                composable("ProfileScreen") { ProfileScreen.InterestSelectionScreen(navController) }
                composable("User") { User.user(navController) }
                composable("Password") { Password.ForgotPasswordScreen(navController) }
                composable(
                    route = "chat/{email}",
                    arguments = listOf(navArgument("email") {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    ChatScreen(initialEmail = email)
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
        // Empty preview
    }
}