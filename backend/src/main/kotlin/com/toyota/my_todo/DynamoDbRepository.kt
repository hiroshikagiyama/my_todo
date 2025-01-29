package com.toyota.my_todo

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.server.ResponseStatusException
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

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Initializing DynamoDbRepository: awsConfig=$awsConfig")
        val todoItemTableSchema = TableSchema.fromBean(TodoItem::class.java)
        val dynamoDbClientBuilder = DynamoDbClient.builder()
        if (awsConfig.endpoint != "") {
            dynamoDbClientBuilder.endpointOverride(URI(awsConfig.endpoint))
            logger.info("overriding endpoint: ${awsConfig.endpoint}")
        }
        val dynamoDbClient = dynamoDbClientBuilder
            .region(awsConfig.region)
            //.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsConfig.accessKey, awsConfig.secret)))
            .build()
        val dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build()
        println("made dynamoDbClient: tableName=${awsConfig.tableName}")
        todoItemDynamoDbTable = dynamoDbEnhancedClient.table(awsConfig.tableName, todoItemTableSchema)
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