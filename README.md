# MobileAppTH2 — Quản Lý Giờ Làm & Bảng Lương Nhân Viên

## Giới Thiệu Dự Án

Ứng dụng Android quản lý chấm công, giờ làm và bảng lương nhân viên, xây dựng bằng **Kotlin + Jetpack Compose + SQLite**.

### Cấu Trúc Thư Mục

```
com.example.mobileappth2/
├── MainActivity.kt                  ← Điều hướng chính (TV1)
├── models/
│   ├── Account.kt                   ← Data class tài khoản (TV1)
│   ├── Employee.kt                  ← Data class nhân viên (TV1)
│   ├── WorkLog.kt                   ← Data class chấm công (TV1)
│   └── Salary.kt                    ← Data class bảng lương (TV1)
├── dal/
│   ├── DBHelper.kt                  ← Quản lý DB schema & seed (TV1)
│   └── SessionManager.kt           ← Quản lý phiên đăng nhập (TV1)
├── dao/
│   ├── AccountDAO.kt                ← CRUD tài khoản (TV1)
│   ├── EmployeeDAO.kt               ← CRUD nhân viên (TV2)
│   ├── WorkLogDAO.kt                ← CRUD chấm công (TV3)
│   └── SalaryDAO.kt                 ← CRUD & tính lương (TV3)
└── activities/
    ├── LoginScreen.kt               ← Màn hình đăng nhập (TV1)
    ├── AdminDashboard.kt            ← Trang quản trị Admin (TV2)
    └── EmployeeDashboard.kt         ← Cổng nhân viên (TV3)
```

### Sơ Đồ Quan Hệ Bảng (ERD)

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ tbl_account  │       │  tbl_employee    │       │  tbl_work_log    │
├──────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)      │◄──FK──│ account_id       │       │ id (PK)          │
│ username     │       │ id (PK)          │◄──FK──│ employee_id      │
│ password     │       │ code (UNIQUE)    │       │ work_date        │
│ role         │       │ fullname         │       │ check_in_time    │
└──────────────┘       │ email            │       │ check_out_time   │
                       │ phone            │       │ total_hours      │
                       │ hourly_rate      │       │ status           │
                       └──────────────────┘       └──────────────────┘
                              │
                              │ FK
                              ▼
                       ┌──────────────────┐
                       │  tbl_salary      │
                       ├──────────────────┤
                       │ id (PK)          │
                       │ employee_id      │
                       │ month            │
                       │ year             │
                       │ total_hours      │
                       │ overtime_hours   │
                       │ total_salary     │
                       │ status           │
                       │ UNIQUE(emp,m,y)  │
                       └──────────────────┘
```

---

## THÀNH VIÊN 1 — Database, Xác Thực & Điều Hướng

### Phạm vi: 9 file

| File | Dòng | Vai trò |
|------|------|---------|
| `Account.kt` | 9 | Model tài khoản |
| `Employee.kt` | 12 | Model nhân viên |
| `WorkLog.kt` | 12 | Model chấm công |
| `Salary.kt` | 13 | Model bảng lương |
| `DBHelper.kt` | 197 | Schema DB + Seed data |
| `SessionManager.kt` | 53 | Lưu session SharedPreferences |
| `AccountDAO.kt` | 87 | CRUD bảng tbl_account |
| `LoginScreen.kt` | 175 | UI đăng nhập |
| `MainActivity.kt` | 129 | Entry point + điều hướng |

### Luồng xử lý chi tiết

#### 1.1. Khởi tạo Database (`DBHelper.kt`)

```
App khởi động
    │
    ▼
MainActivity.onCreate()          ← dòng 35-40
    │  dbHelper = DBHelper(this)
    │  sessionManager = SessionManager(this)
    │
    ▼
DBHelper.onCreate(db)            ← dòng 64-121
    │  CREATE TABLE tbl_account   (dòng 65-72)
    │  CREATE TABLE tbl_employee  (dòng 74-85)
    │  CREATE TABLE tbl_work_log  (dòng 87-98)
    │  CREATE TABLE tbl_salary    (dòng 100-113)
    │
    ▼
seedInitialData(db)              ← dòng 131-193
    │  INSERT admin (admin/admin123, role=ADMIN)
    │  INSERT emp1  (emp1/123456, role=EMPLOYEE) → EMP001 Nguyễn Văn A, 50.000đ/h
    │  INSERT emp2  (emp2/123456, role=EMPLOYEE) → EMP002 Trần Thị B, 65.000đ/h
    │  INSERT emp3  (emp3/123456, role=EMPLOYEE) → EMP003 Lê Văn C, 70.000đ/h
