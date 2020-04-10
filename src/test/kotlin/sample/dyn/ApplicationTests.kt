package sample.dyn

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sample.dyn.rules.LocalDynamoExtension

@SpringBootTest
class ApplicationTests {

    @Test
    fun loadContexts() {

    }

    companion object {
        @RegisterExtension
        @JvmField
        val localDynamoExtension = LocalDynamoExtension()
    }

    @Configuration
    class SpringConfig {
        @Bean
        fun dynamoProperties(): DynamoProperties {
            return DynamoProperties(localDynamoExtension.endpoint!!, "us-east-1")
        }
    }

}