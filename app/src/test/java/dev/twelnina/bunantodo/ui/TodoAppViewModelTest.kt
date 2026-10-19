package dev.twelnina.bunantodo.ui

import androidx.lifecycle.viewModelScope
import dev.twelnina.bunantodo.data.local.FakeTodoDao
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.model.Todo
import dev.twelnina.bunantodo.model.TodoTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodoAppViewModelTest {
    private lateinit var repository: TodoRepository
    private lateinit var viewModel: TodoAppViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = TodoRepository(FakeTodoDao())
        viewModel = TodoAppViewModel(repository)
    }

    @After
    fun tearDown() = runTest {
        try {
            viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restoreDeletedTodo_preservesAllFields() = runTest {
        val todo = Todo(
            id = 42,
            title = "Grocery shopping",
            detail = "Buy milk and bread",
            plannedDate = LocalDate.of(2026, 10, 19),
            tag = TodoTag.SHOPPING,
            isCompleted = true
        )
        repository.insert(todo)
        repository.delete(todo)
        assertNull(repository.getItem(todo.id))

        viewModel.restoreDeletedTodo(todo)
        runCurrent()

        assertEquals(todo, repository.getItem(todo.id))
    }

    @Test
    fun restoreDeletedTodo_preservesNullFieldsAndIncompleteState() = runTest {
        val todo = Todo(
            id = 7,
            title = "Unscheduled todo",
            detail = "",
            plannedDate = null,
            tag = null,
            isCompleted = false
        )
        repository.insert(todo)
        repository.delete(todo)
        assertNull(repository.getItem(todo.id))

        viewModel.restoreDeletedTodo(todo)
        runCurrent()

        assertEquals(todo, repository.getItem(todo.id))
    }
}
