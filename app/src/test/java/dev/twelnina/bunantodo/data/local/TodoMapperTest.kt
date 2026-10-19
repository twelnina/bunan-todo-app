package dev.twelnina.bunantodo.data.local

import dev.twelnina.bunantodo.model.Todo
import dev.twelnina.bunantodo.model.TodoTag
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoMapperTest {
    @Test
    fun toTodo_preservesAllFields() {
        val entity = TodoEntity(
            id = 42,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = LocalDate.of(2026, 10, 19),
            tag = TodoTag.SHOPPING,
            isCompleted = true
        )
        val expected = Todo(
            id = 42,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = LocalDate.of(2026, 10, 19),
            tag = TodoTag.SHOPPING,
            isCompleted = true
        )

        assertEquals(expected, entity.toTodo())
    }

    @Test
    fun toEntity_preservesAllFields() {
        val todo = Todo(
            id = 42,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = LocalDate.of(2026, 10, 19),
            tag = TodoTag.SHOPPING,
            isCompleted = true
        )
        val expected = TodoEntity(
            id = 42,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = LocalDate.of(2026, 10, 19),
            tag = TodoTag.SHOPPING,
            isCompleted = true
        )

        assertEquals(expected, todo.toEntity())
    }

    @Test
    fun toTodo_preservesNullFieldsAndDefaults() {
        val entity = TodoEntity(
            title = "Unscheduled todo",
            detail = "",
            plannedDate = null,
            tag = null
        )
        val expected = Todo(
            title = "Unscheduled todo",
            detail = "",
            plannedDate = null,
            tag = null
        )

        assertEquals(expected, entity.toTodo())
    }

    @Test
    fun toEntity_preservesNullFieldsAndDefaults() {
        val todo = Todo(
            title = "Unscheduled todo",
            detail = "",
            plannedDate = null,
            tag = null
        )
        val expected = TodoEntity(
            title = "Unscheduled todo",
            detail = "",
            plannedDate = null,
            tag = null
        )

        assertEquals(expected, todo.toEntity())
    }
}