```

**Giải thích:** `DBHelper` kế thừa `SQLiteOpenHelper`. Khi DB chưa tồn tại, `onCreate()` được gọi tự động để tạo 4 bảng và seed 4 tài khoản mẫu (1 admin + 3 nhân viên). Mỗi nhân viên có 1 account riêng liên kết qua `account_id` (FK).

#### 1.2. Đăng nhập (`AccountDAO.kt` + `LoginScreen.kt`)

```
Người dùng nhập username + password
    │
    ▼
LoginScreen → Button "Đăng Nhập"       ← LoginScreen.kt dòng 127-143
    │
    ▼
dbHelper.accountDAO.login(username, password)    ← AccountDAO.kt dòng 9-29
    │  SELECT * FROM tbl_account
    │  WHERE username=? AND password=?
    │
    ├── null → Hiển thị "Tên đăng nhập hoặc mật khẩu không chính xác!"
    │
    └── Account(id, username, password, role)
         │
         ├── role == "ADMIN"
         │       └── onLoginSuccess(account, null) → AdminDashboard
         │
         └── role == "EMPLOYEE"
                 │
                 ▼
                 dbHelper.employeeDAO.getEmployeeByAccountId(acc.id)
                       ← EmployeeDAO.kt dòng 82-105
                       SELECT * FROM tbl_employee WHERE account_id=?
                 │
                 └── onLoginSuccess(account, employee) → EmployeeDashboard
```

#### 1.3. Điều hướng & Quản lý Session (`MainActivity.kt` + `SessionManager.kt`)

```
MainAppNavigation()                     ← MainActivity.kt dòng 53-128
    │
    ▼
sessionManager.isLoggedIn()?            ← SessionManager.kt dòng 28-30
    │                                      SharedPreferences: KEY_IS_LOGGED_IN
    │
    ├── false → AppScreen.Login (hiển thị LoginScreen)
    │
    └── true
         │ role = sessionManager.getUserRole()      ← dòng 40-42
         │
         ├── "ADMIN" → AppScreen.Admin(username)
         │               → hiển thị AdminDashboard
         │
         └── "EMPLOYEE"
              │ empId = sessionManager.getEmployeeId()   ← dòng 44-46
              │ emp = dbHelper.employeeDAO.getEmployeeById(empId)
              │
              ├── emp != null → AppScreen.EmployeePortal(emp)
              │                  → hiển thị EmployeeDashboard
              │
              └── emp == null → clearSession() → AppScreen.Login

Đăng xuất:
    sessionManager.clearSession()       ← SessionManager.kt dòng 48-51
    │  editor.clear() → xoá toàn bộ SharedPreferences
    └── currentScreen = AppScreen.Login
```

#### 1.4. AccountDAO — Danh sách hàm

| Hàm | Dòng | SQL | Mô tả |
|-----|------|-----|-------|
| `login()` | 9-29 | `SELECT ... WHERE username=? AND password=?` | Xác thực đăng nhập |
| `getAccountById()` | 31-51 | `SELECT ... WHERE id=?` | Lấy account theo ID |
| `insertAccount()` | 53-61 | `INSERT INTO tbl_account` | Tạo account mới (được `EmployeeDAO.addEmployee()` gọi) |
| `updatePassword()` | 63-75 | `UPDATE tbl_account SET password=? WHERE id=?` | Đổi mật khẩu (được `EmployeeDAO.updateEmployee()` gọi) |
| `deleteAccount()` | 77-85 | `DELETE FROM tbl_account WHERE id=?` | Xoá account (được `EmployeeDAO.deleteEmployee()` gọi) |

---

## THÀNH VIÊN 2 — Quản Trị Admin (AdminDashboard)

### Phạm vi: 2 file

| File | Dòng | Vai trò |
|------|------|---------|
| `EmployeeDAO.kt` | 189 | CRUD nhân viên + liên kết AccountDAO |
| `AdminDashboard.kt` | 658 | UI quản trị 3 tab |

### Luồng xử lý chi tiết

#### 2.1. Tab Quản Lý Nhân Viên (`EmployeeManagementTab`)

```
AdminDashboard → Tab "Nhân Viên"        ← AdminDashboard.kt dòng 120-263
    │
    ▼
