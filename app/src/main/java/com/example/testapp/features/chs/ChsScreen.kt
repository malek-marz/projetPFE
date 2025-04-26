package com.example.testapp.features.chs

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

class ChsScreen {
    companion object {
        const val ChsRoute = "ChsScreen"

        @Composable
        fun ChsScreen(navController: NavController, viewModel: ChsViewModel = viewModel()) {
            val channels by viewModel.channels.collectAsState()

            Scaffold { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    LazyColumn {
                        items(channels) { channel ->
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = channel.username)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChsScreen() {
    val navController = rememberNavController()
    ChsScreen.ChsScreen(navController)
}

