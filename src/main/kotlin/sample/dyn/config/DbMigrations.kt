package sample.dyn.config

import reactor.core.scheduler.Schedulers
import sample.dyn.migrator.DynamoMigrator
import sample.dyn.migrator.TableDefinition
import sample.dyn.repo.DynamoHotelRepo
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.Projection
import software.amazon.awssdk.services.dynamodb.model.ProjectionType
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import javax.annotation.PostConstruct



class DbMigrations(private val dynamoMigrator: DynamoMigrator) {
    fun hotelTableDefinition(): TableDefinition {
        val byStateIndex: GlobalSecondaryIndex = GlobalSecondaryIndex.builder()
            .indexName(DynamoHotelRepo.HOTELS_BY_STATE_INDEX)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName(DynamoHotelRepo.STATE)
                    .keyType(KeyType.HASH).build(),
                KeySchemaElement.builder()
                    .attributeName(DynamoHotelRepo.NAME)
                    .keyType(KeyType.RANGE).build()
            )
            .provisionedThroughput(
                ProvisionedThroughput.builder()
                    .readCapacityUnits(10)
                    .writeCapacityUnits(10)
                    .build()
            )
            .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
            .build()

        return TableDefinition(
            tableName = DynamoHotelRepo.TABLE_NAME,
            attributeDefinitions = listOf(
                AttributeDefinition.builder()
                    .attributeName(DynamoHotelRepo.ID)
                    .attributeType(ScalarAttributeType.S).build(),
                AttributeDefinition.builder()
                    .attributeName(DynamoHotelRepo.NAME)
                    .attributeType(ScalarAttributeType.S).build(),
                AttributeDefinition.builder()
                    .attributeName(DynamoHotelRepo.STATE)
                    .attributeType(ScalarAttributeType.S).build()
            ),
            keySchemaElements = listOf(
                KeySchemaElement.builder()
                    .attributeName(DynamoHotelRepo.ID)
                    .keyType(KeyType.HASH).build()
            ),
            globalSecondaryIndex = listOf(byStateIndex),
            localSecondaryIndex = emptyList(),
            provisionedThroughput = ProvisionedThroughput.builder()
                .readCapacityUnits(10)
                .writeCapacityUnits(10)
                .build()

        )
    }

    @PostConstruct
    fun migrate() {
        dynamoMigrator
            .migrate(listOf(hotelTableDefinition()))
            .subscribeOn(Schedulers.single())
            .subscribe()
    }
}