refreshList()                            ← dòng 130-136
    │
    ├── searchQuery rỗng
    │     └── dbHelper.employeeDAO.getAllEmployees()
    │           ← EmployeeDAO.kt dòng 9-30
    │           SELECT * FROM tbl_employee ORDER BY code ASC
    │
    └── searchQuery có giá trị
          └── dbHelper.employeeDAO.searchEmployees(query)
                ← EmployeeDAO.kt dòng 32-55
                SELECT * FROM tbl_employee
                WHERE code LIKE '%query%' OR fullname LIKE '%query%'
                ORDER BY code ASC
```

##### Thêm nhân viên mới:

```
Nhấn "+ Thêm NV" → AddEditEmployeeDialog       ← dòng 329-452
    │  Nhập: Mã NV, Họ tên, Email, SĐT, Lương/giờ, Username, Password
    │
    ▼
Nhấn "Lưu"
    │  Validate: code, username, password không rỗng; rate > 0
    │
    ▼
dbHelper.employeeDAO.addEmployee(code, fullname, email, phone, rate, username, password)
    ← EmployeeDAO.kt dòng 107-142
    │
    │  db.beginTransaction()                           ← dòng 117
    │  │
    │  ▼
    │  BƯỚC 1: dbHelper.accountDAO.insertAccount(username, password, "EMPLOYEE")
    │           ← AccountDAO.kt dòng 53-61
    │           INSERT INTO tbl_account (username, password, role)
    │           VALUES (?, ?, 'EMPLOYEE')
    │           → Trả về accId
    │  │
    │  ▼
    │  BƯỚC 2: INSERT INTO tbl_employee
    │           (code, fullname, email, phone, hourly_rate, account_id)
    │           VALUES (?, ?, ?, ?, ?, accId)           ← dòng 123-132
    │  │
    │  ▼
    │  db.setTransactionSuccessful()                    ← dòng 134
    │  db.endTransaction()                              ← dòng 140
    │
    ├── true  → Toast "Thêm nhân viên thành công!" + refreshList()
    └── false → Toast "Mã NV hoặc Tên đăng nhập đã tồn tại!"
```

##### Cập nhật nhân viên:

```
Nhấn "Sửa" trên card NV → AddEditEmployeeDialog (chế độ edit)
    │  Các field được điền sẵn từ employeeToEdit
    │  Mã NV + Username không cho sửa (ẩn field)
    │
    ▼
dbHelper.employeeDAO.updateEmployee(id, fullname, email, phone, rate, password)
    ← EmployeeDAO.kt dòng 144-178
    │
    │  db.beginTransaction()
    │  │
    │  ▼
    │  BƯỚC 1: getEmployeeById(id) → lấy emp.accountId     ← dòng 155
    │  │
    │  ▼
    │  BƯỚC 2: UPDATE tbl_employee
    │           SET fullname=?, email=?, phone=?, hourly_rate=?
    │           WHERE id=?                                   ← dòng 157-163
    │  │
    │  ▼
    │  BƯỚC 3: Nếu newPassword không rỗng:
    │           dbHelper.accountDAO.updatePassword(emp.accountId, newPassword)
    │           ← AccountDAO.kt dòng 63-75
    │           UPDATE tbl_account SET password=? WHERE id=?
    │  │
    │  ▼
    │  db.setTransactionSuccessful()
```

##### Xoá nhân viên:

```
Nhấn "Xóa" → AlertDialog xác nhận       ← AdminDashboard.kt dòng 233-261
    │
    ▼
dbHelper.employeeDAO.deleteEmployee(id)
    ← EmployeeDAO.kt dòng 180-187
    │
    │  BƯỚC 1: getEmployeeById(id) → lấy emp.accountId     ← dòng 181
    │  BƯỚC 2: DELETE FROM tbl_employee WHERE id=?          ← dòng 183
    │  BƯỚC 3: dbHelper.accountDAO.deleteAccount(emp.accountId)
    │           ← AccountDAO.kt dòng 77-85
    │           DELETE FROM tbl_account WHERE id=?
    │
    ├── true  → Toast "Đã xóa nhân viên!" + refreshList()
    └── false → Toast "Xóa thất bại!"
