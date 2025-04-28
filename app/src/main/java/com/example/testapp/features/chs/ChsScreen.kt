package com.example.testapp.features.chs

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
class Chs{
    companion object{
        const val ChsRoute="ChsScreen"

        @Composable
        fun ChsScreen(navController: NavController,viewModel:ChsViewModel  = viewModel()) {
        val usernames = viewModel.usernames
    // UI
        LazyColumn {
            items(usernames) { username ->
                Text(
                    text = username,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
            )
        }
    }
}
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewChsScreen() {
    val navController = rememberNavController()
    Chs.ChsScreen(navController = navController)
}
  //  val channels by viewModel.channels.collectAsState()

    //Scaffold { paddingValues ->
      //  Box(
        //    modifier = Modifier
          //      .padding(paddingValues)
            //    .fillMaxSize()
        //) {
            // LazyColumn to display the list of channels
         //   LazyColumn {
           //     items(channels) { channel ->
                    // Display each channel's username
             //       Column(modifier = Modifier.padding(8.dp)) {
                //        Text(text = channel.id)
                 //   }
                //}
            //}
        //}
  //  }
//}