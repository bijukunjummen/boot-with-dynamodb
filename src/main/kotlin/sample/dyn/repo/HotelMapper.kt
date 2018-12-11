package sample.dyn.repo

import sample.dyn.Constants
import sample.dyn.model.Hotel
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object HotelMapper {
    fun toMap(hotel: Hotel): Map<String, AttributeValue> {
        return mapOf(
                Constants.ID to AttributeValue.builder().s(hotel.id).build(),
                Constants.NAME to AttributeValue.builder().s(hotel.name).build(),
                Constants.ZIP to AttributeValue.builder().s(hotel.zip).build(),
                Constants.STATE to AttributeValue.builder().s(hotel.state).build(),
                Constants.ADDRESS to AttributeValue.builder().s(hotel.address).build()
        )
    }

    fun fromMap(key: String, map: Map<String, AttributeValue> ): Hotel {
        return Hotel(
                id = key,
                name = map[Constants.NAME]?.s(),
                address = map[Constants.ADDRESS]?.s(),
                zip = map[Constants.ZIP]?.s(),
                state = map[Constants.STATE]?.s()
        )
    }

}