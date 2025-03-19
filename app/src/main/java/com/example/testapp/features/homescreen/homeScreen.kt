package com.example.testapp.features.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testapp.R
private val LightestBlue = Color(0xFFF5F9FF)
private val LightBlue = Color(0xFFE8F0FE)
private val MediumBlue = Color(0xFF4D8FCC)
private val DarkBlue = Color(0xFF255A9D)
private val White = Color.White
class Home {
    companion object {
        const val homeScreenRoute = "homeScreen"

        @Composable
        fun homeScreen(navController: NavController, viewModel: homeScreenViewmodel = viewModel()) {
            val state by viewModel.state.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LightestBlue, LightBlue)
                        )
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Welcome!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )

                    Text(
                        text = "How can we help you?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MediumBlue,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(200.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ButtonSquare("AskBuddy", R.drawable.amies) {
                                navController.navigate("askBuddyRoute")
                            }
                            ButtonSquare("FindBuddy", R.drawable.meow) {
                                navController.navigate("findBuddyRoute")
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ButtonSquare("Friends", R.drawable.amies) {
                                navController.navigate("friendsRoute")
                            }
                            ButtonSquare("Map", R.drawable.img2) {
                                navController.navigate("mapRoute")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HomeButton(Modifier.weight(1f), navController)
                    UserButton(Modifier.weight(1f), navController)
                    MessageButton(Modifier.weight(1f), navController)
                }
            }
        }
    }
}
@Composable
fun ButtonSquare(text: String, iconRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MediumBlue),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(40.dp),
                tint = White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = text, color = White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HomeButton(modifier: Modifier, navController: NavController) {
    BottomNavButton(modifier, R.drawable.home, "Home", DarkBlue) {
        navController.navigate("homeRoute")
    }
}

@Composable
fun UserButton(modifier: Modifier, navController: NavController) {
    BottomNavButton(modifier, R.drawable.user, "User", MediumBlue) {
        navController.navigate("userRoute")
    }
}

@Composable
fun MessageButton(modifier: Modifier, navController: NavController) {
    BottomNavButton(modifier, R.drawable.message, "Message", DarkBlue) {
        navController.navigate("messageRoute")
    }
}

@Composable
fun BottomNavButton(modifier: Modifier, iconRes: Int, contentDesc: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.size(70.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDesc,
            modifier = Modifier.size(38.dp),
            tint = White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun homeScreenPreview() {
    val navController = rememberNavController()
    Home.homeScreen(navController)
}