package com.toyota.my_todo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import software.amazon.awssdk.regions.Region

@Configuration
@Profile("!testcontainers")
class AwsConfigBean {
    data class AwsConfigOptions(
        val region: Region,
        val endpoint: String,
        val accessKey: String,
        val secret: String
    )

    @Bean
    fun awsConfig(env: Environment): AwsConfigOptions{
        return AwsConfigOptions(
            region = Region.of(env.getProperty("spring.cloud.aws.region.static")),
            endpoint = env.getProperty("spring.cloud.aws.dynamodb.endpoint")!!,
            accessKey = env.getProperty("spring.cloud.aws.credentials.access-key")!!,
            secret = env.getProperty("spring.cloud.aws.credentials.secret-key")!!
        )
    }
}