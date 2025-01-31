package com.toyota.my_todo.child

import com.toyota.my_todo.TestContainerAwsConfigBean
import com.toyota.my_todo.repository.DynamoDbRepository
import com.toyota.my_todo.repository.TodoItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

@SpringBootTest
class DynamoDbRepositoryTest : TestContainerAwsConfigBean() {
    @Autowired
    private lateinit var repository: DynamoDbRepository

    @Test
    fun `データベースにアイテムがない場合は、空の配列を返す`() {
        val result = repository.getDatastore()
        println(result)
        assertThat(result).isEqualTo(listOf<String>())
    }

    @Test
    fun `todoItemを保存できる`() {
        val todoToSave = TodoItem(content = "use dynamoDB")
        repository.append(todoToSave)
        assertThat(repository.getDatastore()).contains(todoToSave)
    }

    @Test
    fun `todoItemを削除できる`() {
        val todoToSave = TodoItem(id = UUID.randomUUID() ,content = "use dynamoDB")
        repository.append(todoToSave)

        repository.deleteDatastore(todoToSave.id)

        assertThat(repository.getDatastore()).doesNotContain(todoToSave)
    }

    @Test
    fun `todoItemを更新できる`() {
        val todoToSave = TodoItem(id = UUID.randomUUID() ,content = "use dynamoDB")
        repository.append(todoToSave)

        val updateTodoIem = todoToSave.copy(content = "update!")
        repository.updateDatastore(updateTodoIem)

        assertThat(repository.getDatastore()).contains(updateTodoIem)
        assertThat(repository.getDatastore()).doesNotContain(todoToSave)
    }

    @Test
    fun `存在しないアイテムの場合、エラーが返る`() {
        val existingTodo = TodoItem(content = "use dynamoDB")
        repository.append(existingTodo)

        val updateTodo = TodoItem(content = "update!")

        val exception = assertThrows<ResponseStatusException> {
            repository.updateDatastore(updateTodo)
        }

        assertThat(exception.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}