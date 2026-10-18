package dev.twelnina.bunantodo.ui.list.component

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.model.Todo
import dev.twelnina.bunantodo.ui.component.TodoItem
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TodoItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val todo = Todo(
        id = 1,
        title = "Study Kotlin",
        detail = "Read Compose documentation",
        plannedDate = null,
        tag = null
    )

    @Test
    fun todoContent_hasNoClickAction() {
        setTodoItem()

        composeTestRule
            .onNodeWithText(todo.title)
            .assert(hasNoClickAction())

        composeTestRule
            .onNodeWithText(todo.detail)
            .assert(hasNoClickAction())
    }

    @Test
    fun checkbox_callsOnCheckedChangeWithTodo() {
        var updatedTodo: Todo? = null
        var updatedValue: Boolean? = null

        setTodoItem(
            onCheckedChange = { selectedTodo, checked ->
                updatedTodo = selectedTodo
                updatedValue = checked
            }
        )

        composeTestRule
            .onNode(isToggleable() and isOff())
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(todo, updatedTodo)
            assertEquals(true, updatedValue)
        }
    }

    @Test
    fun quickActions_areEnabledOnlyWhileExpanded() {
        var expanded by mutableStateOf(false)

        setTodoItem(
            expandedProvider = { expanded },
            onMoreClick = { expanded = !expanded }
        )

        val editDescription = getString(R.string.todo_item_action_edit)

        composeTestRule
            .onNodeWithContentDescription(editDescription)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithContentDescription(getString(R.string.todo_item_action_more_options))
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(editDescription)
            .assertIsEnabled()
    }

    @Test
    fun quickActions_callCorrespondingCallbacks() {
        val calledActions = mutableListOf<String>()

        setTodoItem(
            expandedProvider = { true },
            onChangePlannedDate = { calledActions += "date" },
            onEdit = { calledActions += "edit" },
            onDelete = { calledActions += "delete" }
        )

        composeTestRule
            .onNodeWithContentDescription(
                getString(R.string.todo_item_action_change_planned_date)
            )
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(getString(R.string.todo_item_action_edit))
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(getString(R.string.todo_item_action_delete))
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf("date", "edit", "delete"),
                calledActions
            )
        }
    }

    @Test
    fun daysSincePlannedDate_displaysElapsedDays() {
        val days = 7

        setTodoItem(daysSincePlannedDate = days.toLong())

        composeTestRule
            .onNodeWithText(
                getQuantityString(
                    R.plurals.todo_item_status_days_since_planned_date,
                    days, days
                )
            )
            .assertIsDisplayed()
    }

    @Test
    fun daysSincePlannedDate_displaysSingularDay() {
        val days = 1

        setTodoItem(daysSincePlannedDate = days.toLong())

        composeTestRule
            .onNodeWithText(
                getQuantityString(
                    R.plurals.todo_item_status_days_since_planned_date,
                    days, days
                )
            )
            .assertIsDisplayed()
    }

    @Test
    fun daysSincePlannedDate_whenNull_doesNotDisplayStatus() {
        val days = 1

        setTodoItem(daysSincePlannedDate = null)

        composeTestRule
            .onNodeWithText(
                getQuantityString(
                    R.plurals.todo_item_status_days_since_planned_date,
                    days, days
                )
            )
            .assertDoesNotExist()
    }

    private fun setTodoItem(
        daysSincePlannedDate: Long? = null,
        expandedProvider: () -> Boolean = { false },
        onCheckedChange: (Todo, Boolean) -> Unit = { _, _ -> },
        onChangePlannedDate: () -> Unit = {},
        onMoreClick: () -> Unit = {},
        onEdit: () -> Unit = {},
        onDelete: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TodoAppTheme {
                TodoItem(
                    todoItemInfo = todo,
                    daysSincePlannedDate = daysSincePlannedDate,
                    index = 0,
                    count = 1,
                    expanded = expandedProvider(),
                    onCheckedChange = onCheckedChange,
                    onMoreClick = onMoreClick,
                    onChangePlannedDate = onChangePlannedDate,
                    onEdit = onEdit,
                    onDelete = onDelete
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

    private fun getQuantityString(
        @PluralsRes resourceId: Int,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        return InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .resources
            .getQuantityString(resourceId, quantity, *formatArgs)
    }
}
