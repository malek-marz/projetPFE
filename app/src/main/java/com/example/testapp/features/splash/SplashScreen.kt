package com.example.testapp.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.font.FontWeight
import com.example.testapp.R
import com.example.testapp.features.register.Register

class Splash {
    companion object {
        const val SplashScreenRoute = "SplashScreen"

        @Composable
        fun SplashScreen(navController: NavController) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1ECEC))
            ) {
                // Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 36.sp,
                        color = Color(0xFF2C3E50),
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )
                }

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo3),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                )

                // Buttons (Only Login, Register, and Test HomeScreen)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Login Button
                    Button(
                        onClick = { navController.navigate("LoginScreen") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                            .padding(bottom = 16.dp)
                            .graphicsLayer(
                                shadowElevation = 8f,
                                shape = RoundedCornerShape(16.dp),
                                clip = false
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007BFF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Connect", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Register Button
                    Button(
                        { navController.navigate(Register.RegisterScreenRoute) },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                            .graphicsLayer(
                                shadowElevation = 8f,
                                shape = RoundedCornerShape(16.dp),
                                clip = false
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF03326B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Create account", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    // Test HomeScreen Button
                    Button(
                        onClick = { navController.navigate("HomeScreen") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                            .graphicsLayer(
                                shadowElevation = 8f,
                                shape = RoundedCornerShape(16.dp),
                                clip = false
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28A745),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Test HomeScreen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(device = "id:Nexus S")
@Composable
private fun SplashPreviewPhone() {
    val navController = rememberNavController()
    Splash.SplashScreen(navController)
}