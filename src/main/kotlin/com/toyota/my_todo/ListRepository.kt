package com.toyota.my_todo

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.server.ResponseStatusException
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

    fun updateDatastore(id: UUID, updateContent: TodoItem): List<TodoItem>{
        val todoIndex = datastore.indexOfFirst{ todo -> todo.id == id}
        if(todoIndex == -1){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found")
        }
        datastore[todoIndex].content = updateContent.content
        return datastore
    }
}