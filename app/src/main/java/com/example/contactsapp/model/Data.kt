package com.example.contactsapp.model

data class Data(
    val date: String,
    val totalUsers: Int,
    val users: List<User>
)