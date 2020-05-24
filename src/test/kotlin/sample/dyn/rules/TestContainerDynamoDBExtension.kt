package sample.dyn.rules

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import sample.dyn.repo.KGenericContainer
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

/**
 * Spins up an Docker container based DynamoDB for use in JUnit5 based integration tests
 *
 *
 * Use it the following way:
 *
 *
 *
 * <pre>
 * @RegisterExtension
 * static TestContainerDynamoDBExtension dynamoDb = new TestContainerDynamoDBExtension();
</pre> *
 *
 *
 * Use the dynamically generated endpoint from the rule:
 *
 * <pre>
 * String endpoint = dynamoDb.getEndpoint()
</pre> *
 */
class TestContainerDynamoDBExtension : BeforeAllCallback, AfterAllCallback {
    private lateinit var server: KGenericContainer
    lateinit var endpoint: String
    lateinit var asyncClient: DynamoDbAsyncClient
    lateinit var syncClient: DynamoDbClient

    override fun beforeAll(context: ExtensionContext) {
        try {
            val serverLocal = KGenericContainer(LOCAL_DYNAMODB_IMAGE_NAME)
                .withExposedPorts(EXPOSED_PORT)
                .waitingFor(WaitAllStrategy())
            serverLocal.start()
            endpoint = String.format(
                "http://%s:%d",
                serverLocal.getContainerIpAddress(),
                serverLocal.getMappedPort(EXPOSED_PORT)
            )
            server = serverLocal
            System.setProperty("aws.accessKeyId", "access-key")
            System.setProperty("aws.secretKey", "secret-key")

            val asyncClientBuilder = DynamoDbAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .endpointOverride(URI.create(this.endpoint))

            this.asyncClient = asyncClientBuilder.build()

            val syncClientBuilder = DynamoDbClient.builder().region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .endpointOverride(URI.create(this.endpoint))

            this.syncClient = syncClientBuilder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun afterAll(context: ExtensionContext) {
        try {
            server.stop()
            System.clearProperty("aws.accessKeyId")
            System.clearProperty("aws.secretKey")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val LOCAL_DYNAMODB_IMAGE_NAME = "amazon/dynamodb-local:1.13.0"
        private const val EXPOSED_PORT = 8000
    }
}