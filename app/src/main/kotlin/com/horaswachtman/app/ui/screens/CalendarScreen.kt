package com.horaswachtman.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horaswachtman.app.data.AppDatabase
import com.horaswachtman.app.ui.viewmodel.CalendarViewModel
import com.horaswachtman.app.ui.viewmodel.CalendarViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    database: AppDatabase,
    viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(database.dayWorkDao())
    )
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAnnualStats by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val allDays by viewModel.allDays.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Horas Wachtman ⏰",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Navigation
            item {
                MonthNavigator(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onShowStats = { showAnnualStats = true }
                )
            }

            // Calendar Grid
            item {
                CalendarGrid(
                    yearMonth = currentMonth,
                    allDays = allDays,
                    onDateClick = {
                        selectedDate = it
                        showDialog = true
                    }
                )
            }

            // Month Summary
            item {
                MonthlySummary(
                    yearMonth = currentMonth,
                    allDays = allDays
                )
            }
        }
    }

    // Dialog for entering hours
    if (showDialog && selectedDate != null) {
        HoursDialog(
            date = selectedDate!!,
            allDays = allDays,
            onSave = { date, hours ->
                viewModel.saveHours(date, hours)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    // Annual Stats
    if (showAnnualStats) {
        AnnualStatsDialog(
            allDays = allDays,
            onDismiss = { showAnnualStats = false }
        )
    }
}

@Composable
fun MonthNavigator(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowStats: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE8DFF5),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Mes anterior",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))} ${currentMonth.year}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Próximo mes",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Button(
            onClick = onShowStats,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text(
                text = "📈 Estadísticas Anuales",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    allDays: List<com.horaswachtman.app.data.entity.DayWork>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth().dayOfMonth
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach {
                Text(
                    text = it,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF6200EE)
                )
            }
        }

        // Calendar days
        val days = mutableListOf<LocalDate?>()
        repeat(firstDayOfWeek) { days.add(null) }
        repeat(lastDay) { day ->
            days.add(yearMonth.atDay(day + 1))
        }

        val weeks = days.chunked(7)
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    if (date != null) {
                        val dayWork = allDays.find { it.date == date }
                        val backgroundColor = if (dayWork != null && dayWork.hoursWorked > 0) {
                            when {
                                dayWork.hoursWorked >= 8 -> Color(0xFF00C853)
                                dayWork.hoursWorked >= 4 -> Color(0xFFFFD600)
                                else -> Color(0xFFFF6D00)
                            }
                        } else {
                            Color(0xFFF5F5F5)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    color = backgroundColor,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .clickable { onDateClick(date) }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dayWork != null && dayWork.hoursWorked > 0) Color.White else Color.Black
                            )
                            if (dayWork != null && dayWork.hoursWorked > 0) {
                                Text(
                                    text = "${dayWork.hoursWorked}h",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummary(
    yearMonth: YearMonth,
    allDays: List<com.horaswachtman.app.data.entity.DayWork>
) {
    val monthTotal = allDays
        .filter { it.date.year == yearMonth.year && it.date.monthValue == yearMonth.monthValue }
        .sumOf { it.hoursWorked }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF03DAC5)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total del Mes",
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = "$monthTotal horas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun HoursDialog(
    date: LocalDate,
    allDays: List<com.horaswachtman.app.data.entity.DayWork>,
    onSave: (LocalDate, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var hoursInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val existingDay = allDays.find { it.date == date }
    if (hoursInput.isEmpty() && existingDay != null) {
        hoursInput = existingDay.hoursWorked.toString()
        notesInput = existingDay.notes
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar horas - ${date.dayOfMonth}/${date.monthValue}/${date.year}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = hoursInput,
                    onValueChange = { hoursInput = it },
                    label = { Text("Horas trabajadas") },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("h") }
                )
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hours = hoursInput.toDoubleOrNull() ?: 0.0
                    onSave(date, hours)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AnnualStatsDialog(
    allDays: List<com.horaswachtman.app.data.entity.DayWork>,
    onDismiss: () -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val monthlyTotals = mutableMapOf<Int, Double>()

    repeat(12) { month ->
        val total = allDays
            .filter { it.date.year == currentYear && it.date.monthValue == month + 1 }
            .sumOf { it.hoursWorked }
        monthlyTotals[month + 1] = total
    }

    val annualTotal = monthlyTotals.values.sum()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📈 Estadísticas Anuales $currentYear",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Anual", color = Color.White, fontSize = 12.sp)
                        Text(
                            "$annualTotal horas",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyColumn {
                    items(12) { month ->
                        val monthName = java.time.Month.of(month + 1)
                            .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es", "ES"))
                        val total = monthlyTotals[month + 1] ?: 0.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (total > 0) Color(0xFFE8DFF5) else Color(0xFFF5F5F5),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$total h",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}
