package com.toyota.my_todo.repository

import java.util.*

interface TodoRepository {
    fun getDatastore(): List<TodoItem>
    fun append(newTodoItem: TodoItem)
    fun deleteDatastore(id: UUID)
    fun updateDatastore(updatedItem: TodoItem)
}