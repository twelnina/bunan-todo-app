package dev.twelnina.bunantodo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.twelnina.bunantodo.TodoApplication
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.model.Todo
import kotlinx.coroutines.launch

class TodoAppViewModel(
    private val todoRepository: TodoRepository
) : ViewModel() {

    fun restoreDeletedTodo(todo: Todo) {
        viewModelScope.launch {
            todoRepository.insert(todo)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                as TodoApplication

                TodoAppViewModel(application.repository)
            }
        }
    }
}