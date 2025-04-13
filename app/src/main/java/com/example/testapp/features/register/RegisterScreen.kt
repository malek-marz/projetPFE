
package com.example.testapp.features.register

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.testapp.R.drawable.logo
import com.example.yourapp.model.RegisterViewModel

class Register {

    companion object {
        const val RegisterScreenRoute = "RegisterScreen"

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
            val state by viewModel.state.collectAsState()


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "S'inscrire",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // First Name Field
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = { viewModel.onFirstNameChanged(it) },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Prénom") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Prénom") }
                    )

                    // Last Name Field
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = { viewModel.onLastNameChanged(it) },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Nom") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom") }
                    )

                    // Username Field
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { state.username = it },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Nom d'utilisateur") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom d'utilisateur") }
                    )

                    // Email Field
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { state.email = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription = "Adresse e-mail") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Adresse e-mail") }
                    )

                    // Password Field
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { state.password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = "Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mot de passe") }
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChanged(it)},
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = "Confirmer le mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirmer le mot de passe") }
                    )

                    // Gender Dropdown
                    GenderDropdown(state.gender) { state.gender = it }

                    // Country Dropdown
                    CountryDropdown(state.country) { state.country = it }

                    // Birthday Field
                    OutlinedTextField(
                        value = state.birthday,
                        onValueChange = { viewModel.onBirthdayChanged(it)},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Date de naissance") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Date de naissance ") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Up Button
                    Button(
                        onClick = {
                            viewModel.register(
                                onRegisterSuccess = TODO(),
                                onRegisterFailed = TODO()
                            )
                            Log.i("FireBaseRegister", "Inscription réussie")
                            navController.navigate("LoginScreen")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                    ) {
                        Text("S'inscrire", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Already have an account?
                    Text(
                        text = "Vous avez déjà un compte ? Se connecter",
                        color = Color.Blue,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            navController.navigate("loginScreen")
                        }
                    )
                }
            }
        }
                }
            }


        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun GenderDropdown(selectedGender: String, onGenderSelected: (String) -> Unit) {
            var isExpanded by remember { mutableStateOf(false) }
            val genders = listOf("Homme", "Femme", "Autre")

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Genre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    label = { Text("Gender") }
                )
                ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                    genders.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                onGenderSelected(gender)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun CountryDropdown(selectedCountry: String, onCountrySelected: (String) -> Unit) {
            var isExpanded by remember { mutableStateOf(false) }
            val countries = listOf("Tunisie", "France", "Canada", "USA", "Allemagne", "Autre")

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCountry,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Pays") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    label = { Text("Pays") }
                )
                ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country) },
                            onClick = {
                                onCountrySelected(country)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }

