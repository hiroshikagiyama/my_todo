package com.toyota.my_todo

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI
import java.util.*

@Repository
class DynamoDbRepository: TodoRepository {
    private lateinit var todoItemDynamoDbTable: DynamoDbTable<TodoItem>
    init {
        val todoItemTableSchema = TableSchema.fromBean(TodoItem::class.java)
        val dynamoDbClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(DynamoDbClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("aaa", "aaa")))
                .endpointOverride(URI("http://localhost:4566/"))
                .build())
            .build()
        todoItemDynamoDbTable = dynamoDbClient.table("todo_item", todoItemTableSchema)

        try {
            todoItemDynamoDbTable.describeTable()
        } catch (e: Exception) {
            todoItemDynamoDbTable.createTable();
        }
    }

    override fun getDatastore(): List<TodoItem> {
        return todoItemDynamoDbTable.scan().items().toList()
    }

    override fun append(newTodoItem: TodoItem) {
        todoItemDynamoDbTable.putItem(newTodoItem)
    }

    override fun deleteDatastore(id: UUID) {
        val todoKey = Key.builder()
            .partitionValue(id.toString())
            .build()
        todoItemDynamoDbTable.deleteItem(todoKey)
    }

    override fun updateDatastore(updatedItem: TodoItem) {
        val todoKey = Key.builder()
            .partitionValue(updatedItem.id.toString())
            .build()
        if(todoItemDynamoDbTable.getItem(todoKey) != null){
            todoItemDynamoDbTable.updateItem(updatedItem)
        }else{
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found")
        }
    }
}