package com.example.mobileappth2.models

data class Account(
    val id: Long = 0,
    val username: String,
    val password: String,
    val role: String // "ADMIN" or "EMPLOYEE"
)