```

#### 2.2. Tab Bảng Lương (`SalaryManagementTab`)

```
AdminDashboard → Tab "Bảng Lương"        ← AdminDashboard.kt dòng 457-595
    │
    │  selectedMonth, selectedYear (mặc định = tháng/năm hiện tại)
    │  Nút "< Tháng trước" / "Tháng sau >"
    │
    ▼
refreshSalaryData()                       ← dòng 466-468
    │
    ▼
dbHelper.salaryDAO.getAllSalariesWithEmployee(month, year)
    ← SalaryDAO.kt dòng 121-132
    │  Lấy tất cả employees → gọi calculateAndSaveSalary() cho từng NV
    │  → Trả về List<Pair<Salary, Employee>>
    │
    ▼
Hiển thị danh sách Card:
    │  - Mã NV + Tên
    │  - Trạng thái: "ĐÃ THANH TOÁN" (xanh) / "CHƯA THANH TOÁN" (đỏ)
    │  - Số giờ, OT, đơn giá
    │  - Tổng lương (VNĐ)
    │
    ▼
Nhấn "Đánh dấu đã trả" / "Bỏ thanh toán"    ← dòng 573-587
    │
    ▼
dbHelper.salaryDAO.updateSalaryStatus(empId, month, year, newStatus)
    ← SalaryDAO.kt dòng 134-146
    UPDATE tbl_salary SET status=? WHERE employee_id=? AND month=? AND year=?
```

#### 2.3. Tab Lịch Sử Chấm Công (`AttendanceHistoryTab`)

```
AdminDashboard → Tab "Chấm Công"         ← AdminDashboard.kt dòng 600-657
    │
    ▼
dbHelper.workLogDAO.getAllWorkLogsWithEmployee()
    ← WorkLogDAO.kt dòng 150-179
    │
    │  BƯỚC 1: dbHelper.employeeDAO.getAllEmployees()
    │           → tạo Map<empId, Employee>              ← dòng 152
    │
    │  BƯỚC 2: SELECT * FROM tbl_work_log
    │           ORDER BY work_date DESC, id DESC        ← dòng 154-158
    │
    │  BƯỚC 3: Ghép mỗi WorkLog với Employee tương ứng
    │           → List<Pair<WorkLog, Employee>>
    │
    ▼
Hiển thị danh sách Card:
    - Mã NV + Tên + Ngày
    - Check-In / Check-Out / Tổng giờ
```

#### 2.4. EmployeeDAO — Danh sách hàm

| Hàm | Dòng | SQL chính | Gọi DAO khác |
|-----|------|-----------|--------------|
| `getAllEmployees()` | 9-30 | `SELECT * FROM tbl_employee ORDER BY code` | — |
| `searchEmployees()` | 32-55 | `SELECT ... WHERE code LIKE ? OR fullname LIKE ?` | — |
| `getEmployeeById()` | 57-80 | `SELECT ... WHERE id=?` | — |
| `getEmployeeByAccountId()` | 82-105 | `SELECT ... WHERE account_id=?` | — |
| `addEmployee()` | 107-142 | `INSERT INTO tbl_employee` (transaction) | `accountDAO.insertAccount()` |
| `updateEmployee()` | 144-178 | `UPDATE tbl_employee` (transaction) | `accountDAO.updatePassword()` |
| `deleteEmployee()` | 180-187 | `DELETE FROM tbl_employee` | `accountDAO.deleteAccount()` |

---

## THÀNH VIÊN 3 — Cổng Nhân Viên (EmployeeDashboard)

### Phạm vi: 3 file

| File | Dòng | Vai trò |
|------|------|---------|
| `WorkLogDAO.kt` | 181 | CRUD chấm công + tính giờ |
| `SalaryDAO.kt` | 148 | Tính lương tự động + CRUD |
| `EmployeeDashboard.kt` | 461 | UI nhân viên 3 tab |

### Luồng xử lý chi tiết

#### 3.1. Tab Chấm Công — Check-In / Check-Out (`CheckInCheckOutTab`)

```
EmployeeDashboard → Tab "Chấm Công"      ← EmployeeDashboard.kt dòng 120-232
    │
    ▼
refreshTodayLog()                          ← dòng 129-132
    │  todayDateStr = "2026-10-24" (format yyyy-MM-dd)
    │
    ▼
