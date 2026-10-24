package com.example.mobileappth2.dao

import android.content.ContentValues
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.models.Employee
import com.example.mobileappth2.models.WorkLog
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class WorkLogDAO(private val dbHelper: DBHelper) {

    fun getTodayWorkLog(employeeId: Long, todayDate: String): WorkLog? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_WORK_LOG,
            null,
            "${DBHelper.COL_WL_EMP_ID}=? AND ${DBHelper.COL_WL_WORK_DATE}=?",
            arrayOf(employeeId.toString(), todayDate),
            null, null, null
        )
        var workLog: WorkLog? = null
        if (cursor.moveToFirst()) {
            workLog = WorkLog(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_ID)),
                employeeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_EMP_ID)),
                workDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_WORK_DATE)),
                checkInTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_CHECK_IN)),
                checkOutTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_CHECK_OUT)),
                totalHours = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_TOTAL_HOURS)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_STATUS))
            )
        }
        cursor.close()
        return workLog
    }

    fun checkIn(employeeId: Long, workDate: String, checkInTime: String): Pair<Boolean, String> {
        val existingLog = getTodayWorkLog(employeeId, workDate)
        if (existingLog != null) {
            return Pair(false, "Bạn đã Check-In trong ngày hôm nay ($workDate) rồi!")
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_WL_EMP_ID, employeeId)
            put(DBHelper.COL_WL_WORK_DATE, workDate)
            put(DBHelper.COL_WL_CHECK_IN, checkInTime)
            put(DBHelper.COL_WL_STATUS, "CHECKED_IN")
        }
        val id = db.insert(DBHelper.TABLE_WORK_LOG, null, values)
        return if (id != -1L) {
            Pair(true, "Check-In thành công lúc $checkInTime!")
        } else {
            Pair(false, "Lỗi khi lưu dữ liệu Check-In!")
        }
    }

    fun checkOut(employeeId: Long, workDate: String, checkOutTime: String): Pair<Boolean, String> {
        val existingLog = getTodayWorkLog(employeeId, workDate)
            ?: return Pair(false, "Bạn chưa Check-In hôm nay ($workDate)!")

        if (existingLog.status == "COMPLETED") {
            return Pair(false, "Bạn đã hoàn thành Check-Out cho ngày hôm nay rồi!")
        }

        val calculatedHours = calculateHoursBetween(existingLog.checkInTime, checkOutTime)
        if (calculatedHours <= 0) {
            return Pair(false, "Thời gian Check-Out ($checkOutTime) phải muộn hơn thời gian Check-In (${existingLog.checkInTime})!")
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_WL_CHECK_OUT, checkOutTime)
            put(DBHelper.COL_WL_TOTAL_HOURS, calculatedHours)
            put(DBHelper.COL_WL_STATUS, "COMPLETED")
        }
        val updated = db.update(
            DBHelper.TABLE_WORK_LOG,
            values,
            "${DBHelper.COL_WL_ID}=?",
            arrayOf(existingLog.id.toString())
        )

        return if (updated > 0) {
            Pair(true, "Check-Out thành công! Tổng giờ làm hôm nay: $calculatedHours giờ.")
        } else {
            Pair(false, "Lỗi khi cập nhật Check-Out!")
        }
    }

    private fun calculateHoursBetween(checkIn: String, checkOut: String): Double {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateIn = format.parse(checkIn)
            val dateOut = format.parse(checkOut)
            if (dateIn != null && dateOut != null) {
                val diffMs = dateOut.time - dateIn.time
                if (diffMs <= 0) return 0.0
                val hours = diffMs.toDouble() / (1000 * 60 * 60)
                (hours * 100).roundToInt() / 100.0
            } else 0.0
        } catch (e: Exception) {
            try {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateIn = format.parse(checkIn)
                val dateOut = format.parse(checkOut)
                if (dateIn != null && dateOut != null) {
                    val diffMs = dateOut.time - dateIn.time
                    if (diffMs <= 0) return 0.0
                    val hours = diffMs.toDouble() / (1000 * 60 * 60)
                    (hours * 100).roundToInt() / 100.0
                } else 0.0
            } catch (ex: Exception) {
                0.0
            }
        }
    }

    fun getWorkLogsByEmployee(employeeId: Long): List<WorkLog> {
        val list = mutableListOf<WorkLog>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_WORK_LOG,
            null,
            "${DBHelper.COL_WL_EMP_ID}=?",
            arrayOf(employeeId.toString()),
            null, null,
            "${DBHelper.COL_WL_WORK_DATE} DESC, ${DBHelper.COL_WL_ID} DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    WorkLog(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_ID)),
                        employeeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_EMP_ID)),
                        workDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_WORK_DATE)),
                        checkInTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_CHECK_IN)),
                        checkOutTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_CHECK_OUT)),
                        totalHours = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_TOTAL_HOURS)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_STATUS))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAllWorkLogsWithEmployee(): List<Pair<WorkLog, Employee>> {
        val list = mutableListOf<Pair<WorkLog, Employee>>()
        val employeesMap = dbHelper.employeeDAO.getAllEmployees().associateBy { it.id }
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_WORK_LOG,
            null, null, null, null, null,
            "${DBHelper.COL_WL_WORK_DATE} DESC, ${DBHelper.COL_WL_ID} DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                val empId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_EMP_ID))
                val emp = employeesMap[empId]
                if (emp != null) {
                    val log = WorkLog(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_ID)),
                        employeeId = empId,
                        workDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_WORK_DATE)),
                        checkInTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_CHECK_IN)),
                        checkOutTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_CHECK_OUT)),
                        totalHours = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_TOTAL_HOURS)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WL_STATUS))
                    )
                    list.add(Pair(log, emp))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
