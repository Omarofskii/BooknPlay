package com.example.project

data class Court(
    val name: String = "",
    val availableTimes: List<String> = emptyList(),
    val imageUrl: String = ""
)