dbHelper.workLogDAO.getTodayWorkLog(employee.id, todayDateStr)
    ← WorkLogDAO.kt dòng 13-36
    │  SELECT * FROM tbl_work_log
    │  WHERE employee_id=? AND work_date=?
    │
    ├── null → Hiển thị "Chưa Check-In" (xám)
    ├── status="CHECKED_IN" → Hiển thị "Đang làm việc" (cam) + giờ In
    └── status="COMPLETED" → Hiển thị "Hoàn thành ca làm" (xanh) + giờ In/Out + tổng giờ
```

##### Check-In:

```
Nhấn nút "Check-In"                       ← EmployeeDashboard.kt dòng 198-212
    │  currentTimeStr = "14:30:25" (format HH:mm:ss)
    │
    ▼
dbHelper.workLogDAO.checkIn(employee.id, todayDateStr, currentTimeStr)
    ← WorkLogDAO.kt dòng 38-57
    │
    │  BƯỚC 1: getTodayWorkLog(employeeId, workDate)      ← dòng 39
    │  │
    │  ├── Đã tồn tại → return (false, "Bạn đã Check-In trong ngày hôm nay rồi!")
    │  │
    │  └── Chưa tồn tại → tiếp tục
    │
    │  BƯỚC 2: INSERT INTO tbl_work_log                   ← dòng 44-51
    │           (employee_id, work_date, check_in_time, status)
    │           VALUES (?, '2026-10-24', '14:30:25', 'CHECKED_IN')
    │
    ├── thành công → (true, "Check-In thành công lúc 14:30:25!")
    └── thất bại   → (false, "Lỗi khi lưu dữ liệu Check-In!")
```

##### Check-Out:

```
Nhấn nút "Check-Out"                      ← EmployeeDashboard.kt dòng 214-230
    │  currentTimeStr = "18:00:15"
    │
    ▼
dbHelper.workLogDAO.checkOut(employee.id, todayDateStr, currentTimeStr)
    ← WorkLogDAO.kt dòng 59-90
    │
    │  BƯỚC 1: getTodayWorkLog(employeeId, workDate)      ← dòng 60
    │  │
    │  ├── null → return (false, "Bạn chưa Check-In hôm nay!")
    │  ├── status="COMPLETED" → return (false, "Bạn đã hoàn thành Check-Out rồi!")
    │  └── status="CHECKED_IN" → tiếp tục
    │
    │  BƯỚC 2: calculateHoursBetween("14:30:25", "18:00:15")
    │           ← WorkLogDAO.kt dòng 92-118
    │           │  Parse bằng SimpleDateFormat("HH:mm:ss")
    │           │  diffMs = dateOut.time - dateIn.time
    │           │  hours = diffMs / (1000 * 60 * 60)
    │           │  = (18:00:15 - 14:30:25) = 3.50 giờ (làm tròn 2 chữ số)
    │           │
    │           └── Nếu format HH:mm:ss thất bại → thử lại với HH:mm
    │
    │  BƯỚC 3: Nếu calculatedHours <= 0
    │           → return (false, "Thời gian Check-Out phải muộn hơn Check-In!")
    │
    │  BƯỚC 4: UPDATE tbl_work_log                        ← dòng 73-83
    │           SET check_out_time='18:00:15',
    │               total_hours=3.50,
    │               status='COMPLETED'
    │           WHERE id=?
    │
    ├── thành công → (true, "Check-Out thành công! Tổng giờ làm hôm nay: 3.50 giờ.")
    └── thất bại   → (false, "Lỗi khi cập nhật Check-Out!")
```

#### 3.2. Tab Lịch Sử Chấm Công (`EmployeeAttendanceHistoryTab`)

```
EmployeeDashboard → Tab "Lịch Sử"        ← EmployeeDashboard.kt dòng 237-309
    │
    ▼
dbHelper.workLogDAO.getWorkLogsByEmployee(employee.id)
    ← WorkLogDAO.kt dòng 120-148
    │  SELECT * FROM tbl_work_log
    │  WHERE employee_id=?
    │  ORDER BY work_date DESC, id DESC
    │
    ▼
Tính tổng giờ tích lũy:                   ← EmployeeDashboard.kt dòng 245
    │  totalHoursSum = logsList
    │      .filter { status == "COMPLETED" }
    │      .sumOf { totalHours }
    │
    ▼
Hiển thị:
    - "Tổng số giờ tích lũy: XX.X giờ"
    - Danh sách Card: Ngày | Trạng thái | In/Out | Tổng giờ
