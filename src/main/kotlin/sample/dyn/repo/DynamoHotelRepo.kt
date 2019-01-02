package sample.dyn.repo

import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import sample.dyn.Constants
import sample.dyn.model.Hotel
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.Condition
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.util.stream.Collectors

@Repository
class DynamoHotelRepo(val dynamoClient: DynamoDbAsyncClient) : HotelRepo {
    override fun saveHotel(hotel: Hotel): Mono<Hotel> {
        val putItemRequest = PutItemRequest.builder()
                .tableName(Constants.TABLE_NAME)
                .item(HotelMapper.toMap(hotel))
                .build()
        return Mono.fromCompletionStage(dynamoClient.putItem(putItemRequest))
                .flatMap {
                    getHotel(hotel.id)
                }
    }

    override fun deleteHotel(id: String): Mono<Boolean> {
        val deleteItemRequest = DeleteItemRequest.builder()
                .key(mapOf(Constants.ID to AttributeValue.builder().s(id).build()))
                .tableName(Constants.TABLE_NAME)
                .build()

        return Mono.fromCompletionStage(dynamoClient.deleteItem(deleteItemRequest))
                .map {
                    true
                }
    }

    override fun getHotel(id: String): Mono<Hotel> {
        val getItemRequest: GetItemRequest = GetItemRequest.builder()
                .key(mapOf(Constants.ID to AttributeValue.builder().s(id).build()))
                .tableName(Constants.TABLE_NAME)
                .build()

        return Mono.fromCompletionStage(dynamoClient.getItem(getItemRequest))
                .map { resp ->
                    HotelMapper.fromMap(id, resp.item())
                }
    }

    override fun findHotelsByState(state: String): Flux<Hotel> {
        val qSpec = QueryRequest
                .builder()
                .tableName(Constants.TABLE_NAME)
                .indexName(Constants.HOTELS_BY_STATE_INDEX)
                .keyConditionExpression("#st=:state")
                .expressionAttributeNames(mapOf("#st" to "state"))
                .expressionAttributeValues(mapOf(":state" to AttributeValue.builder().s(state).build()))
                .build()

        return Mono.from(dynamoClient.queryPaginator(qSpec)).flatMapIterable { resp ->
            resp.items().stream().map { item ->
                HotelMapper.fromMap(item[Constants.ID]!!.s(), item)
            }.collect(Collectors.toList())
        }

    }

}
