package com.example.mobileappth2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mobileappth2.dal.DBHelper
import com.example.mobileappth2.dal.SessionManager
import com.example.mobileappth2.models.Employee
import com.example.mobileappth2.activities.AdminDashboard
import com.example.mobileappth2.activities.EmployeeDashboard
import com.example.mobileappth2.activities.LoginScreen


sealed class AppScreen {
    object Login : AppScreen()
    data class Admin(val username: String) : AppScreen()
    data class EmployeePortal(val employee: Employee) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DBHelper(this)
        sessionManager = SessionManager(this)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainAppNavigation(dbHelper, sessionManager)
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    dbHelper: DBHelper,
    sessionManager: SessionManager
) {
    val initialScreen = remember {
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getUserRole()
            val username = sessionManager.getUsername()
            if (role == "ADMIN") {
                AppScreen.Admin(username)
            } else if (role == "EMPLOYEE") {
                val empId = sessionManager.getEmployeeId()
                val emp = dbHelper.employeeDAO.getEmployeeById(empId)
                if (emp != null) {
                    AppScreen.EmployeePortal(emp)
                } else {
                    sessionManager.clearSession()
                    AppScreen.Login
                }
            } else {
                AppScreen.Login
            }
        } else {
            AppScreen.Login
        }
    }

    var currentScreen by remember { mutableStateOf<AppScreen>(initialScreen) }

    when (val screen = currentScreen) {
        is AppScreen.Login -> {
            LoginScreen(
                dbHelper = dbHelper,
                onLoginSuccess = { account, employee ->
                    if (account.role == "ADMIN") {
                        sessionManager.saveSession(
                            userId = account.id,
                            username = account.username,
                            role = account.role
                        )
                        currentScreen = AppScreen.Admin(account.username)
                    } else if (account.role == "EMPLOYEE" && employee != null) {
                        sessionManager.saveSession(
                            userId = account.id,
                            username = account.username,
                            role = account.role,
                            employeeId = employee.id
                        )
                        currentScreen = AppScreen.EmployeePortal(employee)
                    }
                }
            )
        }
        is AppScreen.Admin -> {
            AdminDashboard(
                dbHelper = dbHelper,
                adminUsername = screen.username,
                onLogout = {
                    sessionManager.clearSession()
                    currentScreen = AppScreen.Login
                }
            )
        }
        is AppScreen.EmployeePortal -> {
            EmployeeDashboard(
                dbHelper = dbHelper,
                employee = screen.employee,
                onLogout = {
                    sessionManager.clearSession()
                    currentScreen = AppScreen.Login
                }
            )
        }
    }
}
