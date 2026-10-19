package dev.twelnina.bunantodo.data.local

import dev.twelnina.bunantodo.model.Todo

fun TodoEntity.toTodo(): Todo = Todo(
    id = id,
    title = title,
    detail = detail,
    plannedDate = plannedDate,
    tag = tag,
    isCompleted = isCompleted
)

fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    title = title,
    detail = detail,
    plannedDate = plannedDate,
    tag = tag,
    isCompleted = isCompleted
)
