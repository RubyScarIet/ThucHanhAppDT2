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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.models.Employee
import com.example.mobileappth2.models.Salary
import com.example.mobileappth2.models.WorkLog
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    dbHelper: DBHelper,
    adminUsername: String,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quản Trị Hệ Thống", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Admin: $adminUsername", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    text = { Text("Nhân Viên", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Bảng Lương", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Chấm Công", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                )
            }

            when (selectedTab) {
                0 -> EmployeeManagementTab(dbHelper)
                1 -> SalaryManagementTab(dbHelper)
                2 -> AttendanceHistoryTab(dbHelper)
            }
        }
    }
}

@Composable
fun EmployeeManagementTab(dbHelper: DBHelper) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var employeeList by remember { mutableStateOf<List<Employee>>(emptyList()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    fun refreshList() {
        employeeList = if (searchQuery.isBlank()) {
            dbHelper.employeeDAO.getAllEmployees()
        } else {
            dbHelper.employeeDAO.searchEmployees(searchQuery.trim())
        }
    }

    LaunchedEffect(searchQuery) {
        refreshList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm tên hoặc mã NV...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Danh sách (${employeeList.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("+ Thêm NV", fontSize = 12.sp)
                }
            }

            if (employeeList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không tìm thấy nhân viên nào", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(employeeList) { emp ->
                        EmployeeItemCard(
                            employee = emp,
                            onEdit = { employeeToEdit = emp },
                            onDelete = { employeeToDelete = emp }
                        )
                    }
                }
            }
        }

        if (showAddDialog || employeeToEdit != null) {
            AddEditEmployeeDialog(
                employeeToEdit = employeeToEdit,
                onDismiss = {
                    showAddDialog = false
                    employeeToEdit = null
                },
                onSave = { code, fullname, email, phone, rate, username, password ->
                    if (employeeToEdit == null) {
                        val success = dbHelper.employeeDAO.addEmployee(code, fullname, email, phone, rate, username, password)
                        if (success) {
                            Toast.makeText(context, "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show()
                            refreshList()
                        } else {
                            Toast.makeText(context, "Mã NV hoặc Tên đăng nhập đã tồn tại!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val success = dbHelper.employeeDAO.updateEmployee(employeeToEdit!!.id, fullname, email, phone, rate, password)
                        if (success) {
                            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                            refreshList()
                        } else {
                            Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showAddDialog = false
                    employeeToEdit = null
                }
            )
        }

        if (employeeToDelete != null) {
            AlertDialog(
                onDismissRequest = { employeeToDelete = null },
                title = { Text("Xác nhận xóa", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = { Text("Bạn có chắc muốn xóa nhân viên ${employeeToDelete!!.fullname} (${employeeToDelete!!.code})?", fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = dbHelper.employeeDAO.deleteEmployee(employeeToDelete!!.id)
                            if (success) {
                                Toast.makeText(context, "Đã xóa nhân viên!", Toast.LENGTH_SHORT).show()
                                refreshList()
                            } else {
                                Toast.makeText(context, "Xóa thất bại!", Toast.LENGTH_SHORT).show()
                            }
                            employeeToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa", fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { employeeToDelete = null }) {
                        Text("Hủy", fontSize = 12.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun EmployeeItemCard(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${employee.code} — ${employee.fullname}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%,.0f", employee.hourlyRate)} đ/h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Email: ${employee.email.ifBlank { "Chưa có" }}  |  SĐT: ${employee.phone.ifBlank { "Chưa có" }}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Sửa", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Xóa", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditEmployeeDialog(
    employeeToEdit: Employee?,
    onDismiss: () -> Unit,
    onSave: (code: String, fullname: String, email: String, phone: String, rate: Double, username: String, password: String) -> Unit
) {
    var code by remember { mutableStateOf(employeeToEdit?.code ?: "") }
    var fullname by remember { mutableStateOf(employeeToEdit?.fullname ?: "") }
    var email by remember { mutableStateOf(employeeToEdit?.email ?: "") }
    var phone by remember { mutableStateOf(employeeToEdit?.phone ?: "") }
    var rateStr by remember { mutableStateOf(employeeToEdit?.hourlyRate?.let { String.format(Locale.US, "%.0f", it) } ?: "50000") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employeeToEdit == null) "Thêm Nhân Viên" else "Cập Nhật Nhân Viên", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (employeeToEdit == null) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Mã NV (ví dụ: EMP003)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = fullname,
                    onValueChange = { fullname = it },
                    label = { Text("Họ và Tên") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Mức lương theo giờ (VNĐ)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (employeeToEdit == null) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Tên đăng nhập") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (employeeToEdit == null) "Mật khẩu" else "Mật khẩu mới (bỏ trống nếu giữ nguyên)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (employeeToEdit == null && (code.isBlank() || username.isBlank() || password.isBlank())) {
                        errorMsg = "Vui lòng nhập đủ Mã NV, Tên đăng nhập & Mật khẩu!"
                        return@Button
                    }
                    if (fullname.isBlank()) {
                        errorMsg = "Họ tên không được bỏ trống!"
                        return@Button
                    }
                    val rate = rateStr.toDoubleOrNull() ?: 0.0
                    if (rate <= 0) {
                        errorMsg = "Mức lương phải lớn hơn 0!"
                        return@Button
                    }
                    onSave(code.trim(), fullname.trim(), email.trim(), phone.trim(), rate, username.trim(), password.trim())
                }
            ) {
                Text("Lưu", fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy", fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun SalaryManagementTab(dbHelper: DBHelper) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }

    var salaryList by remember { mutableStateOf<List<Pair<Salary, Employee>>>(emptyList()) }

    fun refreshSalaryData() {
        salaryList = dbHelper.salaryDAO.getAllSalariesWithEmployee(selectedMonth, selectedYear)
    }

    LaunchedEffect(selectedMonth, selectedYear) {
        refreshSalaryData()
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

        Spacer(modifier = Modifier.height(12.dp))

        if (salaryList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có dữ liệu lương", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(salaryList) { pair ->
                    val salary = pair.first
                    val emp = pair.second

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${emp.code} — ${emp.fullname}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                val isPaid = salary.status == "PAID"
                                Text(
                                    text = if (isPaid) "ĐÃ THANH TOÁN" else "CHƯA THANH TOÁN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Số giờ: ${salary.totalHours}h  |  Tăng ca (OT): ${salary.overtimeHours}h  |  Đơn giá: ${String.format(Locale.getDefault(), "%,.0f", emp.hourlyRate)} đ/h", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LƯƠNG: ${String.format(Locale.getDefault(), "%,.0f", salary.totalSalary)} VNĐ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Button(
                                    onClick = {
                                        val newStatus = if (salary.status == "PAID") "UNPAID" else "PAID"
                                        dbHelper.salaryDAO.updateSalaryStatus(emp.id, selectedMonth, selectedYear, newStatus)
                                        Toast.makeText(context, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show()
                                        refreshSalaryData()
                                    },
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (salary.status == "PAID") "Bỏ thanh toán" else "Đánh dấu đã trả",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceHistoryTab(dbHelper: DBHelper) {
    var logsList by remember { mutableStateOf<List<Pair<WorkLog, Employee>>>(emptyList()) }

    LaunchedEffect(Unit) {
        logsList = dbHelper.workLogDAO.getAllWorkLogsWithEmployee()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lịch sử chấm công toàn công ty", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        if (logsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có lịch sử chấm công", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logsList) { pair ->
                    val log = pair.first
                    val emp = pair.second

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
                                Text("${emp.code} — ${emp.fullname}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(log.workDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("In: ${log.checkInTime}  |  Out: ${log.checkOutTime ?: "---"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Tổng: ${log.totalHours}h", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
