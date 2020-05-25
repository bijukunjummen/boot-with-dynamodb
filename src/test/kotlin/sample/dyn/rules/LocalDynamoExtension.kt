package sample.dyn.rules

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.ServerSocket
import java.net.URI
import java.nio.file.Paths

class LocalDynamoExtension : BeforeAllCallback, AfterAllCallback {
    private lateinit var server: DynamoDBProxyServer
    lateinit var endpoint: String
    lateinit var asyncClient: DynamoDbAsyncClient
    lateinit var syncClient: DynamoDbClient

    override fun beforeAll(context: ExtensionContext) {
        val currentPath = Paths.get(".")
        val libPath = currentPath.toAbsolutePath()
            .parent
            .resolve("build")
            .resolve("native-libs")


        System.setProperty("sqlite4java.library.path", libPath.toAbsolutePath().toString())
        val port = randomFreePort()
        val dynamoDbServer =
            ServerRunner.createServerFromCommandLineArgs(arrayOf("-inMemory", "-port", Integer.toString(port)))
        dynamoDbServer.start()

        server = dynamoDbServer
        this.endpoint = "http://localhost:$port"

        System.setProperty("aws.accessKeyId", "test-access-key")
        System.setProperty("aws.secretAccessKey", "test-secret-key")


        val asyncClientBuilder = DynamoDbAsyncClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .endpointOverride(URI.create(this.endpoint))

        this.asyncClient = asyncClientBuilder.build()

        val syncClientBuilder = DynamoDbClient.builder().region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .endpointOverride(URI.create(this.endpoint))

        this.syncClient = syncClientBuilder.build()
    }

    override fun afterAll(context: ExtensionContext) {
        this.server.stop()
        System.clearProperty("aws.accessKeyId")
        System.clearProperty("aws.secretAccessKey")
    }

    private fun randomFreePort(): Int = ServerSocket(0).use { serverSocket -> return serverSocket.localPort }

}
