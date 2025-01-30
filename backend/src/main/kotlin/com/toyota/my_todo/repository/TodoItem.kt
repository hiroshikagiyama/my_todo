package com.toyota.my_todo.repository

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import java.util.UUID
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
data class TodoItem(
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var id: UUID = UUID.randomUUID(),

    var content: String,
    var isCompleted: Boolean = false
){
    constructor() : this(content = "")
}