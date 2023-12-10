package com.example.project

data class Court(
    val id: String = "",
    val name: String = "",
    val availableTimes: List<String> = emptyList(),
    val imageUrl: String = ""
)
