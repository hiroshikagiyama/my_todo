package com.toyota.my_todo

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ListRepository {
    private var datastore: List<TodoItem> = emptyList()

    fun append(newTodoItem: TodoItem){
        datastore += newTodoItem
    }

    fun getDatastore(): List<TodoItem> = datastore

    fun deleteDatastore(id: UUID): List<TodoItem> {
        datastore = datastore.filter{ it.id != id }
        return datastore
    }
}