package sample.dyn.repo

import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import sample.dyn.Constants
import sample.dyn.model.Hotel
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import java.util.stream.Collectors

@Repository
class DynamoHotelRepo(val dynamoClient: DynamoDbAsyncClient) : HotelRepo {
    override fun saveHotel(hotel: Hotel): Mono<Hotel> {
        val putItemRequest = PutItemRequest.builder()
            .tableName(Constants.TABLE_NAME)
            .item(HotelDynamoAttributesMapper.toMap(hotel))
            .build()
        return Mono.fromCompletionStage(dynamoClient.putItem(putItemRequest))
            .flatMap {
                getHotel(hotel.id)
            }
    }

    override fun updateHotel(hotel: Hotel): Mono<Hotel> {
        val putItemRequest = UpdateItemRequest.builder()
            .tableName(Constants.TABLE_NAME)
            .key(HotelDynamoAttributesMapper.keyMap(hotel))
            .updateExpression(
                """
                SET #name=:name,#state=:state,address=:address,zip=:zip 
                ADD version :inc
            """
            )
            .conditionExpression("version = :version")
            .expressionAttributeValues(HotelDynamoAttributesMapper.toUpdateExpressionMap(hotel))
            .expressionAttributeNames(
                mapOf(
                    "#name" to "name",
                    "#state" to "state"
                )
            )
//            .attributeUpdates(HotelDynamoAttributesMapper.toUpdateMap(hotel))
            .build()
        return Mono.fromCompletionStage(dynamoClient.updateItem(putItemRequest))
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
                HotelDynamoAttributesMapper.fromMap(id, resp.item())
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
                HotelDynamoAttributesMapper.fromMap(item[Constants.ID]!!.s(), item)
            }.collect(Collectors.toList())
        }

    }

}
