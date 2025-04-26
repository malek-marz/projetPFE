package com.example.testapp.features.chs

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.testapp.features.chs.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChsViewModel  : ViewModel() {
    private val firebaseDatabase = Firebase.database
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        getChannels()
    }

    private fun getChannels() {
        firebaseDatabase.getReference("channel").get().addOnSuccessListener {
            val list = mutableListOf<Channel>()
            it.children.forEach { data ->
                val channel = Channel(data.key!!, data.value.toString())
                list.add(channel)
            }
            _channels.value = list
        }
    }

    fun addChannel(name: String) {
        val key = firebaseDatabase.getReference("channel").push().key
        firebaseDatabase.getReference("channel").child(key!!).setValue(name).addOnSuccessListener {
            getChannels()
        }
    }
}