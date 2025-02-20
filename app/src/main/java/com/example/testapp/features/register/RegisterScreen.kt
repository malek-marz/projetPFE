package com.example.testapp.features.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx. compose. foundation. text. ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testapp.R
import com.example.testapp.R.drawable.logo

class Register {

    companion object {
        const val RegisterScreenRoute = "RegisterScreen"

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun RegisterScreen(navController: NavController) {
            var firstName by remember { mutableStateOf("") }
            var lastName by remember { mutableStateOf("") }
            var username by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            var birthday by remember { mutableStateOf("") }

            val genders = listOf("Male", "Female", "Other")
            var selectedGender by remember { mutableStateOf(genders[0]) }
            var isGenderDropdownExpanded by remember { mutableStateOf(false) }

            val countries = listOf("Tunisia", "France", "Canada", "USA", "Germany", "Other")
            var selectedCountry by remember { mutableStateOf(countries[0]) }
            var isCountryDropdownExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo above the Sign Up title
                    Image(
                        painter = painterResource(id = logo), // Replace with your actual logo file
                        contentDescription = "Logo",
                        modifier = Modifier.size(120.dp) // Adjust the size of the logo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title for Sign Up
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // First Name Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "First Name") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("First Name") }
                        )
                    }

                    // Last Name Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Last Name") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Last Name") }
                        )
                    }

                    // Username Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Username") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Username") }
                        )
                    }

                    // Email Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription = "Email") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email") }
                        )
                    }

                    // Password Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = "Password") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") }
                        )
                    }

                    // Confirm Password Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = "Confirm Password") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Confirm Password") }
                        )
                    }

                    // Gender Dropdown with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isGenderDropdownExpanded,
                            onExpandedChange = { isGenderDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedGender,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Gender") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isGenderDropdownExpanded = true },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isGenderDropdownExpanded
                                    )
                                },
                                label = { Text("Gender") }
                            )
                            ExposedDropdownMenu(
                                expanded = isGenderDropdownExpanded,
                                onDismissRequest = { isGenderDropdownExpanded = false }
                            ) {
                                genders.forEach { gender ->
                                    DropdownMenuItem(
                                        text = { Text(gender) },
                                        onClick = {
                                            selectedGender = gender
                                            isGenderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Country Dropdown with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isCountryDropdownExpanded,
                            onExpandedChange = { isCountryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCountry,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Country of Origin") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCountryDropdownExpanded = true },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isCountryDropdownExpanded
                                    )
                                },
                                label = { Text("Country of Origin") }
                            )
                            ExposedDropdownMenu(
                                expanded = isCountryDropdownExpanded,
                                onDismissRequest = { isCountryDropdownExpanded = false }
                            ) {
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text(country) },
                                        onClick = {
                                            selectedCountry = country
                                            isCountryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Birthday Field with Label and Icon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = birthday,
                            onValueChange = { birthday = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Date of Birth") },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Date of Birth") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Up Button with Blue color
                    Button(
                        onClick = {
                            // Add sign-up logic here
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue) // Blue color
                    ) {
                        Text("Sign Up", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Already have an account text

                    ClickableText(
                        text = AnnotatedString("Already have an account? Sign In"),
                        onClick = { navController.navigate("loginScreen") },
                        style = TextStyle(color = Color.Blue, fontSize = 14.sp)

                    )
                }
            }
        }
    }
}


@Preview(device = "id:pixel_9_pro")
@Composable
private fun RegisterPreviewSmallPhone() {
    val navController = rememberNavController()
    Register.RegisterScreen(navController)
}