```

#### 3.3. Tab Phiếu Lương (`EmployeeSalaryTab`)

```
EmployeeDashboard → Tab "Phiếu Lương"    ← EmployeeDashboard.kt dòng 314-447
    │
    │  selectedMonth, selectedYear (mặc định = hiện tại)
    │  Nút "< Tháng trước" / "Tháng sau >"
    │
    ▼
loadSalary()                               ← dòng 322-324
    │
    ▼
dbHelper.salaryDAO.calculateAndSaveSalary(employee.id, month, year)
    ← SalaryDAO.kt dòng 11-88
    │
    │  ╔══════════════════════════════════════════════════════╗
    │  ║         LOGIC TÍNH LƯƠNG (Chi tiết)                 ║
    │  ╠══════════════════════════════════════════════════════╣
    │  ║                                                      ║
    │  ║  BƯỚC 1: Lấy thông tin nhân viên                    ║
    │  ║    emp = employeeDAO.getEmployeeById(id)  ← dòng 12 ║
    │  ║    → lấy hourlyRate (VD: 50.000 đ/h)                ║
    │  ║                                                      ║
    │  ║  BƯỚC 2: Query work_log tháng đó         ← dòng 18  ║
    │  ║    SELECT * FROM tbl_work_log                        ║
    │  ║    WHERE employee_id=?                               ║
    │  ║      AND status='COMPLETED'                          ║
    │  ║      AND work_date LIKE '2026-10-%'                  ║
    │  ║                                                      ║
    │  ║  BƯỚC 3: Tính tổng giờ + OT              ← dòng 29  ║
    │  ║    Duyệt từng ngày:                                  ║
    │  ║      totalMonthHours += dailyHours                   ║
    │  ║      if (dailyHours > 8.0):                          ║
    │  ║        totalOvertimeHours += (dailyHours - 8.0)      ║
    │  ║                                                      ║
    │  ║    VÍ DỤ: NV làm 3 ngày (9h, 10h, 7h)              ║
    │  ║      totalMonthHours = 26.0                          ║
    │  ║      totalOvertimeHours = (9-8)+(10-8) = 3.0        ║
    │  ║      regularHours = 26.0 - 3.0 = 23.0               ║
    │  ║                                                      ║
    │  ║  BƯỚC 4: Tính lương                       ← dòng 44 ║
    │  ║    salary = (regularHours × hourlyRate)              ║
    │  ║           + (overtimeHours × hourlyRate × 1.5)       ║
    │  ║                                                      ║
    │  ║    VÍ DỤ: rate = 50.000đ/h                          ║
    │  ║      = (23.0 × 50.000) + (3.0 × 50.000 × 1.5)      ║
    │  ║      = 1.150.000 + 225.000                           ║
    │  ║      = 1.375.000 VNĐ                                ║
    │  ║                                                      ║
    │  ║  BƯỚC 5: Lưu/Cập nhật DB                 ← dòng 47  ║
    │  ║    Kiểm tra tbl_salary đã có record (emp,m,y)?      ║
    │  ║    ├── CÓ  → UPDATE (giữ nguyên status cũ)          ║
    │  ║    └── CHƯA → INSERT (status = 'UNPAID')             ║
    │  ║                                                      ║
    │  ╚══════════════════════════════════════════════════════╝
    │
    ▼
Hiển thị Phiếu Lương:                     ← EmployeeDashboard.kt dòng 385-444
    │  ┌─────────────────────────────────┐
    │  │  PHIẾU LƯƠNG THÁNG 10/2026     │
    │  ├─────────────────────────────────┤
    │  │  Nhân viên:    Nguyễn Văn A     │
    │  │  Lương cơ bản: 50.000 đ/h       │
    │  │  Tổng giờ:     26.0 giờ         │
    │  │  Giờ OT:       3.0 giờ          │
    │  ├─────────────────────────────────┤
    │  │  Lương chuẩn:  1.150.000 VNĐ    │
    │  │  Thưởng OT:    225.000 VNĐ      │
    │  ├─────────────────────────────────┤
    │  │  THỰC LĨNH:   1.375.000 VNĐ    │
    │  │  Trạng thái:  CHƯA THANH TOÁN  │
    │  └─────────────────────────────────┘
