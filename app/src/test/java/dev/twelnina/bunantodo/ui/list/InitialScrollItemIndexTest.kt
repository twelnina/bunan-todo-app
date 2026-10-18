package dev.twelnina.bunantodo.ui.list

import dev.twelnina.bunantodo.model.Todo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class InitialScrollItemIndexTest {

    private val today = LocalDate.of(2026, 9, 27)

    @Test
    fun `returns today header index when today group exists`() {
        val groups = listOf(
            createGroup(today.minusDays(1), todoCount = 2, firstTodoId = 1),
            createGroup(today, todoCount = 1, firstTodoId = 3),
            createGroup(today.plusDays(1), todoCount = 1, firstTodoId = 4)
        )

        val result = initialScrollItemIndex(groups, today)

        assertEquals(3, result)
    }

    @Test
    fun `returns nearest future header index when today group is absent`() {
        val groups = listOf(
            createGroup(today.minusDays(1), todoCount = 1, firstTodoId = 1),
            createGroup(today.plusDays(2), todoCount = 1, firstTodoId = 2),
            createGroup(today.plusDays(5), todoCount = 1, firstTodoId = 3)
        )

        val result = initialScrollItemIndex(groups, today)

        assertEquals(2, result)
    }

    @Test
    fun `returns nearest past header index when today and future groups are absent`() {
        val groups = listOf(
            createGroup(today.minusDays(5), todoCount = 2, firstTodoId = 1),
            createGroup(today.minusDays(2), todoCount = 1, firstTodoId = 3)
        )

        val result = initialScrollItemIndex(groups, today)

        assertEquals(3, result)
    }

    @Test
    fun `ignores unscheduled group when selecting target date`() {
        val groups = listOf(
            createGroup(today.plusDays(3), todoCount = 1, firstTodoId = 1),
            createGroup(null, todoCount = 2, firstTodoId = 2)
        )

        val result = initialScrollItemIndex(groups, today)

        assertEquals(0, result)
    }

    @Test
    fun `returns null when only unscheduled group exists`() {
        val groups = listOf(
            createGroup(null, todoCount = 1, firstTodoId = 1)
        )

        val result = initialScrollItemIndex(groups, today)

        assertNull(result)
    }

    @Test
    fun `returns null when list is empty`() {
        val result = initialScrollItemIndex(emptyList(), today)

        assertNull(result)
    }

    private fun createGroup(
        date: LocalDate?,
        todoCount: Int,
        firstTodoId: Int
    ): TodoDateGroup {
        val todos = List(todoCount) { index ->
            val id = firstTodoId + index
            Todo(
                id = id,
                title = "Todo $id",
                detail = "Description $id",
                plannedDate = date,
                tag = null
            )
        }

        return TodoDateGroup(date = date, todos = todos)
    }
}
