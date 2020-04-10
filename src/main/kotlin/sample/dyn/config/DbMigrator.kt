package sample.dyn.config

import reactor.core.publisher.Mono
import sample.dyn.Constants
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.Projection
import software.amazon.awssdk.services.dynamodb.model.ProjectionType
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import software.amazon.awssdk.services.dynamodb.model.TableStatus
import java.time.Duration
import javax.annotation.PostConstruct


class DbMigrator(private val syncClient: DynamoDbClient) {

    @PostConstruct
    fun migrate() {
        val describeTableRequest = DescribeTableRequest.builder()
            .tableName(Constants.TABLE_NAME)
            .build()


        val migrationResult = Mono.fromSupplier {
            syncClient
                .describeTable(describeTableRequest)
        }
            .onErrorResume { error ->
                if (error is ResourceNotFoundException) {
                    val byStateIndex = GlobalSecondaryIndex.builder()
                        .indexName(Constants.HOTELS_BY_STATE_INDEX)
                        .provisionedThroughput(
                            ProvisionedThroughput.builder()
                                .readCapacityUnits(10)
                                .writeCapacityUnits(10)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())


                    byStateIndex
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName(Constants.STATE)
                                .keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder()
                                .attributeName(Constants.NAME)
                                .keyType(KeyType.RANGE).build()
                        )

                    val createTableRequest = CreateTableRequest.builder()
                        .attributeDefinitions(
                            AttributeDefinition.builder()
                                .attributeName(Constants.ID)
                                .attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder()
                                .attributeName(Constants.NAME)
                                .attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder()
                                .attributeName(Constants.STATE)
                                .attributeType(ScalarAttributeType.S).build()
                        )
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName(Constants.ID)
                                .keyType(KeyType.HASH).build()
                        )
                        .provisionedThroughput(
                            ProvisionedThroughput.builder()
                                .readCapacityUnits(10)
                                .writeCapacityUnits(10)
                                .build()
                        )
                        .tableName(Constants.TABLE_NAME)
                        .globalSecondaryIndexes(byStateIndex.build())
                        .build()

                    Mono
                        .fromSupplier { syncClient.createTable(createTableRequest) }
                        .flatMap {
                            Mono.fromSupplier {
                                syncClient.describeTable(describeTableRequest)
                            }.map { describeTable: DescribeTableResponse ->
                                if (describeTable.table().tableStatus() != TableStatus.ACTIVE) {
                                    throw RuntimeException("Table Not Ready")
                                }
                                describeTable
                            }
                                .retryBackoff(5, Duration.ofSeconds(1), Duration.ofSeconds(2))
                        }
                } else {
                    Mono.error(error)
                }
            }

        println(migrationResult.block())
    }
}