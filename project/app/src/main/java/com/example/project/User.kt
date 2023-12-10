package com.example.project

data class User(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val district: String = "",
    val country: String = "",
    val bestHand: String = "",
    val favoritePlayTime: String = "",
    val favoriteCourtPosition: String = "",
    val profileImageUrl: String? = null
)
