package sample.dyn.repo

import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import sample.dyn.model.Hotel
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

@Repository
class DynamoHotelRepo(val dynamoClient: DynamoDbAsyncClient) : HotelRepo {
    override fun saveHotel(hotel: Hotel): Mono<Hotel> {
        val putItemRequest = PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(
                mapOf(
                    ID to AttributeValue.builder().s(hotel.id).build(),
                    NAME to AttributeValue.builder().s(hotel.name).build(),
                    ZIP to AttributeValue.builder().s(hotel.zip).build(),
                    STATE to AttributeValue.builder().s(hotel.state).build(),
                    ADDRESS to AttributeValue.builder().s(hotel.address).build(),
                    VERSION to AttributeValue.builder().n(hotel.version.toString()).build()
                )
            )
            .build()
        return Mono.fromCompletionStage(dynamoClient.putItem(putItemRequest))
            .flatMap {
                getHotel(hotel.id)
            }
    }

    override fun updateHotel(hotel: Hotel): Mono<Hotel> {
        val updateItemRequest = UpdateItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(
                mapOf(
                    ID to AttributeValue.builder().s(hotel.id).build()
                )
            )
            .updateExpression(
                """
                SET #name=:name,
                #state=:state,
                address=:address,
                zip=:zip 
                ADD version :inc
            """
            )
//            .conditionExpression("version = :version")
            .expressionAttributeValues(
                mapOf(
                    ":${NAME}" to AttributeValue.builder().s(hotel.name).build(),
                    ":${ZIP}" to AttributeValue.builder().s(hotel.zip).build(),
                    ":${STATE}" to AttributeValue.builder().s(hotel.state).build(),
                    ":${ADDRESS}" to AttributeValue.builder().s(hotel.address).build(),
//                    ":${VERSION}" to AttributeValue.builder().n(hotel.version.toString()).build(),
                    ":inc" to AttributeValue.builder().n("1").build()
                )
            )
            .expressionAttributeNames(
                mapOf(
                    "#name" to "name",
                    "#state" to "state"
                )
            )
            .build()
        val updateItem: CompletableFuture<UpdateItemResponse> = dynamoClient.updateItem(updateItemRequest)
        return Mono.fromCompletionStage(updateItem)
            .flatMap {
                getHotel(hotel.id)
            }
    }

    override fun deleteHotel(id: String): Mono<Boolean> {
        val deleteItemRequest = DeleteItemRequest.builder()
            .key(mapOf(ID to AttributeValue.builder().s(id).build()))
            .tableName(TABLE_NAME)
            .build()

        return Mono.fromCompletionStage(dynamoClient.deleteItem(deleteItemRequest))
            .map {
                true
            }
    }

    override fun getHotel(id: String): Mono<Hotel> {
        val getItemRequest: GetItemRequest = GetItemRequest.builder()
            .key(mapOf(ID to AttributeValue.builder().s(id).build()))
            .tableName(TABLE_NAME)
            .build()

        return Mono.fromCompletionStage(dynamoClient.getItem(getItemRequest))
            .map { resp ->
                fromMap(id, resp.item())
            }
    }

    override fun findHotelsByState(state: String): Flux<Hotel> {
        val qSpec: QueryRequest = QueryRequest
            .builder()
            .tableName(TABLE_NAME)
            .indexName(HOTELS_BY_STATE_INDEX)
            .keyConditionExpression("#st=:state")
            .expressionAttributeNames(mapOf("#st" to "state"))
            .expressionAttributeValues(mapOf(":state" to AttributeValue.builder().s(state).build()))
            .build()

        return Mono.from(dynamoClient.queryPaginator(qSpec)).flatMapIterable { resp ->
            resp.items().stream().map { item ->
                fromMap(item[ID]!!.s(), item)
            }.collect(Collectors.toList())
        }

    }

    companion object {
        const val TABLE_NAME = "hotels"
        const val ID = "id"
        const val NAME = "name"
        const val ZIP = "zip"
        const val STATE = "state"
        const val ADDRESS = "address"
        const val HOTELS_BY_STATE_INDEX = "HotelsByState"
        const val VERSION = "version"

        fun fromMap(key: String, map: Map<String, AttributeValue>): Hotel {
            return Hotel(
                id = key,
                name = map[NAME]?.s() ?: "",
                address = map[ADDRESS]?.s(),
                zip = map[ZIP]?.s(),
                state = map[STATE]?.s(),
                version = map[VERSION]?.n()?.toLong() ?: 1
            )
        }
    }

}
