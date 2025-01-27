package com.toyota.my_todo

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.regions.Region

@TestConfiguration
@Testcontainers
@Profile("testcontainers")
class TestContainerAwsConfigBean {
    companion object {
        @Container
        val localStack: LocalStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(Service.DYNAMODB)
    }

    @Bean
    fun awsTestContainerConfig(): AwsConfigBean.AwsConfigOptions {
        println("IN TEST BEAN !!!!!")
        return AwsConfigBean.AwsConfigOptions(
            region = Region.of(localStack.region),
            endpoint = localStack.getEndpointOverride(Service.DYNAMODB).toString(),
            accessKey = localStack.accessKey,
            secret = localStack.secretKey
        )
    }
}