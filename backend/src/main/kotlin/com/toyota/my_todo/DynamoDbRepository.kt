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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI
import java.util.*

@Repository
class DynamoDbRepository(awsConfig: AwsConfigBean.AwsConfigOptions): TodoRepository {
    private lateinit var todoItemDynamoDbTable: DynamoDbTable<TodoItem>

    init {
        println("@@@@!!!!@@")
        println(awsConfig)
        val todoItemTableSchema = TableSchema.fromBean(TodoItem::class.java)
        println("made schema")
        val dynamoDbClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(DynamoDbClient.builder()
                .region(awsConfig.region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsConfig.accessKey, awsConfig.secret)))
                .endpointOverride(URI(awsConfig.endpoint))
                .build())
            .build()
        println("made dynamoDbClient")
        todoItemDynamoDbTable = dynamoDbClient.table("todo_item", todoItemTableSchema)
        println("made table object")
        try {
            todoItemDynamoDbTable.describeTable()
            println("described table")
        } catch (e: Exception) {
            todoItemDynamoDbTable.createTable()
            println("created table")
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