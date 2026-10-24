package com.example.mobileappth2.dao

import android.content.ContentValues
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.models.Employee
import com.example.mobileappth2.models.Salary
import kotlin.math.roundToInt

class SalaryDAO(private val dbHelper: DBHelper) {

    fun calculateAndSaveSalary(employeeId: Long, month: Int, year: Int): Salary? {
        val emp = dbHelper.employeeDAO.getEmployeeById(employeeId) ?: return null
        val db = dbHelper.writableDatabase

        val monthStr = if (month < 10) "0$month" else "$month"
        val datePrefix = "$year-$monthStr-"

        val cursor = db.query(
            DBHelper.TABLE_WORK_LOG,
            null,
            "${DBHelper.COL_WL_EMP_ID}=? AND ${DBHelper.COL_WL_STATUS}='COMPLETED' AND ${DBHelper.COL_WL_WORK_DATE} LIKE ?",
            arrayOf(employeeId.toString(), "$datePrefix%"),
            null, null, null
        )

        var totalMonthHours = 0.0
        var totalOvertimeHours = 0.0

        if (cursor.moveToFirst()) {
            do {
                val dailyHours = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_TOTAL_HOURS))
                totalMonthHours += dailyHours
                if (dailyHours > 8.0) {
                    totalOvertimeHours += (dailyHours - 8.0)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        totalMonthHours = (totalMonthHours * 100).roundToInt() / 100.0
        totalOvertimeHours = (totalOvertimeHours * 100).roundToInt() / 100.0
        val regularHours = totalMonthHours - totalOvertimeHours

        val calculatedSalary = (regularHours * emp.hourlyRate) + (totalOvertimeHours * emp.hourlyRate * 1.5)
        val roundedTotalSalary = (calculatedSalary * 100).roundToInt() / 100.0

        val existingCursor = db.query(
            DBHelper.TABLE_SALARY,
            null,
            "${DBHelper.COL_SAL_EMP_ID}=? AND ${DBHelper.COL_SAL_MONTH}=? AND ${DBHelper.COL_SAL_YEAR}=?",
            arrayOf(employeeId.toString(), month.toString(), year.toString()),
            null, null, null
        )

        val values = ContentValues().apply {
            put(DBHelper.COL_SAL_EMP_ID, employeeId)
            put(DBHelper.COL_SAL_MONTH, month)
            put(DBHelper.COL_SAL_YEAR, year)
            put(DBHelper.COL_SAL_TOTAL_HOURS, totalMonthHours)
            put(DBHelper.COL_SAL_OVERTIME_HOURS, totalOvertimeHours)
            put(DBHelper.COL_SAL_TOTAL_SALARY, roundedTotalSalary)
        }

        var status = "UNPAID"
        if (existingCursor.moveToFirst()) {
            status = existingCursor.getString(existingCursor.getColumnIndexOrThrow(DBHelper.COL_SAL_STATUS))
            db.update(
                DBHelper.TABLE_SALARY,
                values,
                "${DBHelper.COL_SAL_EMP_ID}=? AND ${DBHelper.COL_SAL_MONTH}=? AND ${DBHelper.COL_SAL_YEAR}=?",
                arrayOf(employeeId.toString(), month.toString(), year.toString())
            )
        } else {
            values.put(DBHelper.COL_SAL_STATUS, "UNPAID")
            db.insert(DBHelper.TABLE_SALARY, null, values)
        }
        existingCursor.close()

        return Salary(
            employeeId = employeeId,
            month = month,
            year = year,
            totalHours = totalMonthHours,
            overtimeHours = totalOvertimeHours,
            totalSalary = roundedTotalSalary,
            status = status
        )
    }

    fun getSalariesByEmployee(employeeId: Long): List<Salary> {
        val list = mutableListOf<Salary>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_SALARY,
            null,
            "${DBHelper.COL_SAL_EMP_ID}=?",
            arrayOf(employeeId.toString()),
            null, null,
            "${DBHelper.COL_SAL_YEAR} DESC, ${DBHelper.COL_SAL_MONTH} DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Salary(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_ID)),
                        employeeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_EMP_ID)),
                        month = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_MONTH)),
                        year = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_YEAR)),
                        totalHours = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_TOTAL_HOURS)),
                        overtimeHours = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_OVERTIME_HOURS)),
                        totalSalary = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_TOTAL_SALARY)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SAL_STATUS))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAllSalariesWithEmployee(month: Int, year: Int): List<Pair<Salary, Employee>> {
        val employees = dbHelper.employeeDAO.getAllEmployees()
        val list = mutableListOf<Pair<Salary, Employee>>()

        for (emp in employees) {
            val sal = calculateAndSaveSalary(emp.id, month, year)
            if (sal != null) {
                list.add(Pair(sal, emp))
            }
        }
        return list
    }

    fun updateSalaryStatus(employeeId: Long, month: Int, year: Int, newStatus: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_SAL_STATUS, newStatus)
        }
        val updated = db.update(
            DBHelper.TABLE_SALARY,
            values,
            "${DBHelper.COL_SAL_EMP_ID}=? AND ${DBHelper.COL_SAL_MONTH}=? AND ${DBHelper.COL_SAL_YEAR}=?",
            arrayOf(employeeId.toString(), month.toString(), year.toString())
        )
        return updated > 0
    }
}
