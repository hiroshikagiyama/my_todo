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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.web.server.ResponseStatusException
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

    /*
    - 準備
    UUID set
    val uuid = UUID.fromString("ccf17f62-5f15-47a8-a2a0-10033c275716")
        val mockStatic = Mockito.mockStatic(UUID::class.java)
        mockStatic.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)

    模擬のデータ準備(MockのDBの戻り値を固定している)
    Mockito.`when`(dynamoDbRepository.getDatastore())
            .thenReturn(listOf(TodoItem(id = uuid, content = "DIG")))

    - 実行
    get all（Mockからの返値をGetする）
    val result = mockMvc.get("/api/todo").andReturn()

    - 確認
     assertThat(result.response.status).isEqualTo(200)
     assertThat("""
        [{"id":"ccf17f62-5f15-47a8-a2a0-10033c275716","content":"shiva","isCompleted":false}]
        """.trimIndent())
            .isEqualTo(result.response.contentAsString)
        mockStatic.close()
     */

    @Test
    // fun `POST /api/todo` () {}
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
        // deleteDatastoreが呼び出されているかを確認する、呼び出し回数が１回
        Mockito.verify(dynamoDbRepository, Mockito.times(1))
            .deleteDatastore(
                // 呼び出し時の引数の確認
                beforeTodoItem.id
            )
    }

    @Test
    fun `存在するアイテムを更新できる`() {
        val afterTodoItem = beforeTodoItem.copy(content = "Diva")

        val updateResponse = mockMvc.perform(
            patch("/api/todo/${beforeTodoItem.id}").contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(afterTodoItem))
        ).andReturn()

        assertThat(updateResponse.response.status).isEqualTo(200)
        Mockito.verify(dynamoDbRepository, Mockito.times(1))
            .updateDatastore(
                // 呼び出し時の引数の確認
                afterTodoItem
            )
    }

    @Test
    fun `アイテムのステータスを完了にできる`() {
        val afterTodoItem = beforeTodoItem.copy(isCompleted = true)

        val completedResponse = mockMvc.perform(
            patch("/api/todo/${beforeTodoItem.id}").contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(afterTodoItem))
        ).andReturn()

        assertThat(200).isEqualTo(completedResponse.response.status)
        Mockito.verify(dynamoDbRepository, Mockito.times(1))
            .updateDatastore(
                // 呼び出し時の引数の確認
                afterTodoItem
            )
    }

    @Test
    fun `存在しないアイテムを更新するとエラーメッセージが返る`() {
        // 偽物のアイテム作成
        val id = UUID.randomUUID()
        val afterTodoItem = TodoItem(id = id, content = "Diva")
        // 返値をセット
        Mockito.`when`(dynamoDbRepository.updateDatastore(afterTodoItem))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found"))

        // 偽物のアイテムをPATCH
        val updateResponse = mockMvc.perform(
            patch("/api/todo/${id}").contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(afterTodoItem))
        ).andReturn()

        assertThat(updateResponse.response.status).isEqualTo(404)
    }



//    private fun getAllTodo(): List<TodoItem> {
//        val getResult = mockMvc.get("/api/todo").andReturn()
//
//        assertThat(200).isEqualTo(getResult.response.status)
//        return objectMapper.readValue<List<TodoItem>>(getResult.response.contentAsString)
//    }
}