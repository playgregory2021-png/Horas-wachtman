package com.horaswachtman.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.horaswachtman.app.data.dao.DayWorkDao
import com.horaswachtman.app.data.entity.DayWork
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(private val dayWorkDao: DayWorkDao) : ViewModel() {
    val allDays: StateFlow<List<DayWork>> = dayWorkDao.getAllDaysFlow()
        .let { flow ->
            val stateFlow = kotlinx.coroutines.flow.MutableStateFlow<List<DayWork>>(emptyList())
            viewModelScope.launch {
                flow.collect { days ->
                    stateFlow.value = days
                }
            }
            stateFlow.asStateFlow()
        }

    fun saveHours(date: LocalDate, hours: Double) {
        viewModelScope.launch {
            val existingDay = dayWorkDao.getByDate(date)
            val dayWork = if (existingDay != null) {
                existingDay.copy(hoursWorked = hours)
            } else {
                DayWork(date = date, hoursWorked = hours)
            }
            dayWorkDao.insert(dayWork)
        }
    }
}

class CalendarViewModelFactory(private val dayWorkDao: DayWorkDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarViewModel(dayWorkDao) as T
    }
}
