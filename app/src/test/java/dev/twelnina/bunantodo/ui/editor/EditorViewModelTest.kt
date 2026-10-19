package dev.twelnina.bunantodo.ui.editor

import androidx.lifecycle.viewModelScope
import dev.twelnina.bunantodo.MainDispatcherRule
import dev.twelnina.bunantodo.data.local.FakeTodoDao
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import dev.twelnina.bunantodo.model.Todo
import dev.twelnina.bunantodo.model.TodoTag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: EditorViewModel
    private lateinit var repository: TodoRepository

    @After
    fun tearDown() = runTest(timeout = 10.seconds) {
        viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
    }

    //    Add mode
    @Test
    fun saveTodo_addMode_persistsAllInputFields() = runTest {
        createViewModel(EditorMode.Add)
        runCurrent()

        val plannedDate = LocalDate.of(2026, 10, 17)

        viewModel.updateTitle("Grocery shopping")
        viewModel.updateDetail("Buy milk and bread")
        viewModel.updatePlannedDate(plannedDate)
        viewModel.updateSelectedTag(TodoTag.SHOPPING)

        var savedCount = 0
        viewModel.saveTodo { savedCount++ }
        runCurrent()

        val expected = Todo(
            id = 1,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = plannedDate,
            tag = TodoTag.SHOPPING,
            isCompleted = false
        )

        assertEquals(expected, repository.getItem(1))
        assertEquals(1, savedCount)
    }

    @Test
    fun saveTodo_addMode_doesNotSaveWhenTitleIsWhitespaceOnly() = runTest {
        createViewModel(EditorMode.Add)
        runCurrent()

        viewModel.updateTitle("   ")

        var savedCount = 0
        viewModel.saveTodo { savedCount++ }
        runCurrent()

        assertNull(repository.getItem(1))
        assertEquals(0, savedCount)
    }

    @Test
    fun saveTodo_addMode_doesNotSaveWhenTitleIsEmpty() = runTest {
        createViewModel(EditorMode.Add)
        runCurrent()

        viewModel.updateTitle("")

        var savedCount = 0
        viewModel.saveTodo { savedCount++ }
        runCurrent()

        assertNull(repository.getItem(1))
        assertEquals(0, savedCount)
    }

    @Test
    fun saveTodo_addMode_doesNotSaveBeforeTodayIsLoaded() = runTest {
        createViewModel(
            mode = EditorMode.Add,
            currentDates = MutableSharedFlow<LocalDate>()
        )
        runCurrent()

        viewModel.updateTitle("Grocery shopping")
        assertNull(viewModel.uiState.value.today)

        var savedCount = 0
        viewModel.saveTodo { savedCount++ }
        runCurrent()

        assertNull(viewModel.uiState.value.today)
        assertNull(repository.getItem(1))
        assertEquals(0, savedCount)
    }

    @Test
    fun saveTodo_addMode_savesWithTitleOnly() = runTest {
        createViewModel(EditorMode.Add)
        runCurrent()

        viewModel.updateTitle("Grocery shopping")

        var savedCount = 0
        viewModel.saveTodo { savedCount++ }
        runCurrent()

        val expected = Todo(
            id = 1,
            title = "Grocery shopping",
            detail = "",
            plannedDate = null,
            tag = null,
            isCompleted = false
        )

        assertEquals(expected, repository.getItem(1))
        assertEquals(1, savedCount)
    }

    //    Edit mode
    @Test
    fun uiState_editMode_loadsExistingTodo() = runTest {
        val existing = Todo(
            id = 1,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = LocalDate.of(2026, 10, 17),
            tag = TodoTag.SHOPPING,
            isCompleted = false
        )

        createViewModel(EditorMode.Edit(existing.id))
        repository.insert(existing)

        assertTrue(viewModel.uiState.value.isLoading)

        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isTodoMissing)
        assertEquals(existing.title, state.title)
        assertEquals(existing.detail, state.detail)
        assertEquals(existing.plannedDate, state.plannedDate)
        assertEquals(existing.tag, state.selectedTag)
    }

    @Test
    fun saveTodo_editMode_updatesInputFieldsAndPreservesIdAndCompletion() = runTest {
        val existing = Todo(
            id = 42,
            title = "Original title",
            detail = "Original detail",
            plannedDate = LocalDate.of(2026, 10, 17),
            tag = TodoTag.STUDY,
            isCompleted = true
        )

        createViewModel(EditorMode.Edit(existing.id))
        repository.insert(existing)
        runCurrent()

        val newPlannedDate = LocalDate.of(2026, 10, 20)

        viewModel.updateTitle("Updated title")
        viewModel.updateDetail("Updated detail")
        viewModel.updatePlannedDate(newPlannedDate)
        viewModel.updateSelectedTag(null)

        var savedCount = 0
        viewModel.saveTodo { savedCount++ }
        runCurrent()

        val expected = existing.copy(
            title = "Updated title",
            detail = "Updated detail",
            plannedDate = newPlannedDate,
            tag = null
        )

        assertEquals(expected, repository.getItem(existing.id))
        assertEquals(1, savedCount)
    }

    @Test
    fun saveTodo_editMode_doesNotSaveWhenTodoIsMissing() = runTest {
        createViewModel(EditorMode.Edit(42))
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isTodoMissing)
    }

    private fun createViewModel(
        mode: EditorMode,
        currentDates: Flow<LocalDate> = flowOf(LocalDate.of(2026, 10, 16))
    ) {
        repository = TodoRepository(FakeTodoDao())
        val dateProvider = object : CurrentDateProvider {
            override fun observeDate(): Flow<LocalDate> = currentDates
        }

        viewModel = EditorViewModel(
            repository = repository,
            currentDateProvider = dateProvider,
            mode = mode
        )
    }
}
