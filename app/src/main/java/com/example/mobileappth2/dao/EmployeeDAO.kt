package com.example.mobileappth2.dao

import android.content.ContentValues
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.models.Employee

class EmployeeDAO(private val dbHelper: DBHelper) {

    fun getAllEmployees(): List<Employee> {
        val list = mutableListOf<Employee>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DBHelper.TABLE_EMPLOYEE, null, null, null, null, null, "${DBHelper.COL_EMP_CODE} ASC")
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Employee(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ID)),
                        code = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_CODE)),
                        fullname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_FULLNAME)),
                        email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_EMAIL)),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_PHONE)),
                        hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_HOURLY_RATE)),
                        accountId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ACC_ID))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun searchEmployees(query: String): List<Employee> {
        val list = mutableListOf<Employee>()
        val db = dbHelper.readableDatabase
        val selection = "${DBHelper.COL_EMP_CODE} LIKE ? OR ${DBHelper.COL_EMP_FULLNAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        val cursor = db.query(DBHelper.TABLE_EMPLOYEE, null, selection, selectionArgs, null, null, "${DBHelper.COL_EMP_CODE} ASC")
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Employee(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ID)),
                        code = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_CODE)),
                        fullname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_FULLNAME)),
                        email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_EMAIL)),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_PHONE)),
                        hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_HOURLY_RATE)),
                        accountId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ACC_ID))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getEmployeeById(employeeId: Long): Employee? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_EMPLOYEE,
            null,
            "${DBHelper.COL_EMP_ID}=?",
            arrayOf(employeeId.toString()),
            null, null, null
        )
        var emp: Employee? = null
        if (cursor.moveToFirst()) {
            emp = Employee(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ID)),
                code = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_CODE)),
                fullname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_FULLNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_PHONE)),
                hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_HOURLY_RATE)),
                accountId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ACC_ID))
            )
        }
        cursor.close()
        return emp
    }

    fun getEmployeeByAccountId(accountId: Long): Employee? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_EMPLOYEE,
            null,
            "${DBHelper.COL_EMP_ACC_ID}=?",
            arrayOf(accountId.toString()),
            null, null, null
        )
        var emp: Employee? = null
        if (cursor.moveToFirst()) {
            emp = Employee(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ID)),
                code = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_CODE)),
                fullname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_FULLNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_PHONE)),
                hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_HOURLY_RATE)),
                accountId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_EMP_ACC_ID))
            )
        }
        cursor.close()
        return emp
    }

    fun addEmployee(
        code: String,
        fullname: String,
        email: String,
        phone: String,
        hourlyRate: Double,
        username: String,
        password: String
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Gọi AccountDAO để tạo account
            val accId = dbHelper.accountDAO.insertAccount(username, password, "EMPLOYEE")
            if (accId == -1L) return false

            val empValues = ContentValues().apply {
                put(DBHelper.COL_EMP_CODE, code)
                put(DBHelper.COL_EMP_FULLNAME, fullname)
                put(DBHelper.COL_EMP_EMAIL, email)
                put(DBHelper.COL_EMP_PHONE, phone)
                put(DBHelper.COL_EMP_HOURLY_RATE, hourlyRate)
                put(DBHelper.COL_EMP_ACC_ID, accId)
            }
            val empId = db.insert(DBHelper.TABLE_EMPLOYEE, null, empValues)
            if (empId == -1L) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun updateEmployee(
        employeeId: Long,
        fullname: String,
        email: String,
        phone: String,
        hourlyRate: Double,
        newPassword: String? = null
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val emp = getEmployeeById(employeeId) ?: return false

            val empValues = ContentValues().apply {
                put(DBHelper.COL_EMP_FULLNAME, fullname)
                put(DBHelper.COL_EMP_EMAIL, email)
                put(DBHelper.COL_EMP_PHONE, phone)
                put(DBHelper.COL_EMP_HOURLY_RATE, hourlyRate)
            }
            val updatedEmp = db.update(DBHelper.TABLE_EMPLOYEE, empValues, "${DBHelper.COL_EMP_ID}=?", arrayOf(employeeId.toString()))

            // Gọi AccountDAO để đổi mật khẩu
            if (!newPassword.isNullOrBlank()) {
                dbHelper.accountDAO.updatePassword(emp.accountId, newPassword)
            }

            db.setTransactionSuccessful()
            return updatedEmp > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteEmployee(employeeId: Long): Boolean {
        val emp = getEmployeeById(employeeId) ?: return false
        val db = dbHelper.writableDatabase
        val deletedRows = db.delete(DBHelper.TABLE_EMPLOYEE, "${DBHelper.COL_EMP_ID}=?", arrayOf(employeeId.toString()))
        // Gọi AccountDAO để xoá account liên kết
        dbHelper.accountDAO.deleteAccount(emp.accountId)
        return deletedRows > 0
    }
}
