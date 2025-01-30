package com.toyota.my_todo.child

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.toyota.my_todo.repository.DynamoDbRepository
import com.toyota.my_todo.repository.TodoItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest
class ControllerTest {
    @MockitoBean
    private lateinit var dynamoDbRepository: DynamoDbRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper
    private val beforeTodoItem = TodoItem(content = "shiva")

    @Test
    fun `TODOが保存される前に、GET todoは空のリストを返します`() {
        val uuid = UUID.fromString("ccf17f62-5f15-47a8-a2a0-10033c275716")
        val mockStatic = Mockito.mockStatic(UUID::class.java)
        mockStatic.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)
        Mockito.`when`(dynamoDbRepository.getDatastore())
            .thenReturn(listOf(TodoItem(id = uuid, content = "shiva")))


        val result = mockMvc.get("/api/todo").andReturn()


        assertThat(result.response.status).isEqualTo(200)
        assertThat("""
            [{"id":"ccf17f62-5f15-47a8-a2a0-10033c275716","content":"shiva","isCompleted":false}]
        """.trimIndent())
            .isEqualTo(result.response.contentAsString)
        mockStatic.close()
    }

    @Test
    fun `A todoはPOSTで保存でき、GETで取得できます`() {
        val uuid = UUID.fromString("ccf17f62-5f15-47a8-a2a0-10033c275716")
        val mockStatic = Mockito.mockStatic(UUID::class.java)
        mockStatic.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)


        val postResult = mockMvc.perform(
            post("/api/todo")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"content": "Hello World"}
                """.trimIndent()
                )
        )
            .andReturn()


        assertThat(200).isEqualTo(postResult.response.status)
        Mockito.verify(dynamoDbRepository, Mockito.times(1))
            .append(
                TodoItem(id = uuid, content = "Hello World")
            )
        mockStatic.close()
    }

    @Test
    fun `アイテムを削除できます`() {
        //act
        val deleteResponse = mockMvc.perform(delete("/api/todo/${beforeTodoItem.id}"))
            .andReturn()

        //assert
        assertThat(200).isEqualTo(deleteResponse.response.status)
        Mockito.verify(dynamoDbRepository, Mockito.times(1))
            .deleteDatastore(
                beforeTodoItem.id
            )
    }

    @Test
    fun `存在するアイテムを更新できる`() {
        val afterTodoItem = beforeTodoItem.copy(content = "Diva")
        mockMvc.perform(
            post("/api/todo").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(beforeTodoItem))
        )

        val updateResponse = mockMvc.perform(
            patch("/api/todo/${beforeTodoItem.id}").contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(afterTodoItem))
        ).andReturn()

        assertThat(200).isEqualTo(updateResponse.response.status)
        assertThat(getAllTodo()).doesNotContain(beforeTodoItem)
        assertThat(getAllTodo()).contains(afterTodoItem)
    }

    @Test
    fun `存在しないアイテムを更新するとエラーメッセージが返る`() {
        val id = UUID.randomUUID()
        val afterTodoItem = TodoItem(id = id, content = "Diva")

        val updateResponse = mockMvc.perform(
            patch("/api/todo/${id}").contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(afterTodoItem))
        ).andReturn()
        val errorMessage = updateResponse.response.errorMessage

        assertThat(404).isEqualTo(updateResponse.response.status)
        assertThat("Not Found").isEqualTo(errorMessage)
    }

    @Test
    fun `アイテムのステータスを完了にできる`() {
        val afterTodoItem = beforeTodoItem.copy(isCompleted = true)
        mockMvc.perform(
            post("/api/todo").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(beforeTodoItem))
        )

        val completedResponse = mockMvc.perform(
            patch("/api/todo/${beforeTodoItem.id}").contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(afterTodoItem))
        ).andReturn()

        assertThat(200).isEqualTo(completedResponse.response.status)
        assertThat(getAllTodo()).doesNotContain(beforeTodoItem)
        assertThat(getAllTodo()).contains(afterTodoItem)
    }


    private fun getAllTodo(): List<TodoItem> {
        val getResult = mockMvc.get("/api/todo").andReturn()

        assertThat(200).isEqualTo(getResult.response.status)
        return objectMapper.readValue<List<TodoItem>>(getResult.response.contentAsString)
    }
}