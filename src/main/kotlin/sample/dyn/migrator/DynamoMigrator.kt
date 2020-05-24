package sample.dyn.migrator

import io.vavr.control.Try
import org.slf4j.Logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import sample.dyn.extensions.kotlin.loggerFor
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException
import software.amazon.awssdk.services.dynamodb.model.TableStatus
import java.time.Duration

class DynamoMigrator(
    private val syncClient: DynamoDbClient
) {
    fun migrate(definitions: List<TableDefinition>): Flux<DescribeTableResponse> {
        return Flux.fromIterable(definitions)
            .doOnNext { definition: TableDefinition -> LOGGER.info("About to run migration for $definition") }
            .flatMap { definition: TableDefinition -> migrate(definition) }
            .doOnNext { describeTableResponse: DescribeTableResponse ->
                LOGGER.info("Completed migration: $describeTableResponse")
            }
    }

    private fun migrate(tableDefinition: TableDefinition): Mono<DescribeTableResponse> {
        val describeTableRequest: DescribeTableRequest = DescribeTableRequest.builder()
            .tableName(tableDefinition.tableName)
            .build()

        return Mono
            .fromSupplier { Try.of { syncClient.describeTable(describeTableRequest) } }
            .flatMap { t: Try<DescribeTableResponse> ->
                //Existing Table
                if (t.isSuccess()) {
                    //Update existing table
                    val describeTableResponse: DescribeTableResponse = t.get()
                    Mono.just(t.get())
                } else {
                    when (t.getCause()) {
                        //table does not exist, so create one with the provided specs
                        is ResourceNotFoundException -> {
                            val createBuilder: CreateTableRequest.Builder = CreateTableRequest.builder()
                                .attributeDefinitions(tableDefinition.attributeDefinitions)
                                .keySchema(tableDefinition.keySchemaElements)
                                .provisionedThroughput(tableDefinition.provisionedThroughput)
                                .tableName(tableDefinition.tableName)

                            if (tableDefinition.localSecondaryIndex.isNotEmpty()) {
                                createBuilder.localSecondaryIndexes(tableDefinition.localSecondaryIndex)
                            }
                            if (tableDefinition.globalSecondaryIndex.isNotEmpty()) {
                                createBuilder.globalSecondaryIndexes(tableDefinition.globalSecondaryIndex)
                            }

                            val createTableRequest: CreateTableRequest = createBuilder.build()

                            Mono
                                .fromSupplier { syncClient.createTable(createTableRequest) }
                                .flatMap { _: CreateTableResponse ->
                                    Mono.fromSupplier {
                                        syncClient.describeTable(describeTableRequest)
                                    }.map { describeTable: DescribeTableResponse ->
                                        if (describeTable.table().tableStatus() != TableStatus.ACTIVE) {
                                            throw RuntimeException("Table Not Ready")
                                        }
                                        describeTable
                                    }
                                        //Wait for table to be active.
                                        .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                                }
                        }
                        else -> Mono.error(t.getCause()!!)
                    }

                }
            }
    }

    companion object {
        private val LOGGER: Logger = loggerFor<DynamoMigrator>()
    }
}
