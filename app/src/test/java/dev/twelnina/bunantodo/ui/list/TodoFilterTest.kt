package dev.twelnina.bunantodo.ui.list

import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.model.PlannedDateFilter
import dev.twelnina.bunantodo.model.TodoTag
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoFilterTest {
    @Test
    fun `query matches title ignoring case`() {
        val today = LocalDate.of(2026, 9, 12)

        val kotlinTodo = TodoEntity(
            id = 1,
            title = "Study Kotlin",
            detail = "Read the documentation",
            plannedDate = today,
            tag = TodoTag.STUDY
        )

        val shoppingTodo = TodoEntity(
            id = 2,
            title = "Buy milk",
            detail = "Go to the supermarket",
            plannedDate = today,
            tag = TodoTag.SHOPPING
        )

        val result = filterTodos(
            items = listOf(kotlinTodo, shoppingTodo),
            query = "KOTLIN",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.ALL,
            today = today
        )

        assertEquals(listOf(kotlinTodo), result)
    }

    @Test
    fun `query matches description ignoring case`() {
        val today = LocalDate.of(2026, 9, 12)

        val kotlinTodo = TodoEntity(
            id = 1,
            title = "Study Programming",
            detail = "Read the kotlin documentation",
            plannedDate = today,
            tag = TodoTag.STUDY
        )

        val shoppingTodo = TodoEntity(
            id = 2,
            title = "Buy milk",
            detail = "Go to the supermarket",
            plannedDate = today,
            tag = TodoTag.SHOPPING
        )

        val result = filterTodos(
            items = listOf(kotlinTodo, shoppingTodo),
            query = "DOCUMENTATION",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.ALL,
            today = today
        )

        assertEquals(listOf(kotlinTodo), result)
    }

    @Test
    fun `selected tag returns matching todos`() {
        val today = LocalDate.of(2026, 9, 12)

        val workTodo = TodoEntity(
            id = 1,
            title = "Write report",
            detail = "Prepare the monthly report",
            plannedDate = today,
            tag = TodoTag.WORK
        )

        val studyTodo = TodoEntity(
            id = 2,
            title = "Study Kotlin",
            detail = "Read the documentation",
            plannedDate = today,
            tag = TodoTag.STUDY
        )

        val noTagTodo = TodoEntity(
            id = 3,
            title = "Clean room",
            detail = "Organize the desk",
            plannedDate = today,
            tag = null
        )

        val result = filterTodos(
            items = listOf(workTodo, studyTodo, noTagTodo),
            query = "",
            tags = setOf(TodoTag.WORK),
            plannedDateFilter = PlannedDateFilter.ALL,
            today = today
        )

        assertEquals(listOf(workTodo), result)
    }

    @Test
    fun `multiple selected tags return todos matching any tag`() {
        val today = LocalDate.of(2026, 9, 13)

        val workTodo = TodoEntity(
            id = 1,
            title = "Write report",
            detail = "Prepare the monthly report",
            plannedDate = today,
            tag = TodoTag.WORK
        )

        val studyTodo = TodoEntity(
            id = 2,
            title = "Study Kotlin",
            detail = "Read the documentation",
            plannedDate = today,
            tag = TodoTag.STUDY
        )

        val shoppingTodo = TodoEntity(
            id = 3,
            title = "Buy milk",
            detail = "Go to the supermarket",
            plannedDate = today,
            tag = TodoTag.SHOPPING
        )

        val result = filterTodos(
            items = listOf(workTodo, studyTodo, shoppingTodo),
            query = "",
            tags = setOf(TodoTag.WORK, TodoTag.STUDY),
            plannedDateFilter = PlannedDateFilter.ALL,
            today = today
        )

        assertEquals(listOf(workTodo, studyTodo), result)
    }

    @Test
    fun `today filter returns only todos planned for today`() {
        val today = LocalDate.of(2026, 9, 9)

        val yesterdayTodo = TodoEntity(
            id = 1,
            title = "Yesterday task",
            detail = "This task was planned for yesterday",
            plannedDate = today.minusDays(1),
            tag = TodoTag.WORK
        )

        val todayTodo = TodoEntity(
            id = 2,
            title = "Today task",
            detail = "This task is planned for today",
            plannedDate = today,
            tag = TodoTag.STUDY
        )

        val tomorrowTodo = TodoEntity(
            id = 3,
            title = "Tomorrow task",
            detail = "This task is planned for tomorrow",
            plannedDate = today.plusDays(1),
            tag = TodoTag.SHOPPING
        )

        val noDateTodo = TodoEntity(
            id = 4,
            title = "Unscheduled task",
            detail = "This task has no planned date",
            plannedDate = null,
            tag = null
        )

        val result = filterTodos(
            items = listOf(
                yesterdayTodo,
                todayTodo,
                tomorrowTodo,
                noDateTodo
            ),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.TODAY,
            today = today
        )

        assertEquals(listOf(todayTodo), result)
    }

    @Test
    fun `tomorrow filter returns only todos planned for tomorrow`() {
        val today = LocalDate.of(2026, 9, 9)

        val todayTodo = TodoEntity(
            id = 1,
            title = "Today task",
            detail = "This task is planned for today",
            plannedDate = today,
            tag = TodoTag.WORK
        )

        val tomorrowTodo = TodoEntity(
            id = 2,
            title = "Tomorrow task",
            detail = "This task is planned for tomorrow",
            plannedDate = today.plusDays(1),
            tag = TodoTag.STUDY
        )

        val dayAfterTomorrowTodo = TodoEntity(
            id = 3,
            title = "Day after tomorrow task",
            detail = "This task is planned for in two days",
            plannedDate = today.plusDays(2),
            tag = TodoTag.SHOPPING
        )

        val noDateTodo = TodoEntity(
            id = 4,
            title = "Unscheduled task",
            detail = "This task has no planned date",
            plannedDate = null,
            tag = null
        )

        val result = filterTodos(
            items = listOf(
                todayTodo,
                tomorrowTodo,
                dayAfterTomorrowTodo,
                noDateTodo
            ),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.TOMORROW,
            today = today
        )

        assertEquals(listOf(tomorrowTodo), result)
    }

    @Test
    fun `this week filter returns todos from today through Sunday`() {
        // It's Wednesday
        val today = LocalDate.of(2026, 9, 9)

        val yesterdayTodo = TodoEntity(
            id = 1,
            title = "Yesterday task",
            detail = "This task was planned for yesterday",
            plannedDate = today.minusDays(1),
            tag = null
        )

        val todayTodo = TodoEntity(
            id = 2,
            title = "Today task",
            detail = "This task is planned for today",
            plannedDate = today,
            tag = null
        )

        val fridayTodo = TodoEntity(
            id = 3,
            title = "Friday task",
            detail = "This task is planned for on Friday",
            plannedDate = today.plusDays(2),
            tag = null
        )

        val sundayTodo = TodoEntity(
            id = 4,
            title = "Sunday task",
            detail = "This task is planned for on Sunday",
            plannedDate = today.plusDays(4),
            tag = null
        )

        val nextMondayTodo = TodoEntity(
            id = 5,
            title = "Next Monday task",
            detail = "This task is planned for next week",
            plannedDate = today.plusDays(5),
            tag = null
        )

        val noDateTodo = TodoEntity(
            id = 6,
            title = "Unscheduled task",
            detail = "This task has no planned date",
            plannedDate = null,
            tag = null
        )

        val result = filterTodos(
            items = listOf(
                yesterdayTodo,
                todayTodo,
                fridayTodo,
                sundayTodo,
                nextMondayTodo,
                noDateTodo
            ),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.THIS_WEEK,
            today = today
        )

        assertEquals(listOf(todayTodo, fridayTodo, sundayTodo), result)
    }

    @Test
    fun `past incomplete filter returns only todos before today`() {
        val today = LocalDate.of(2026, 9, 9)

        val lastWeekTodo = TodoEntity(
            id = 1,
            title = "Last week task",
            detail = "This task was planned for last week",
            plannedDate = today.minusDays(7),
            tag = null
        )

        val yesterdayTodo = TodoEntity(
            id = 2,
            title = "Yesterday task",
            detail = "This task was planned for yesterday",
            plannedDate = today.minusDays(1),
            tag = null
        )

        val todayTodo = TodoEntity(
            id = 3,
            title = "Today task",
            detail = "This task is planned for today",
            plannedDate = today,
            tag = null
        )

        val tomorrowTodo = TodoEntity(
            id = 4,
            title = "Tomorrow task",
            detail = "This task is planned for tomorrow",
            plannedDate = today.plusDays(1),
            tag = null
        )

        val noDateTodo = TodoEntity(
            id = 5,
            title = "Unscheduled task",
            detail = "This task has no planned date",
            plannedDate = null,
            tag = null
        )

        val result = filterTodos(
            items = listOf(
                lastWeekTodo,
                yesterdayTodo,
                todayTodo,
                tomorrowTodo,
                noDateTodo
            ),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.PAST_INCOMPLETE,
            today = today
        )

        assertEquals(listOf(lastWeekTodo, yesterdayTodo), result)
    }

    @Test
    fun `unscheduled filter returns only todos without planned date`() {
        val today = LocalDate.of(2026, 9, 9)

        val pastIncompleteTodo = TodoEntity(
            id = 1,
            title = "Earlier task",
            detail = "This task was planned for yesterday",
            plannedDate = today.minusDays(1),
            tag = null
        )

        val todayTodo = TodoEntity(
            id = 2,
            title = "Today task",
            detail = "This task is planned for today",
            plannedDate = today,
            tag = null
        )

        val futureTodo = TodoEntity(
            id = 3,
            title = "Future task",
            detail = "This task is planned for next week",
            plannedDate = today.plusDays(7),
            tag = null
        )

        val noDateTodo = TodoEntity(
            id = 4,
            title = "Unscheduled task",
            detail = "This task has no planned date",
            plannedDate = null,
            tag = null
        )

        val result = filterTodos(
            items = listOf(
                pastIncompleteTodo,
                todayTodo,
                futureTodo,
                noDateTodo
            ),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.UNSCHEDULED,
            today = today
        )

        assertEquals(listOf(noDateTodo), result)
    }

    @Test
    fun `all date filter returns dated and undated todos`() {
        val today = LocalDate.of(2026, 9, 9)

        val pastIncompleteTodo = TodoEntity(
            id = 1,
            title = "Earlier task",
            detail = "This task was planned for yesterday",
            plannedDate = today.minusDays(1),
            tag = null
        )

        val todayTodo = TodoEntity(
            id = 2,
            title = "Today task",
            detail = "This task is planned for today",
            plannedDate = today,
            tag = null
        )

        val futureTodo = TodoEntity(
            id = 3,
            title = "Future task",
            detail = "This task is planned for next week",
            plannedDate = today.plusDays(7),
            tag = null
        )

        val noDateTodo = TodoEntity(
            id = 4,
            title = "Unscheduled task",
            detail = "This task has no planned date",
            plannedDate = null,
            tag = null
        )

        val result = filterTodos(
            items = listOf(
                pastIncompleteTodo,
                todayTodo,
                futureTodo,
                noDateTodo
            ),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.ALL,
            today = today
        )

        assertEquals(
            listOf(
                pastIncompleteTodo,
                todayTodo,
                futureTodo,
                noDateTodo
            ),
            result
        )
    }

    @Test
    fun `past incomplete filter excludes completed todos`() {
        val today = LocalDate.of(2026, 9, 23)

        val incompleteTodo = TodoEntity(
            id = 1,
            title = "Earlier task",
            detail = "",
            plannedDate = today.minusDays(1),
            tag = null,
            isCompleted = false
        )

        val completedTodo = incompleteTodo.copy(
            id = 2,
            isCompleted = true
        )

        val result = filterTodos(
            items = listOf(incompleteTodo, completedTodo),
            query = "",
            tags = emptySet(),
            plannedDateFilter = PlannedDateFilter.PAST_INCOMPLETE,
            today = today
        )

        assertEquals(listOf(incompleteTodo), result)
    }

    @Test
    fun `combined filters return todos matching every condition`() {
        val today = LocalDate.of(2026, 9, 9)

        val matchingTodo = TodoEntity(
            id = 1,
            title = "Write report",
            detail = "Prepare the monthly summary",
            plannedDate = today,
            tag = TodoTag.WORK
        )

        val queryMismatchTodo = TodoEntity(
            id = 2,
            title = "Prepare slides",
            detail = "Create the presentation",
            plannedDate = today,
            tag = TodoTag.WORK
        )

        val tagMismatchTodo = TodoEntity(
            id = 3,
            title = "Study report",
            detail = "Read the study materials",
            plannedDate = today,
            tag = TodoTag.STUDY
        )

        val dateMismatchTodo = TodoEntity(
            id = 4,
            title = "Future report",
            detail = "Prepare next month's summary",
            plannedDate = today.plusDays(1),
            tag = TodoTag.WORK
        )

        val result = filterTodos(
            items = listOf(
                matchingTodo,
                queryMismatchTodo,
                tagMismatchTodo,
                dateMismatchTodo
            ),
            query = "report",
            tags = setOf(TodoTag.WORK),
            plannedDateFilter = PlannedDateFilter.TODAY,
            today = today
        )

        assertEquals(listOf(matchingTodo), result)
    }
}
