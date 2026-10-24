package com.example.mobileappth2.dao

import android.content.ContentValues
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.models.Account

class AccountDAO(private val dbHelper: DBHelper) {

    fun login(username: String, password: String): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_ACCOUNT,
            null,
            "${DBHelper.COL_ACC_USERNAME}=? AND ${DBHelper.COL_ACC_PASSWORD}=?",
            arrayOf(username, password),
            null, null, null
        )
        var account: Account? = null
        if (cursor.moveToFirst()) {
            account = Account(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_PASSWORD)),
                role = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_ROLE))
            )
        }
        cursor.close()
        return account
    }

    fun getAccountById(accountId: Long): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_ACCOUNT,
            null,
            "${DBHelper.COL_ACC_ID}=?",
            arrayOf(accountId.toString()),
            null, null, null
        )
        var account: Account? = null
        if (cursor.moveToFirst()) {
            account = Account(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_PASSWORD)),
                role = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_ROLE))
            )
        }
        cursor.close()
        return account
    }

    fun insertAccount(username: String, password: String, role: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_ACC_USERNAME, username)
            put(DBHelper.COL_ACC_PASSWORD, password)
            put(DBHelper.COL_ACC_ROLE, role)
        }
        return db.insert(DBHelper.TABLE_ACCOUNT, null, values)
    }

    fun updatePassword(accountId: Long, newPassword: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_ACC_PASSWORD, newPassword)
        }
        val updated = db.update(
            DBHelper.TABLE_ACCOUNT,
            values,
            "${DBHelper.COL_ACC_ID}=?",
            arrayOf(accountId.toString())
        )
        return updated > 0
    }

    fun deleteAccount(accountId: Long): Boolean {
        val db = dbHelper.writableDatabase
        val deleted = db.delete(
            DBHelper.TABLE_ACCOUNT,
            "${DBHelper.COL_ACC_ID}=?",
            arrayOf(accountId.toString())
        )
        return deleted > 0
    }
}
