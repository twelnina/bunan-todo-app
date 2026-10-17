package dev.twelnina.bunantodo.ui.list

import dev.twelnina.bunantodo.data.local.TodoEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoGroupingTest {

    @Test
    fun `groups todos chronologically with no date last`() {
        val september15 = LocalDate.of(2026, 9, 15)
        val september20 = LocalDate.of(2026, 9, 20)

        val firstTodo = createTodo(1, september15)
        val secondTodo = createTodo(2, september15)
        val laterTodo = createTodo(3, september20)
        val noDateTodo = createTodo(4, null)

        val result = groupTodosByDate(
            todos = listOf(noDateTodo, laterTodo, firstTodo, secondTodo)
        )

        assertEquals(
            listOf(
                TodoDateGroup(september15, listOf(firstTodo, secondTodo)),
                TodoDateGroup(september20, listOf(laterTodo)),
                TodoDateGroup(null, listOf(noDateTodo))
            ),
            result
        )
    }

    @Test
    fun `preserve todo order within a date group`() {
        val date = LocalDate.of(2026, 9, 20)

        val firstTodo = createTodo(2, date)
        val secondTodo = createTodo(1, date)

        val result = groupTodosByDate(listOf(firstTodo, secondTodo))

        assertEquals(listOf(firstTodo, secondTodo), result.single().todos)
    }

    @Test
    fun `returns empty groups when todos are empty`() {
        val result = groupTodosByDate(emptyList())

        assertEquals(emptyList<TodoDateGroup>(), result)
    }


    private fun createTodo(id: Int, plannedDate: LocalDate?): TodoEntity {
        return TodoEntity(
            id = id,
            title = "Todo $id",
            detail = "Description $id",
            plannedDate = plannedDate,
            tag = null
        )
    }
}
