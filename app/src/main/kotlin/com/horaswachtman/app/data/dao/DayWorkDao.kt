package com.horaswachtman.app.data.dao

import androidx.room.*
import com.horaswachtman.app.data.entity.DayWork
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayWorkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dayWork: DayWork)

    @Query("SELECT * FROM day_work WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DayWork?

    @Query("SELECT * FROM day_work ORDER BY date DESC")
    fun getAllDaysFlow(): Flow<List<DayWork>>

    @Query("SELECT * FROM day_work WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getDaysBetween(startDate: LocalDate, endDate: LocalDate): List<DayWork>

    @Update
    suspend fun update(dayWork: DayWork)

    @Delete
    suspend fun delete(dayWork: DayWork)

    @Query("SELECT SUM(hoursWorked) FROM day_work WHERE strftime('%Y-%m', date) = :yearMonth")
    suspend fun getMonthTotal(yearMonth: String): Double

    @Query("SELECT SUM(hoursWorked) FROM day_work WHERE strftime('%Y', date) = :year")
    suspend fun getYearTotal(year: String): Double
}
