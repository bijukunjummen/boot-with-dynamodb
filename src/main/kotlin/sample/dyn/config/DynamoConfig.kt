package sample.dyn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils
import sample.dyn.DynamoProperties
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder
import java.net.URI


@Configuration
class DynamoConfig {

    @Bean
    fun dynamoDbClient(dynamoProperties: DynamoProperties): DynamoDbAsyncClient {
        val builder:DynamoDbAsyncClientBuilder = DynamoDbAsyncClient.builder()
                .region(Region.of(dynamoProperties.region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())

        if (!StringUtils.isEmpty(dynamoProperties.endpoint)) {
            builder.endpointOverride(URI.create(dynamoProperties.endpoint))
        }

        return builder.build()
    }

    @Bean
    fun dbMigrator(dynamoDbClient: DynamoDbAsyncClient): DbMigrator {
        return DbMigrator(dynamoDbClient)
    }

}