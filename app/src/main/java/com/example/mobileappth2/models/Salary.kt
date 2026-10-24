package com.example.mobileappth2.models

data class Salary(
    val id: Long = 0,
    val employeeId: Long,
    val month: Int,
    val year: Int,
    val totalHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val totalSalary: Double = 0.0,
    val status: String // "UNPAID", "PAID"
)
