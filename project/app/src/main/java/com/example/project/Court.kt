package com.example.project

import com.google.type.DateTime
import com.google.firebase.Timestamp
import java.util.Date

data class Court(
    val id: String = "",
    val name: String = "",
    val availableTimes: List<String> = emptyList(),
    val imageUrl: String = ""
)

data class Match(
    var id: String = "",
    val courtId: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val organizerId: String = "",
    val playerList: List<String> = listOf(),
    val pricePerPlayer: Double = 0.0 // Make sure all fields have default values
)
