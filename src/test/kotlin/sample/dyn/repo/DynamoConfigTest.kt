package sample.dyn.repo

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import reactor.test.StepVerifier
import sample.dyn.DynamoProperties
import sample.dyn.config.DbMigrator
import sample.dyn.model.Hotel
import sample.dyn.rules.LocalDynamoExtension
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder
import java.net.URI

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
class DynamoConfigTest {

    @Test
    fun saveHotel() {
        val hotelRepo = DynamoHotelRepo(localDynamoExtension.asyncClient!!)
        val hotel = Hotel(id = "1", name = "test hotel", address = "test address", state = "OR", zip = "zip")
        val resp = hotelRepo.saveHotel(hotel)

        StepVerifier.create(resp)
            .expectNext(hotel)
            .expectComplete()
            .verify()
    }

    @Test
    fun deleteHotel() {
        val hotelRepo = DynamoHotelRepo(localDynamoExtension.asyncClient!!)
        val hotel = Hotel(id = "1", name = "test hotel", address = "test address", state = "OR", zip = "zip")
        val deleteResp = hotelRepo
            .saveHotel(hotel)
            .flatMap { hotelRepo.deleteHotel("1") }

        StepVerifier.create(deleteResp)
            .expectNext(true)
            .expectComplete()
            .verify()
    }

    @Test
    fun deleteNonExistentHotel() {
        val hotelRepo = DynamoHotelRepo(localDynamoExtension.asyncClient!!)
        val deleteResp = hotelRepo.deleteHotel("1")

        StepVerifier.create(deleteResp)
            .expectNext(true)
            .expectComplete()
            .verify()
    }

    @Test
    fun findHotelsByState() {
        val hotelRepo = DynamoHotelRepo(localDynamoExtension.asyncClient!!)
        val hotel1 = Hotel(id = "1", name = "test hotel1", address = "test address1", state = "OR", zip = "zip")
        val hotel2 = Hotel(id = "2", name = "test hotel2", address = "test address2", state = "OR", zip = "zip")
        val hotel3 = Hotel(id = "3", name = "test hotel3", address = "test address3", state = "WA", zip = "zip")


        val resp = hotelRepo.saveHotel(hotel1)
            .then(hotelRepo.saveHotel(hotel2))
            .then(hotelRepo.saveHotel(hotel3))

        StepVerifier.create(resp)
            .expectNext(hotel3)
            .expectComplete()
            .verify()

        StepVerifier.create(hotelRepo.findHotelsByState("OR"))
            .expectNext(hotel1, hotel2)
            .expectComplete()
            .verify()

        StepVerifier.create(hotelRepo.findHotelsByState("WA"))
            .expectNext(hotel3)
            .expectComplete()
            .verify()

    }

    @TestConfiguration
    class SpringConfig {
        @Bean
        fun dynamoDbAsyncClient(dynamoProperties: DynamoProperties): DynamoDbAsyncClient {
            val builder: DynamoDbAsyncClientBuilder = DynamoDbAsyncClient.builder()
                .region(Region.of("us-east-1"))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .endpointOverride(URI.create(localDynamoExtension.endpoint!!))

            return builder.build()
        }

        @Bean
        fun dynamoDbSyncClient(dynamoProperties: DynamoProperties): DynamoDbClient {
            val builder: DynamoDbClientBuilder = DynamoDbClient.builder()
                .region(Region.of("us-east-1"))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .endpointOverride(URI.create(localDynamoExtension.endpoint!!))

            return builder.build()
        }
    }

    companion object {
        @RegisterExtension
        @JvmField
        val localDynamoExtension = LocalDynamoExtension()

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val dbMigrator = DbMigrator(localDynamoExtension.syncClient!!)
            dbMigrator.migrate()
        }

    }
}