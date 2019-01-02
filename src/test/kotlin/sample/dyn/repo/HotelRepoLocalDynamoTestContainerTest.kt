package sample.dyn.repo

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import sample.dyn.config.DbMigrator
import sample.dyn.model.Hotel
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder
import java.net.URI


class HotelRepoLocalDynamoTestContainerTest {
    @Test
    fun saveHotel() {
        val hotelRepo = DynamoHotelRepo(getAsyncClient(dynamoDB))
        val hotel = Hotel(id = "1", name = "test hotel", address = "test address", state = "OR", zip = "zip")
        val resp = hotelRepo.saveHotel(hotel)

        StepVerifier.create(resp)
                .expectNext(hotel)
                .expectComplete()
                .verify()
    }

    @Test
    fun deleteHotel() {
        val hotelRepo = DynamoHotelRepo(getAsyncClient(dynamoDB))
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
        val hotelRepo = DynamoHotelRepo(getAsyncClient(dynamoDB))
        val deleteResp = hotelRepo.deleteHotel("1")

        StepVerifier.create(deleteResp)
                .expectNext(true)
                .expectComplete()
                .verify()
    }

    @Test
    fun findHotelsByState() {
        val hotelRepo = DynamoHotelRepo(getAsyncClient(dynamoDB))
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



    companion object {
        val dynamoDB: KGenericContainer = KGenericContainer("amazon/dynamodb-local:1.11.119")
                .withExposedPorts(8000)

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            dynamoDB.start()
            val dbMigrator = DbMigrator(getSyncClient(dynamoDB))
            dbMigrator.migrate()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            dynamoDB.stop()
        }

        fun getAsyncClient(dynamoDB: KGenericContainer): DynamoDbAsyncClient {
            val endpointUri = "http://" + dynamoDB.getContainerIpAddress() + ":" +
                    dynamoDB.getMappedPort(8000)
            val builder: DynamoDbAsyncClientBuilder = DynamoDbAsyncClient.builder()
                    .endpointOverride(URI.create(endpointUri))
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider
                            .create(AwsBasicCredentials
                                    .create("acc", "sec")))
            return builder.build()
        }
        fun getSyncClient(dynamoDB: KGenericContainer): DynamoDbClient {
            val endpointUri = "http://" + dynamoDB.getContainerIpAddress() + ":" +
                    dynamoDB.getMappedPort(8000)
            val builder: DynamoDbClientBuilder = DynamoDbClient.builder()
                    .endpointOverride(URI.create(endpointUri))
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider
                            .create(AwsBasicCredentials
                                    .create("acc", "sec")))
            return builder.build()
        }
    }
}