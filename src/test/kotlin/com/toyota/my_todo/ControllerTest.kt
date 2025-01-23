package com.toyota.my_todo

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest
class ControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @Test
    fun `TODOが保存される前に、GET todoは空のリストを返します`(){
        val result = mockMvc.get("/api/todo").andReturn()
        assertThat(result.response.status).isEqualTo(200)
        assertThat("[]").isEqualTo(result.response.contentAsString)
    }


    @Test
    fun `A todoはPOSTで保存でき、GETで取得できます`(){
        //arrange
        val todoItemToSave = TodoItem(content = "頑張って")

        //action
        val postResult = mockMvc.perform(post("/api/todo").contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(todoItemToSave)))
            .andReturn()
        val getResult = mockMvc.get("/api/todo").andReturn()

        //assert
        val expectedResponse: List<TodoItem> = listOf(todoItemToSave)
        val responseContentAsList = objectMapper.readValue<List<TodoItem>>(getResult.response.contentAsString)

        assertThat(200).isEqualTo(postResult.response.status)
        assertThat(200).isEqualTo(getResult.response.status)
        assertThat(expectedResponse).isEqualTo(responseContentAsList)
    }

    @Test
    fun `アイテムを削除できます`(){
        //arrange
        val todoItemToDelete = TodoItem(content = "東京都に行く")
        val anotherTodoItem = TodoItem(content = "頑張って")
        mockMvc.perform(post("/api/todo").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(todoItemToDelete)))
        mockMvc.perform(post("/api/todo").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(anotherTodoItem)))

        //act
        val deleteResponse = mockMvc.perform(delete("/api/todo/${todoItemToDelete.id}")).andReturn()
        val getResult = mockMvc.get("/api/todo").andReturn()

        //assert
        val responseContentAsList = objectMapper.readValue<List<TodoItem>>(getResult.response.contentAsString)
        assertThat(200).isEqualTo(deleteResponse.response.status)
        assertThat(responseContentAsList).doesNotContain(todoItemToDelete)
        assertThat(responseContentAsList).contains(anotherTodoItem)
    }

    // DELETE test
    // Update test
    // Complete test
}