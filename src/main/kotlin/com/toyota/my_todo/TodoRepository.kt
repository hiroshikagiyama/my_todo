package com.toyota.my_todo

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

interface TodoRepository {
    fun getDatastore(): List<TodoItem>
    fun append(newTodoItem: TodoItem)
    fun deleteDatastore(id: UUID)
    fun updateDatastore(updatedItem: TodoItem)
}