```

#### 3.4. WorkLogDAO — Danh sách hàm

| Hàm | Dòng | SQL chính | Ghi chú |
|-----|------|-----------|---------|
| `getTodayWorkLog()` | 13-36 | `SELECT ... WHERE emp_id=? AND work_date=?` | Kiểm tra đã chấm công chưa |
| `checkIn()` | 38-57 | `INSERT INTO tbl_work_log` | Validate: chưa check-in hôm nay |
| `checkOut()` | 59-90 | `UPDATE ... SET check_out, total_hours, status` | Validate: đã check-in, chưa completed |
| `calculateHoursBetween()` | 92-118 | — (helper) | Parse HH:mm:ss hoặc HH:mm, tính diff |
| `getWorkLogsByEmployee()` | 120-148 | `SELECT ... WHERE emp_id=? ORDER BY date DESC` | Lịch sử cá nhân |
| `getAllWorkLogsWithEmployee()` | 150-179 | `SELECT * ... ORDER BY date DESC` + JOIN Employee | Cho Admin xem toàn công ty |

#### 3.5. SalaryDAO — Danh sách hàm

| Hàm | Dòng | SQL chính | Ghi chú |
|-----|------|-----------|---------|
| `calculateAndSaveSalary()` | 11-88 | SELECT work_log + INSERT/UPDATE salary | Logic OT > 8h × 1.5 |
| `getSalariesByEmployee()` | 90-119 | `SELECT ... WHERE emp_id=? ORDER BY year,month DESC` | Lịch sử lương cá nhân |
| `getAllSalariesWithEmployee()` | 121-132 | Gọi `calculateAndSaveSalary()` cho mọi NV | Cho Admin xem bảng lương |
| `updateSalaryStatus()` | 134-146 | `UPDATE ... SET status=?` | Đánh dấu PAID/UNPAID |

---

## Sơ Đồ Tổng Thể — Luồng Dữ Liệu Giữa 3 Thành Viên

```
┌─────────────────────────────────────────────────────────────────────┐
│                        THÀNH VIÊN 1                                │
│  MainActivity → LoginScreen → AccountDAO → tbl_account             │
│       │              │                         ▲                    │
│       │              │ onLoginSuccess           │ insert/update/    │
│       │              ▼                          │ delete            │
│       │         SessionManager                  │                    │
│       │         (SharedPreferences)             │                    │
├───────┼─────────────────────────────────────────┼───────────────────┤
│       │         THÀNH VIÊN 2                    │                    │
│       ├──► AdminDashboard ──► EmployeeDAO ──────┘                   │
│       │     │ Tab NV: CRUD                 ▲                        │
│       │     │ Tab Lương: xem + trả lương   │ getAllEmployees()      │
│       │     │ Tab Chấm công: xem lịch sử   │                       │
│       │     │         │          │          │                       │
│       │     │         ▼          ▼          │                       │
├───────┼─────┼───────────────────────────────┼───────────────────────┤
│       │     │  THÀNH VIÊN 3                 │                       │
│       └──► EmployeeDashboard                │                       │
│              │ Tab Chấm công ──► WorkLogDAO─┘                       │
│              │                   tbl_work_log                       │
│              │ Tab Lịch sử  ──► WorkLogDAO                          │
│              │ Tab Phiếu lương──► SalaryDAO                         │
│                                  tbl_salary                         │
└─────────────────────────────────────────────────────────────────────┘
```

### Dependency giữa các DAO (quan trọng khi phối hợp)

```
AccountDAO          ◄──── EmployeeDAO gọi khi add/update/delete NV
    │                         (TV2 phụ thuộc TV1)
    │
    └── Không phụ thuộc DAO nào khác

EmployeeDAO         ◄──── WorkLogDAO.getAllWorkLogsWithEmployee() gọi getAllEmployees()
    │                         (TV3 phụ thuộc TV2)
    │               ◄──── SalaryDAO.calculateAndSaveSalary() gọi getEmployeeById()
    │                         (TV3 phụ thuộc TV2)
    │               ◄──── SalaryDAO.getAllSalariesWithEmployee() gọi getAllEmployees()
    │                         (TV3 phụ thuộc TV2)
    │
    └── Phụ thuộc AccountDAO (TV1)

WorkLogDAO          ◄──── SalaryDAO đọc tbl_work_log qua SQL trực tiếp
    │                         (trong cùng TV3, không gọi DAO)
    │
    └── Phụ thuộc EmployeeDAO (TV2)

SalaryDAO
    └── Phụ thuộc EmployeeDAO (TV2) + đọc tbl_work_log
```
