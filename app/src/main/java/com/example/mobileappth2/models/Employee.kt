package com.example.mobileappth2.models

data class Employee(
    val id: Long = 0,
    val code: String,
    val fullname: String,
    val email: String,
    val phone: String,
    val hourlyRate: Double,
    val accountId: Long
)
