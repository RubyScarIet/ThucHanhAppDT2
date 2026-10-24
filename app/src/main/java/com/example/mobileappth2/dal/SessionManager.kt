package com.example.mobileappth2.dal

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val PREF_NAME = "EmployeeManagerSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_EMPLOYEE_ID = "employeeId"
    }

    fun saveSession(userId: Long, username: String, role: String, employeeId: Long = -1) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_ROLE, role)
        editor.putLong(KEY_EMPLOYEE_ID, employeeId)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): Long {
        return pref.getLong(KEY_USER_ID, -1)
    }

    fun getUsername(): String {
        return pref.getString(KEY_USERNAME, "") ?: ""
    }

    fun getUserRole(): String {
        return pref.getString(KEY_ROLE, "") ?: ""
    }

    fun getEmployeeId(): Long {
        return pref.getLong(KEY_EMPLOYEE_ID, -1)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}
