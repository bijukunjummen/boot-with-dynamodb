package sample.dyn.migrator

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput

data class TableDefinition(
    val tableName: String,
    val attributeDefinitions: List<AttributeDefinition>,
    val keySchemaElements: List<KeySchemaElement>,
    val localSecondaryIndex: List<LocalSecondaryIndex>,
    val globalSecondaryIndex: List<GlobalSecondaryIndex>,
    val provisionedThroughput: ProvisionedThroughput
)