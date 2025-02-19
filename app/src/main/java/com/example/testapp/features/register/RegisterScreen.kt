package com.example.testapp.features.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


class Register {
    companion object {
        const val RegisterScreenRoute = "RegisterScreen"

        @Composable
        fun RegisterScreen(navController: NavController) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Magenta),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Text 1")
                Text("Text 2")
            }
        }
    }
}

@Preview(device = "id:Nexus S")
@Composable
private fun LoginPreviewBigPhone() {
    val navController = rememberNavController()
    Register.RegisterScreen(navController)
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun LoginPreviewSmallPhone() {
    val navController = rememberNavController()
    //Text("Hetha ecran small")
    Register.RegisterScreen(navController)
}