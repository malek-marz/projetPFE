package com.example.testapp.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx. compose. foundation. text. ClickableText
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.testapp.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

class Login {
    companion object {
        const val LoginScreenRoute = "LoginScreen"
        @Composable
        fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
            val state by viewModel.state.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1ECEC)), // Light gray background
                contentAlignment = Alignment.TopCenter // Aligns content to the top
            ) {
                // Column to stack the image and form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Image at the top
                    Image(
                        painter = painterResource(id = R.drawable.logo3), // Ensure the image name is correct (e.g., "logo")
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(130.dp)
                            .padding(bottom = 25.dp) // Add space below the image
                    )

                    // Form content starts here
                    Text(text = "Login", fontSize = 24.sp, color = Color.Black)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MailOutline,
                                contentDescription = "Email"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.errorMessage, color = Color.Red, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    val darkBlue = Color(0xFF00008B)

                    Button(
                        onClick = 
                       { viewModel.login { navController.navigate("HomeScreen") }},
                            modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("connect", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ClickableText(
                        text = AnnotatedString("forgot password ?"),
                        onClick = {
                            navController.navigate("ForgotPasswordScreen")
                        },
                        style = TextStyle(color = Color.Blue, fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ClickableText(
                        text = AnnotatedString("Creat account ?"),
                        onClick = { navController.navigate("RegisterScreen") },
                        style = TextStyle(color = Color.Blue, fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

@Preview(device = "id:Nexus S")
@Composable
private fun LoginPreviewPhone() {
    val navController = rememberNavController()
    Login.LoginScreen(navController)
}



