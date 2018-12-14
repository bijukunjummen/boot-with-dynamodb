package sample.dyn.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import sample.dyn.repo.HotelRepo

@Configuration
class HotelAdditionalRoutes {

    @Bean
    fun routes(hotelRepo: HotelRepo) = router {
        GET("/hotels/{id}") { req ->
            val id = req.pathVariable("id")
            val response: Mono<ServerResponse> = hotelRepo.getHotel(id)
                    .flatMap { hotel ->
                        ServerResponse.ok().body(BodyInserters.fromObject(hotel))
                    }
            response.switchIfEmpty(ServerResponse.notFound().build())
        }
    }
}
