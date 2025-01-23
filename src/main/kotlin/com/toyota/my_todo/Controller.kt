package com.toyota.my_todo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController()
class Controller {
    @Autowired lateinit var listRepository: ListRepository

    @GetMapping("/api/todo")
    fun getTodo(): List<TodoItem> {
      return listRepository.getDatastore()
    }

    @PostMapping("/api/todo")
    fun postTodo(@RequestBody content: TodoItem){
        listRepository.append(content)
    }

    @DeleteMapping("/api/todo/{todoId}")
    fun deleteTodo(@PathVariable todoId:UUID){
        listRepository.deleteDatastore(todoId)
    }

    @PatchMapping("/api/todo/{todoId}")
    fun updateTodo(@PathVariable todoId: UUID, @RequestBody updateContent: TodoItem){
        listRepository.updateDatastore(todoId, updateContent)
    }

}