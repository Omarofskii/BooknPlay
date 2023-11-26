package com.example.project

data class User(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val district: String = "",
    val country: String = "",
    val matches: Int = 0,
    val sport: String = "",
    val level: Int = 0,
    val bestHand: String = "",
    val profileImageUrl: String? = null
)
