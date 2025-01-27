//package com.toyota.my_todo
//
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.extension.ExtendWith
//import org.junit.runner.RunWith
//import org.springframework.context.annotation.Import
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.context.junit4.SpringRunner
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@SpringBootTest
////@Import(value = [TestContainerAwsConfigBean::class])
//@ActiveProfiles("testcontainers")
//class DynamoDbRepositoryTest {
//
//    @Autowired
//    private lateinit var repository: DynamoDbRepository
//
//    @Test
//    fun `todoItemを保存できます`(){
//        val todoToSave = TodoItem(content = "use dynamoDB")
//        repository.append(todoToSave)
//        assertThat(repository.getDatastore()).contains(todoToSave)
//    }
//}