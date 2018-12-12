package sample.dyn.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import sample.dyn.model.Hotel
import sample.dyn.repo.HotelRepo

@RestController
class HotelController(val hotelRepo: HotelRepo) {

    val logger = LoggerFactory.getLogger(HotelController::class.java)

    @RequestMapping(value = ["/hotels"], method = [RequestMethod.POST])
    fun save(@RequestBody hotel: Hotel): Mono<ResponseEntity<Hotel>> {
        return hotelRepo.saveHotel(hotel)
                .map { saved ->
                    ResponseEntity.status(HttpStatus.CREATED).body(saved)
                }.onErrorResume { t ->
                    logger.error(t.message, t)
                    Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                }
    }

    @RequestMapping(value = ["/hotels"], method = [RequestMethod.GET])
    fun getHotelsByState(@RequestParam("state") state: String): Flux<Hotel> {
        return hotelRepo.findHotelsByState(state)
    }

}