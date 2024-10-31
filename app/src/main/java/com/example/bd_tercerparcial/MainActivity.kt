package com.example.bd_tercerparcial

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import androidx.navigation.compose.rememberNavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseOpenHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DatabaseOpenHelper(this)

        setContent {

            val navController = rememberNavController()
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen {
                    showSplash = false
                }
            } else {
                NavHost(navController = navController, startDestination = "PersonalData") {
                    composable("PersonalData?editingAppointmentId={editingAppointmentId}") { backStackEntry ->
                        PersonalData(
                            navController = navController,
                            editingAppointmentId = backStackEntry.arguments?.getString("editingAppointmentId")?.toIntOrNull(),
                        )
                    }
                    composable("Appointment/{name}/{number}?editingAppointmentId={editingAppointmentId}") { backStackEntry ->
                        Appointment(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            number = backStackEntry.arguments?.getString("number") ?: "",
                            editingAppointmentId = backStackEntry.arguments?.getString("editingAppointmentId")?.toIntOrNull(),
                            dbHelper = dbHelper
                        )
                    }
                    composable("AppointmentsList") {
                        AppointmentsList(navController, dbHelper)
                    }
                }
            }
        }
    }

    @Composable
    fun SplashScreen(onTimeout: () -> Unit) {
        LaunchedEffect(true) {
            delay(3000)
            onTimeout()
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )
        }
    }

    @Composable
    fun PersonalData(navController: NavController, editingAppointmentId: Int? = null) {
        var name by remember { mutableStateOf("") }
        var number by remember { mutableStateOf("") }

        LaunchedEffect(editingAppointmentId) {
            if (editingAppointmentId != null) {
                val appointment = dbHelper.getAppointmentById(editingAppointmentId)
                name = appointment["name"] ?: ""
                number = appointment["phone"] ?: ""
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (editingAppointmentId == null) "Name" else "Edit name") })
                OutlinedTextField(value = number, onValueChange = {
                    if (it.length <= 10) {
                        number = it
                    }
                }, label = { Text(if (editingAppointmentId == null) "Phone number" else "Edit phone number") })

                Spacer(modifier = Modifier.padding(8.dp))

                Button(onClick = {
                    if (name.isNotEmpty() && number.length == 10) {
                        val route = buildString {
                            append("Appointment/$name/$number")
                            if (editingAppointmentId != null) {
                                append("?editingAppointmentId=$editingAppointmentId")
                            }
                        }
                        navController.navigate(route)
                    }
                }) {
                    Text(text = "Continue")
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Button(onClick = {
                    navController.navigate("AppointmentsList")
                }) {
                    Text(text = "Appointments list")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Appointment(navController: NavController, name: String, number: String, editingAppointmentId: Int? = null, dbHelper: DatabaseOpenHelper) {
        var initialDateMillis by remember { mutableStateOf<Long?>(null) }
        var initialHour by remember { mutableIntStateOf(0) }
        var initialMinute by remember { mutableIntStateOf(0) }

        LaunchedEffect(editingAppointmentId) {
            if (editingAppointmentId != null) {
                val appointment = dbHelper.getAppointmentById(editingAppointmentId)
                val dateString = appointment["date"] ?: ""
                val timeString = appointment["time"] ?: ""

                if (dateString.isNotEmpty()) {
                    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
                    val date = LocalDate.parse(dateString, dateFormatter)
                    initialDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }

                val timeParts = timeString.split(":")
                if (timeParts.size == 2) {
                    initialHour = timeParts[0].toIntOrNull() ?: 0
                    initialMinute = timeParts[1].toIntOrNull() ?: 0
                }
            }
        }

        // Initialize DatePickerState and TimePickerState without wrapping them in remember
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
            initialDisplayMode = DisplayMode.Input
        )

        val timeState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute
        )


        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DatePicker(state = dateState)
                Spacer(modifier = Modifier.padding(8.dp))
                TimePicker(state = timeState)
                Spacer(modifier = Modifier.padding(8.dp))

                Button(onClick = {
                    val selectedDateMillis = dateState.selectedDateMillis
                    val date = selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    val formattedDate = date?.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) ?: ""
                    val time = "${timeState.hour}:${timeState.minute}"

                    if (formattedDate.isNotEmpty() && dbHelper.getByDateAndTime(formattedDate, time).isEmpty()) {
                        if (editingAppointmentId == null) {
                            dbHelper.insertAppointment(name, number, formattedDate, time)
                        } else {
                            dbHelper.updateAppointment(editingAppointmentId, name, number, formattedDate, time)
                        }
                        navController.navigate("AppointmentsList") {
                            popUpTo("AppointmentsList") { inclusive = true }
                        }
                    }
                }) {
                    Text(if (editingAppointmentId == null) "Save" else "Edit")
                }
            }
        }
    }

    @Composable
    fun AppointmentsList(navController: NavController, dbHelper: DatabaseOpenHelper) {
        var appointments by remember { mutableStateOf(dbHelper.getAllAppointments()) }

        LaunchedEffect(Unit) {
            appointments = dbHelper.getAllAppointments()
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                appointments.forEach { appointment ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Name: ${appointment["name"]}")
                        Text(text = "Phone: ${appointment["phone"]}")
                        Text(text = "Date: ${appointment["date"]}")
                        Text(text = "Time: ${appointment["time"]}")

                        Row {
                            Button(onClick = {
                                navController.navigate("PersonalData?editingAppointmentId=${appointment["id"]}")
                            }) {
                                Text(text = "Edit")
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                            Button(onClick = {
                                if (dbHelper.deleteAppointment(appointment["id"]!!.toInt())) {
                                    appointments = dbHelper.getAllAppointments()
                                    Toast.makeText(this@MainActivity, "Appointment deleted", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this@MainActivity, "Error deleting appointment", Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Text(text = "Delete")
                            }
                        }

                        Spacer(modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }

}
