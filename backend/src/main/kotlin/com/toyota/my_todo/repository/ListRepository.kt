package com.toyota.my_todo.repository

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.server.ResponseStatusException
import java.util.UUID


@Repository
class ListRepository: TodoRepository {
    private var datastore: MutableList<TodoItem> = mutableListOf()

    override fun append(newTodoItem: TodoItem){
        datastore += newTodoItem
    }

    override fun getDatastore(): List<TodoItem> = datastore

    override fun deleteDatastore(id: UUID){
        datastore = datastore.filter{todo -> todo.id != id }.toMutableList()
    }

    override fun updateDatastore(updatedItem: TodoItem){
        val todoIndex = datastore.indexOfFirst{ todo -> todo.id == updatedItem.id}
        if(todoIndex == -1){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found")
        }
        datastore[todoIndex] = updatedItem
    }
}