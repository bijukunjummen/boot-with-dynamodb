package sample.dyn.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import sample.dyn.model.Hotel
import sample.dyn.repo.HotelRepo

@Configuration
class HotelAdditionalRoutes {

    @Bean
    fun routes(hotelRepo: HotelRepo) = router {
        GET("/hotels/{id}") { req ->
            val id = req.pathVariable("id")
            val response: Mono<ServerResponse> = hotelRepo.getHotel(id)
                .flatMap { hotel ->
                    ServerResponse.ok().body(BodyInserters.fromValue(hotel))
                }
            response.switchIfEmpty(ServerResponse.notFound().build())
        }

        PUT("/hotels/{id}") { req ->
            val id = req.pathVariable("id")
            val hotelReq: Mono<Hotel> = req.bodyToMono()

            val hotelToReturn: Mono<Hotel> =
                hotelRepo.getHotel(id)
                    .flatMap {
                        //hotel with id exists in db
                        hotelReq.flatMap { hotel ->
                            val toSave: Hotel = hotel.copy(id = id)
                            hotelRepo.updateHotel(toSave)
                        }
                    }

            hotelToReturn.flatMap { hotel ->
                ServerResponse
                    .status(HttpStatus.CREATED)
                    .body(BodyInserters.fromValue(hotel))
            }.switchIfEmpty(ServerResponse.notFound().build())
        }
    }
}