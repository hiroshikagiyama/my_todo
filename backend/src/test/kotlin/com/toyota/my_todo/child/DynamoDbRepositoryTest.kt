package com.toyota.my_todo.child

import com.toyota.my_todo.TestContainerAwsConfigBean
import com.toyota.my_todo.repository.DynamoDbRepository
import com.toyota.my_todo.repository.TodoItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DynamoDbRepositoryTest : TestContainerAwsConfigBean() {
    @Autowired
    private lateinit var repository: DynamoDbRepository

    @Test
    fun `todoItemを保存できる`() {
        val todoToSave = TodoItem(content = "use dynamoDB")
        repository.append(todoToSave)
        assertThat(repository.getDatastore()).contains(todoToSave)
    }
}