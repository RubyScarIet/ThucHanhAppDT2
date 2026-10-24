package com.example.mobileappth2.dal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mobileappth2.dao.AccountDAO
import com.example.mobileappth2.dao.EmployeeDAO
import com.example.mobileappth2.dao.SalaryDAO
import com.example.mobileappth2.dao.WorkLogDAO

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val accountDAO by lazy { AccountDAO(this) }
    val employeeDAO by lazy { EmployeeDAO(this) }
    val workLogDAO by lazy { WorkLogDAO(this) }
    val salaryDAO by lazy { SalaryDAO(this) }

    companion object {
        const val DATABASE_NAME = "EmployeeHoursSalary.db"
        const val DATABASE_VERSION = 2

        // Table Names
        const val TABLE_ACCOUNT = "tbl_account"
        const val TABLE_EMPLOYEE = "tbl_employee"
        const val TABLE_WORK_LOG = "tbl_work_log"
        const val TABLE_SALARY = "tbl_salary"

        // Account Columns
        const val COL_ACC_ID = "id"
        const val COL_ACC_USERNAME = "username"
        const val COL_ACC_PASSWORD = "password"
        const val COL_ACC_ROLE = "role"

        // Employee Columns
        const val COL_EMP_ID = "id"
        const val COL_EMP_CODE = "code"
        const val COL_EMP_FULLNAME = "fullname"
        const val COL_EMP_EMAIL = "email"
        const val COL_EMP_PHONE = "phone"
        const val COL_EMP_HOURLY_RATE = "hourly_rate"
        const val COL_EMP_ACC_ID = "account_id"

        // WorkLog Columns
        const val COL_WL_ID = "id"
        const val COL_WL_EMP_ID = "employee_id"
        const val COL_WL_WORK_DATE = "work_date"
        const val COL_WL_CHECK_IN = "check_in_time"
        const val COL_WL_CHECK_OUT = "check_out_time"
        const val COL_WL_TOTAL_HOURS = "total_hours"
        const val COL_WL_STATUS = "status"

        // Salary Columns
        const val COL_SAL_ID = "id"
        const val COL_SAL_EMP_ID = "employee_id"
        const val COL_SAL_MONTH = "month"
        const val COL_SAL_YEAR = "year"
        const val COL_SAL_TOTAL_HOURS = "total_hours"
        const val COL_SAL_OVERTIME_HOURS = "overtime_hours"
        const val COL_SAL_TOTAL_SALARY = "total_salary"
        const val COL_SAL_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAccountTable = """
            CREATE TABLE $TABLE_ACCOUNT (
                $COL_ACC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ACC_USERNAME TEXT UNIQUE NOT NULL,
                $COL_ACC_PASSWORD TEXT NOT NULL,
                $COL_ACC_ROLE TEXT NOT NULL
            )
        """.trimIndent()

        val createEmployeeTable = """
            CREATE TABLE $TABLE_EMPLOYEE (
                $COL_EMP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EMP_CODE TEXT UNIQUE NOT NULL,
                $COL_EMP_FULLNAME TEXT NOT NULL,
                $COL_EMP_EMAIL TEXT,
                $COL_EMP_PHONE TEXT,
                $COL_EMP_HOURLY_RATE REAL NOT NULL,
                $COL_EMP_ACC_ID INTEGER NOT NULL,
                FOREIGN KEY($COL_EMP_ACC_ID) REFERENCES $TABLE_ACCOUNT($COL_ACC_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createWorkLogTable = """
            CREATE TABLE $TABLE_WORK_LOG (
                $COL_WL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WL_EMP_ID INTEGER NOT NULL,
                $COL_WL_WORK_DATE TEXT NOT NULL,
                $COL_WL_CHECK_IN TEXT NOT NULL,
                $COL_WL_CHECK_OUT TEXT,
                $COL_WL_TOTAL_HOURS REAL DEFAULT 0.0,
                $COL_WL_STATUS TEXT NOT NULL,
                FOREIGN KEY($COL_WL_EMP_ID) REFERENCES $TABLE_EMPLOYEE($COL_EMP_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createSalaryTable = """
            CREATE TABLE $TABLE_SALARY (
                $COL_SAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SAL_EMP_ID INTEGER NOT NULL,
                $COL_SAL_MONTH INTEGER NOT NULL,
                $COL_SAL_YEAR INTEGER NOT NULL,
                $COL_SAL_TOTAL_HOURS REAL DEFAULT 0.0,
                $COL_SAL_OVERTIME_HOURS REAL DEFAULT 0.0,
                $COL_SAL_TOTAL_SALARY REAL DEFAULT 0.0,
                $COL_SAL_STATUS TEXT NOT NULL DEFAULT 'UNPAID',
                FOREIGN KEY($COL_SAL_EMP_ID) REFERENCES $TABLE_EMPLOYEE($COL_EMP_ID) ON DELETE CASCADE,
                UNIQUE($COL_SAL_EMP_ID, $COL_SAL_MONTH, $COL_SAL_YEAR)
            )
        """.trimIndent()

        db.execSQL(createAccountTable)
        db.execSQL(createEmployeeTable)
        db.execSQL(createWorkLogTable)
        db.execSQL(createSalaryTable)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALARY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORK_LOG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPLOYEE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNT")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // Seed Admin Account
        val adminValues = ContentValues().apply {
            put(COL_ACC_USERNAME, "admin")
            put(COL_ACC_PASSWORD, "admin123")
            put(COL_ACC_ROLE, "ADMIN")
        }
        db.insert(TABLE_ACCOUNT, null, adminValues)

        // Seed Employee 1
        val emp1Acc = ContentValues().apply {
            put(COL_ACC_USERNAME, "emp1")
            put(COL_ACC_PASSWORD, "123456")
            put(COL_ACC_ROLE, "EMPLOYEE")
        }
        val emp1AccId = db.insert(TABLE_ACCOUNT, null, emp1Acc)

        val emp1Values = ContentValues().apply {
            put(COL_EMP_CODE, "EMP001")
            put(COL_EMP_FULLNAME, "Nguyễn Văn A")
            put(COL_EMP_EMAIL, "nguyenvana@gmail.com")
            put(COL_EMP_PHONE, "0901234567")
            put(COL_EMP_HOURLY_RATE, 50000.0)
            put(COL_EMP_ACC_ID, emp1AccId)
        }
        db.insert(TABLE_EMPLOYEE, null, emp1Values)

        // Seed Employee 2
        val emp2Acc = ContentValues().apply {
            put(COL_ACC_USERNAME, "emp2")
            put(COL_ACC_PASSWORD, "123456")
            put(COL_ACC_ROLE, "EMPLOYEE")
        }
        val emp2AccId = db.insert(TABLE_ACCOUNT, null, emp2Acc)

        val emp2Values = ContentValues().apply {
            put(COL_EMP_CODE, "EMP002")
            put(COL_EMP_FULLNAME, "Trần Thị B")
            put(COL_EMP_EMAIL, "tranthib@gmail.com")
            put(COL_EMP_PHONE, "0987654321")
            put(COL_EMP_HOURLY_RATE, 65000.0)
            put(COL_EMP_ACC_ID, emp2AccId)
        }
        db.insert(TABLE_EMPLOYEE, null, emp2Values)

        // Seed Employee 3
        val emp3Acc = ContentValues().apply {
            put(COL_ACC_USERNAME, "emp3")
            put(COL_ACC_PASSWORD, "123456")
            put(COL_ACC_ROLE, "EMPLOYEE")
        }
        val emp3AccId = db.insert(TABLE_ACCOUNT, null, emp3Acc)

        val emp3Values = ContentValues().apply {
            put(COL_EMP_CODE, "EMP003")
            put(COL_EMP_FULLNAME, "Lê Văn C")
            put(COL_EMP_EMAIL, "levanc@gmail.com")
            put(COL_EMP_PHONE, "0912345678")
            put(COL_EMP_HOURLY_RATE, 70000.0)
            put(COL_EMP_ACC_ID, emp3AccId)
        }
        db.insert(TABLE_EMPLOYEE, null, emp3Values)
    }

}

