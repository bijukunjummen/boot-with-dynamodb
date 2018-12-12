package sample.dyn.web

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import sample.dyn.model.Hotel
import sample.dyn.repo.HotelRepo


@ExtendWith(SpringExtension::class)
@WebFluxTest(HotelController::class)
class HotelControllerWebTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockBean
    lateinit var hotelRepo: HotelRepo


    @Test
    fun testCreateHotel() {
        val expectedHotel = Hotel(id = "1", name = "Test Hotel", address = "Test Address", state = "OR", zip = "zip")
        whenever(hotelRepo.saveHotel(any()))
                .thenReturn(Mono.just(expectedHotel))

        webTestClient.post()
                .uri("/hotels")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(BodyInserters
                        .fromObject("""
                            | {
                            |   "id": "1",
                            |   "name": "Test Hotel",
                            |   "zip": "zip",
                            |   "address": "Test Address",
                            |   "state": "OR"
                            | }
                            """.trimMargin()))
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .json("""
                    | {
                    |   "id": "1",
                    |   "name": "Test Hotel",
                    |   "zip": "zip",
                    |   "address": "Test Address",
                    |   "state": "OR"
                    | }

                """.trimMargin())
    }
}