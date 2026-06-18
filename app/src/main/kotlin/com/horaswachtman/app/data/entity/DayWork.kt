package com.horaswachtman.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "day_work")
data class DayWork(
    @PrimaryKey
    val date: LocalDate,
    val hoursWorked: Double = 0.0,
    val notes: String = ""
)
