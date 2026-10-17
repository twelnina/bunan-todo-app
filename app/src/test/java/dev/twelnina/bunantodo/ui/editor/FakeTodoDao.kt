package dev.twelnina.bunantodo.ui.editor

import dev.twelnina.bunantodo.data.local.TodoDao
import dev.twelnina.bunantodo.data.local.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FakeTodoDao : TodoDao {
    private val todos = MutableStateFlow<List<TodoEntity>>(emptyList())
    private var nextId = 1

    override suspend fun insert(todoEntity: TodoEntity) {
        val id = if (todoEntity.id == 0) nextId else todoEntity.id

        check(todos.value.none { it.id == id }) {
            "Todo with id=$id already exists"
        }

        nextId = maxOf(nextId, id + 1)

        todos.value += todoEntity.copy(id = id)
    }

    override suspend fun update(todoEntity: TodoEntity) {
        todos.value = todos.value.map { existing ->
            if (existing.id == todoEntity.id) {
                todoEntity
            } else {
                existing
            }
        }
    }

    override suspend fun delete(todoEntity: TodoEntity) {
        todos.value = todos.value.filterNot {
            it.id == todoEntity.id
        }
    }

    override fun observeTodayItems(today: LocalDate): Flow<List<TodoEntity>> {
        error("Not used by editor tests")
    }

    override fun getAllItem(): Flow<List<TodoEntity>> {
        error("Not used by editor tests")
    }

    override suspend fun getItem(id: Int): TodoEntity? = todos.value.find { it.id == id }

    override fun observePastIncompleteItems(today: LocalDate): Flow<List<TodoEntity>> {
        error("Not used by editor tests")
    }
}