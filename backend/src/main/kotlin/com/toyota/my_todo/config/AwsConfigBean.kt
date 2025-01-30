package com.toyota.my_todo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import software.amazon.awssdk.regions.Region

@Configuration
class AwsConfigBean {
    data class AwsConfigOptions(
        val region: Region,
        val endpoint: String,
        val accessKey: String,
        val secret: String,
        val tableName: String,
    )

    @Bean
    fun awsConfig(env: Environment): AwsConfigOptions {
        return AwsConfigOptions(
            region = Region.of(env.getProperty("spring.cloud.aws.region.static")),
            endpoint = env.getProperty("spring.cloud.aws.dynamodb.endpoint")!!,
            accessKey = env.getProperty("spring.cloud.aws.credentials.access-key")!!,
            secret = env.getProperty("spring.cloud.aws.credentials.secret-key")!!,
            tableName = env.getProperty("spring.cloud.aws.dynamodb.table-name")!!,
        )
    }
}