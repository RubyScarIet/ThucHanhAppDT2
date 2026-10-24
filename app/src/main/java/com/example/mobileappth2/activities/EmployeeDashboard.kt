package com.example.mobileappth2.activities

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.models.Employee
import com.example.mobileappth2.models.Salary
import com.example.mobileappth2.models.WorkLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboard(
    dbHelper: DBHelper,
    employee: Employee,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cổng Nhân Viên", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${employee.fullname} (${employee.code})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Đăng xuất", color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Chấm Công", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Lịch Sử", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Phiếu Lương", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                )
            }

            when (selectedTab) {
                0 -> CheckInCheckOutTab(dbHelper, employee)
                1 -> EmployeeAttendanceHistoryTab(dbHelper, employee)
                2 -> EmployeeSalaryTab(dbHelper, employee)
            }
        }
    }
}

// ==========================================
// TAB 1: CHECK-IN / CHECK-OUT TAB
// ==========================================
@Composable
fun CheckInCheckOutTab(dbHelper: DBHelper, employee: Employee) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    var todayDateStr by remember { mutableStateOf(dateFormat.format(Date())) }
    var todayWorkLog by remember { mutableStateOf<WorkLog?>(null) }

    fun refreshTodayLog() {
        todayDateStr = dateFormat.format(Date())
        todayWorkLog = dbHelper.workLogDAO.getTodayWorkLog(employee.id, todayDateStr)
    }

    LaunchedEffect(Unit) {
        refreshTodayLog()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hôm nay: $todayDateStr", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Lương: ${String.format(Locale.getDefault(), "%,.0f", employee.hourlyRate)} đ/h", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Trạng Thái Ca Làm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                when (todayWorkLog?.status) {
                    "CHECKED_IN" -> {
                        Text(text = "Đang làm việc", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Check-In: ${todayWorkLog!!.checkInTime}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    "COMPLETED" -> {
                        Text(text = "Hoàn thành ca làm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "In: ${todayWorkLog!!.checkInTime}  |  Out: ${todayWorkLog!!.checkOutTime}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Tổng: ${todayWorkLog!!.totalHours} giờ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                    }
                    else -> {
                        Text(text = "Chưa Check-In", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isCheckedIn = todayWorkLog != null
            val isCompleted = todayWorkLog?.status == "COMPLETED"

            Button(
                onClick = {
                    val currentTimeStr = timeFormat.format(Date())
                    val result = dbHelper.workLogDAO.checkIn(employee.id, todayDateStr, currentTimeStr)
                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                    if (result.first) refreshTodayLog()
                },
                enabled = !isCheckedIn,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Check-In", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val currentTimeStr = timeFormat.format(Date())
                    val result = dbHelper.workLogDAO.checkOut(employee.id, todayDateStr, currentTimeStr)
                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                    if (result.first) refreshTodayLog()
                },
                enabled = isCheckedIn && !isCompleted,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Check-Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ==========================================
// TAB 2: EMPLOYEE ATTENDANCE HISTORY TAB
// ==========================================
@Composable
fun EmployeeAttendanceHistoryTab(dbHelper: DBHelper, employee: Employee) {
    var logsList by remember { mutableStateOf<List<WorkLog>>(emptyList()) }

    LaunchedEffect(Unit) {
        logsList = dbHelper.workLogDAO.getWorkLogsByEmployee(employee.id)
    }

    val totalHoursSum = logsList.filter { it.status == "COMPLETED" }.sumOf { it.totalHours }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tổng số giờ tích lũy:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${String.format(Locale.getDefault(), "%.1f", totalHoursSum)} giờ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        if (logsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có lịch sử chấm công", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logsList) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ngày: ${log.workDate}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (log.status == "COMPLETED") "Hoàn thành" else "Đang làm",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (log.status == "COMPLETED") Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("In: ${log.checkInTime}  |  Out: ${log.checkOutTime ?: "---"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (log.status == "COMPLETED") {
                                    Text("${log.totalHours}h", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: EMPLOYEE SALARY TAB
// ==========================================
@Composable
fun EmployeeSalaryTab(dbHelper: DBHelper, employee: Employee) {
    val cal = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }

    var currentSalary by remember { mutableStateOf<Salary?>(null) }

    fun loadSalary() {
        currentSalary = dbHelper.salaryDAO.calculateAndSaveSalary(employee.id, selectedMonth, selectedYear)
    }

    LaunchedEffect(selectedMonth, selectedYear) {
        loadSalary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tháng $selectedMonth/$selectedYear", fontSize = 15.sp, fontWeight = FontWeight.Bold)

            Row {
                OutlinedButton(
                    onClick = {
                        if (selectedMonth > 1) selectedMonth--
                        else {
                            selectedMonth = 12
                            selectedYear--
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("< Tháng trước", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = {
                        if (selectedMonth < 12) selectedMonth++
                        else {
                            selectedMonth = 1
                            selectedYear++
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Tháng sau >", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentSalary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có dữ liệu lương", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            val sal = currentSalary!!
            val regularHours = sal.totalHours - sal.overtimeHours
            val basePay = regularHours * employee.hourlyRate
            val overtimePay = sal.overtimeHours * employee.hourlyRate * 1.5

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PHIẾU LƯƠNG THÁNG $selectedMonth/$selectedYear",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SalaryDetailRow("Nhân viên:", "${employee.fullname} (${employee.code})")
                    SalaryDetailRow("Mức lương cơ bản:", "${String.format(Locale.getDefault(), "%,.0f", employee.hourlyRate)} đ/h")
                    SalaryDetailRow("Tổng số giờ làm:", "${sal.totalHours} giờ")
                    SalaryDetailRow("Giờ tăng ca (OT > 8h/ngày):", "${sal.overtimeHours} giờ")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SalaryDetailRow("Lương giờ chuẩn:", "${String.format(Locale.getDefault(), "%,.0f", basePay)} VNĐ")
                    SalaryDetailRow("Thưởng OT (1.5x):", "${String.format(Locale.getDefault(), "%,.0f", overtimePay)} VNĐ")

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("THỰC LĨNH:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "${String.format(Locale.getDefault(), "%,.0f", sal.totalSalary)} VNĐ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trạng thái:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val isPaid = sal.status == "PAID"
                        Text(
                            text = if (isPaid) "ĐÃ THANH TOÁN" else "CHƯA THANH TOÁN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalaryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
