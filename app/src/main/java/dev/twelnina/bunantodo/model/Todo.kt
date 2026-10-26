package dev.twelnina.bunantodo.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Todo(
    val id: Int = 0,
    val title: String,
    val detail: String,
    val plannedDate: LocalDate?,
    val tag: TodoTag?,
    val isCompleted: Boolean = false
) {
    fun calculateOverdueDays(today: LocalDate): Long? {
        val date = plannedDate ?: return null

        if (isCompleted || !date.isBefore(today)) {
            return null
        }

        return ChronoUnit.DAYS.between(date, today)
    }
}