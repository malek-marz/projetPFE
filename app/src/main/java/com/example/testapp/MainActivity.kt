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
import com.example.testapp.features.chat.ChatScreen
import com.example.testapp.features.chatPartnerProfile.ChatPartnerProfileScreen
import com.example.testapp.features.chs.Chs
import com.example.testapp.features.homescreen.Home
import com.example.testapp.features.login.Login
import com.example.testapp.features.login.Password
import com.example.testapp.features.profileUser.ProfileUserScreen
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.features.chs.ReportUserScreen
import com.example.testapp.features.USER.User
import com.example.testapp.ui.theme.TestAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
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

                // Chat screen with username and email params
                composable(
                    route = "chat_screen/{username}/{email}",
                    arguments = listOf(
                        navArgument("username") { type = NavType.StringType },
                        navArgument("email") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    ChatScreen(
                        initialEmail = email,
                        initialUsername = username,
                        onProfileClick = { clickedUsername ->
                            navController.navigate("chat_partner_profile/$clickedUsername")
                        }
                    )
                }

                // Chat partner profile screen with username param
                composable(
                    route = "chat_partner_profile/{username}",
                    arguments = listOf(navArgument("username") { type = NavType.StringType })
                ) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    ChatPartnerProfileScreen(partnerUsername = username)
                }

                // New: Report User screen with username param
                composable(
                    route = "report_user/{uid}",
                    arguments = listOf(navArgument("uid") { type = NavType.StringType })
                ) { backStackEntry ->
                    val uid = backStackEntry.arguments?.getString("uid") ?: ""
                    ReportUserScreen(reportedUserUid = uid)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
        // Empty preview for now
    }
}
