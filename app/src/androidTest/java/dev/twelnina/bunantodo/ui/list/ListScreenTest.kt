package dev.twelnina.bunantodo.ui.list

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.model.PlannedDateFilter
import dev.twelnina.bunantodo.model.TodoTag
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTodoFromUiState() {
        val today = LocalDate.of(2026, 9, 26)
        val todo = TodoEntity(
            id = 1,
            title = "Study Kotlin",
            detail = "Read the documentation",
            plannedDate = LocalDate.of(2026, 9, 13),
            tag = TodoTag.STUDY
        )

        val uiState = ListUiState(
            today = today,
            todoGroups = listOf(
                TodoDateGroup(
                    date = todo.plannedDate,
                    todos = listOf(todo)
                )
            )
        )

        setListScreenContent(uiStateProvider = { uiState })

        composeTestRule
            .onNodeWithText("Study Kotlin")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Sep 13")
            .assertIsDisplayed()
    }

    @Test
    fun enteringSearchQueryCallsOnQueryChange() {
        val query = mutableStateOf("")

        setListScreenContent(
            uiStateProvider = {
                ListUiState(searchQuery = query.value)
            },
            onQueryChange = { newQuery ->
                query.value = newQuery
            }
        )

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput("Kotlin")

        composeTestRule.runOnIdle {
            assertEquals("Kotlin", query.value)
        }
    }

    @Test
    fun tappingTagCallsOnTagSelected() {
        var selectedTag: TodoTag? = null

        setListScreenContent(
            onTagSelected = { tag ->
                selectedTag = tag
            }
        )

        composeTestRule
            .onNodeWithText("Work")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(TodoTag.WORK, selectedTag)
        }
    }

    @Test
    fun selectingPlannedDateCallsOnPlannedDateFilterChange() {
        val showBottomSheet = mutableStateOf(false)
        var selectedFilter: PlannedDateFilter? = null

        setListScreenContent(
            uiStateProvider = {
                ListUiState(showBottomSheet = showBottomSheet.value)
            },
            onPlannedDateChipClick = {
                showBottomSheet.value = true
            },
            onPlannedDateFilterChange = { filter ->
                selectedFilter = filter
                showBottomSheet.value = false
            },
            onDismissRequest = {
                showBottomSheet.value = false
            }
        )

        composeTestRule
            .onNodeWithText("Planned date")
            .performClick()

        composeTestRule
            .onNodeWithText("Today")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                PlannedDateFilter.TODAY,
                selectedFilter
            )
        }
    }

    @Test
    fun displaysUnscheduledHeaderForTodoWithoutPlannedDate() {
        val todo = TodoEntity(
            id = 2,
            title = "Read a book",
            detail = "Read one chapter",
            plannedDate = null,
            tag = null
        )

        val uiState = ListUiState(
            todoGroups = listOf(TodoDateGroup(null, listOf(todo)))
        )

        setListScreenContent(uiStateProvider = { uiState })

        composeTestRule
            .onNodeWithText("Unscheduled")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Read a book")
            .assertIsDisplayed()
    }

    @Test
    fun tappingEditQuickActionCallsOnEditWithTodoId() {
        val todo = TodoEntity(
            id = 42,
            title = "Update project",
            detail = "Review the implementation",
            plannedDate = null,
            tag = null
        )

        val uiState = ListUiState(
            todoGroups = listOf(
                TodoDateGroup(todo.plannedDate, listOf(todo))
            )
        )

        var editedTodoId: Int? = null

        setListScreenContent(
            uiStateProvider = { uiState },
            onEdit = { todoId ->
                editedTodoId = todoId
            }
        )

        composeTestRule
            .onNodeWithContentDescription(getString(R.string.todo_item_action_more_options))
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(getString(R.string.todo_item_action_edit))
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(42, editedTodoId)
        }
    }

    @Test
    fun displaysTodayHeaderForTodoPlannedToday() {
        val today = LocalDate.of(2026, 9, 26)
        val todo = TodoEntity(
            id = 2,
            title = "today's todo",
            detail = "Todo planned for today",
            plannedDate = today,
            tag = null
        )

        val uiState = ListUiState(
            today = today,
            todoGroups = listOf(
                TodoDateGroup(
                    date = today,
                    todos = listOf(todo)
                )
            )
        )

        setListScreenContent(uiStateProvider = { uiState })

        composeTestRule
            .onNodeWithText(getString(R.string.todo_list_header_today))
            .assertIsDisplayed()
    }

    @Test
    fun displaysYesterdayHeaderForTodoPlannedYesterday() {
        val today = LocalDate.of(2026, 9, 26)
        val yesterday = today.minusDays(1)
        val todo = TodoEntity(
            id = 3,
            title = "Yesterday's todo",
            detail = "Todo planned for yesterday",
            plannedDate = yesterday,
            tag = null
        )

        val uiState = ListUiState(
            today = today,
            todoGroups = listOf(
                TodoDateGroup(
                    date = yesterday,
                    todos = listOf(todo)
                )
            )
        )

        setListScreenContent(uiStateProvider = { uiState })

        composeTestRule
            .onNodeWithText(getString(R.string.todo_list_header_yesterday))
            .assertIsDisplayed()
    }

    @Test
    fun displaysTomorrowHeaderForTodoPlannedTomorrow() {
        val today = LocalDate.of(2026, 9, 26)
        val tomorrow = today.plusDays(1)
        val todo = TodoEntity(
            id = 4,
            title = "Tomorrow's todo",
            detail = "Todo planned for tomorrow",
            plannedDate = tomorrow,
            tag = null
        )

        val uiState = ListUiState(
            today = today,
            todoGroups = listOf(
                TodoDateGroup(
                    date = tomorrow,
                    todos = listOf(todo)
                )
            )
        )

        setListScreenContent(uiStateProvider = { uiState })

        composeTestRule
            .onNodeWithText(getString(R.string.todo_list_header_tomorrow))
            .assertIsDisplayed()
    }

    private fun setListScreenContent(
        uiStateProvider: () -> ListUiState = { ListUiState() },
        onCheckedChange: (TodoEntity, Boolean) -> Unit = { _, _ -> },
        onPlannedDateChange: (TodoEntity, LocalDate) -> Unit = { _, _ -> },
        onDelete: (TodoEntity) -> Unit = {},
        onEdit: (Int) -> Unit = {},
        onQueryChange: (String) -> Unit = {},
        onTagSelected: (TodoTag) -> Unit = {},
        onPlannedDateChipClick: () -> Unit = {},
        onPlannedDateFilterChange: (PlannedDateFilter) -> Unit = {},
        onDismissRequest: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TodoAppTheme {
                ListScreenContent(
                    uiState = uiStateProvider(),
                    onCheckedChange = onCheckedChange,
                    onPlannedDateChange = onPlannedDateChange,
                    onDelete = onDelete,
                    onEdit = onEdit,
                    onQueryChange = onQueryChange,
                    onTagSelected = onTagSelected,
                    onPlannedDateChipClick = onPlannedDateChipClick,
                    onPlannedDateFilterChange = onPlannedDateFilterChange,
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }

    private fun getString(@StringRes resourceId: Int): String {
        return InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(resourceId)
    }
}
