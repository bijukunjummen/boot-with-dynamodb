package sample.dyn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils
import sample.dyn.DynamoProperties
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder
import java.net.URI


@Configuration
class DynamoConfig {

    @Bean
    fun dynamoDbAsyncClient(dynamoProperties: DynamoProperties): DynamoDbAsyncClient {
        val builder:DynamoDbAsyncClientBuilder = DynamoDbAsyncClient.builder()
                .region(Region.of(dynamoProperties.region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())

        if (!StringUtils.isEmpty(dynamoProperties.endpoint)) {
            builder.endpointOverride(URI.create(dynamoProperties.endpoint))
        }

        return builder.build()
    }

    @Bean
    fun dynampoDbSyncClient(dynamoProperties: DynamoProperties): DynamoDbClient {
        val builder:DynamoDbClientBuilder = DynamoDbClient.builder()
                .region(Region.of(dynamoProperties.region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())

        if (!StringUtils.isEmpty(dynamoProperties.endpoint)) {
            builder.endpointOverride(URI.create(dynamoProperties.endpoint))
        }

        return builder.build()
    }

    @Bean
    fun dbMigrator(dynamoDbAsyncClient: DynamoDbAsyncClient, dynamoDbSyncClient: DynamoDbClient): DbMigrator {
        return DbMigrator(dynamoDbSyncClient)
    }

}