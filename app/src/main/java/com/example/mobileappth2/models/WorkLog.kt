package com.example.mobileappth2.models

data class WorkLog(
    val id: Long = 0,
    val employeeId: Long,
    val workDate: String, // YYYY-MM-DD
    val checkInTime: String, // HH:mm:ss
    val checkOutTime: String? = null, // HH:mm:ss
    val totalHours: Double = 0.0,
    val status: String // "CHECKED_IN", "COMPLETED"
)
