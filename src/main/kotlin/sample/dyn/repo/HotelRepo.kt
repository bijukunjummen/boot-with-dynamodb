package sample.dyn.repo

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import sample.dyn.model.Hotel

interface HotelRepo {
    fun getHotel(id: String): Mono<Hotel>
    fun saveHotel(hotel: Hotel): Mono<Hotel>
    fun updateHotel(hotel: Hotel): Mono<Hotel>
    fun deleteHotel(id: String): Mono<Boolean>
    fun findHotelsByState(state: String): Flux<Hotel>
}