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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.testapp.features.login.Login
import com.example.testapp.features.login.Password
import com.example.testapp.features.profileUser.ProfileUserScreen
import com.example.testapp.features.register.Register
import com.example.testapp.features.splash.Splash
import com.example.testapp.features.chs.ReportUserScreen
import com.example.testapp.features.USER.User
import com.example.testapp.presentation.country.CountryNav
import com.example.testapp.presentation.country.CountryScreen
import com.example.testapp.presentation.country.CountryViewModel
import com.example.testapp.ui.theme.TestAppTheme
import com.example.testapp.viewmodels.Review
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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
                composable(HomeScreen.HomeScreenRoute) {
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                    val viewModel: Review.HomeViewModel = viewModel()

                    HomeScreen.HomeScreen(
                        navController = navController,
                        currentUserEmail = email,
                        viewModel = viewModel
                    )
                }
                composable(Buddy.buddyRoute) { Buddy.buddy(navController) }
                composable("Chs") { Chs.ChsScreen(navController) }
                composable("ProfileUserScreen") { ProfileUserScreen.profileUser(navController) }
                composable("User") { User.user(navController) }
                composable("Password") { Password.ForgotPasswordScreen(navController) }

                composable(ProfileScreen.ProfileScreenRoute) { ProfileScreen(navController) }
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
