package sample.dyn.web

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import sample.dyn.model.Hotel
import sample.dyn.repo.HotelRepo


@WebFluxTest(value = [HotelController::class, HotelAdditionalRoutes::class])
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
            .body(
                BodyInserters
                    .fromValue(
                        """
                            | {
                            |   "id": "1",
                            |   "name": "Test Hotel",
                            |   "zip": "zip",
                            |   "address": "Test Address",
                            |   "state": "OR"
                            | }
                            """.trimMargin()
                    )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .json(
                """
                    | {
                    |   "id": "1",
                    |   "name": "Test Hotel",
                    |   "zip": "zip",
                    |   "address": "Test Address",
                    |   "state": "OR"
                    | }
                """.trimMargin()
            )
    }

    @Test
    fun testGetHotel() {
        val expectedHotel = Hotel(id = "2", name = "Test Hotel", address = "Test Address", state = "OR", zip = "zip")

        whenever(hotelRepo.getHotel("2"))
            .thenReturn(Mono.just(expectedHotel))

        whenever(hotelRepo.getHotel("3"))
            .thenReturn(Mono.empty())


        webTestClient.get()
            .uri("/hotels/2")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json(
                """
                    | {
                    |   "id": "2",
                    |   "name": "Test Hotel",
                    |   "zip": "zip",
                    |   "address": "Test Address",
                    |   "state": "OR"
                    | }

                """.trimMargin()
            )

        webTestClient.get()
            .uri("/hotels/3")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .exchange()
            .expectStatus().isNotFound

    }

    @Test
    fun testUpdateHotelWithNoExistingEntity() {

        whenever(hotelRepo.getHotel(ArgumentMatchers.anyString()))
            .thenReturn(Mono.empty())


        webTestClient.put()
            .uri("/hotels/2")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(
                BodyInserters
                    .fromValue(
                        """
                            | {
                            |   "id": "2",
                            |   "name": "Test Hotel",
                            |   "zip": "zip",
                            |   "address": "Test Address",
                            |   "state": "OR"
                            | }
                            """.trimMargin()
                    )
            )
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun testUpdateAnExistingEntity() {
        val expectedHotel = Hotel(id = "2", name = "Test Hotel", address = "Test Address", state = "OR", zip = "zip")

        whenever(hotelRepo.getHotel(Mockito.anyString()))
            .thenReturn(Mono.just(expectedHotel))

        whenever(hotelRepo.updateHotel(any()))
            .thenAnswer { invocation ->
                val hotel: Hotel = invocation.getArgument(0)
                Mono.just(hotel)
            }

        webTestClient.put()
            .uri("/hotels/2")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(
                BodyInserters
                    .fromValue(
                        """
                            | {
                            |   "id": "2",
                            |   "name": "Test Hotel Updated",
                            |   "zip": "zip",
                            |   "address": "Test Address",
                            |   "state": "OR"
                            | }
                            """.trimMargin()
                    )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .json(
                """
                    | {
                    |   "id": "2",
                    |   "name": "Test Hotel Updated",
                    |   "zip": "zip",
                    |   "address": "Test Address",
                    |   "state": "OR"
                    | }
                """.trimMargin()
            )
    }
}