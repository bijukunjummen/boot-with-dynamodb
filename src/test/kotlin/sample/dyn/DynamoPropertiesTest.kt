package sample.dyn

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
        classes = [DynamoProperties::class],
        properties = ["dynamo.endpoint=http://some-test-end-point", "dynamo.region=sample-region"]
)
@EnableConfigurationProperties(value=[DynamoProperties::class])
class DynamoPropertiesTest {

    @Autowired
    lateinit var dynamoProperties: DynamoProperties

    @Test
    fun testDynamoProperties() {
        assertThat(dynamoProperties.endpoint).isEqualTo("http://some-test-end-point")
        assertThat(dynamoProperties.region).isEqualTo("sample-region")
    }
}