package dev.twelnina.bunantodo.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TodoTest {
    private val today = LocalDate.of(2026, 10, 19)

    @Test
    fun calculateDaysSincePlannedDate_past_returnsElapsedDays() {
        val todo = createTodo(today.minusDays(3))

        assertEquals(3L, todo.calculateDaysSincePlannedDate(today))
    }

    @Test
    fun calculateDaysSincePlannedDate_today_returnsNull() {
        val todo = createTodo(today)

        assertNull(todo.calculateDaysSincePlannedDate(today))
    }

    @Test
    fun calculateDaysSincePlannedDate_future_returnsNull() {
        val todo = createTodo(today.plusDays(1))

        assertNull(todo.calculateDaysSincePlannedDate(today))
    }

    @Test
    fun calculateDaysSincePlannedDate_unscheduled_returnsNull() {
        val todo = createTodo(null)

        assertNull(todo.calculateDaysSincePlannedDate(today))
    }

    @Test
    fun calculateDaysSincePlannedDate_completedPast_returnsNull() {
        val todo = createTodo(today.minusDays(3), isCompleted = true)

        assertNull(todo.calculateDaysSincePlannedDate(today))
    }

    private fun createTodo(
        plannedDate: LocalDate?,
        isCompleted: Boolean = false
    ): Todo = Todo(
        title = "Test todo",
        detail = "",
        plannedDate = plannedDate,
        tag = null,
        isCompleted = isCompleted
    )
}
