package com.toyota.my_todo

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URI

abstract class TestContainerAwsConfigBean {
    protected companion object {
        val localStack: LocalStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(Service.DYNAMODB)

        init {
            localStack.start()
            val dynamoDbClient =
                DynamoDbClient
                    .builder()
                    .region(Region.AP_NORTHEAST_1)
                    .endpointOverride(URI.create("http://localhost:${localStack.getMappedPort(4566)}"))
                    .credentialsProvider(
                        StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("aaa", "zzz"),
                        ),
                    ).build()
            val createTableRequest = CreateTableRequest
                .builder()
                .attributeDefinitions(
                    AttributeDefinition
                        .builder()
                        .attributeName("PK")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                ).keySchema(
                    KeySchemaElement
                        .builder()
                        .attributeName("PK")
                        .keyType(KeyType.HASH)
                        .build()
                ).provisionedThroughput(
                    ProvisionedThroughput
                        .builder()
                        .readCapacityUnits(1)
                        .writeCapacityUnits(1)
                        .build(),
                )
                .tableName("todo_item")
                .build()
            dynamoDbClient.createTable(createTableRequest)
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamoDbProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.cloud.aws.dynamodb.endpoint") { "http://localhost:${localStack.getMappedPort(4566)}" }
            registry.add("spring.cloud.aws.dynamodb.table-name") { "todo_item" }
        }
    }

}