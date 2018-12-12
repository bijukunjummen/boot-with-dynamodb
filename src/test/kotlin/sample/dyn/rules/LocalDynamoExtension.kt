package sample.dyn.rules

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import sample.dyn.DynamoMigrationTests
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import java.net.ServerSocket
import java.net.URI
import java.nio.file.Paths

class LocalDynamoExtension : BeforeAllCallback, AfterAllCallback {

    private var server: DynamoDBProxyServer? = null
    var endpoint: String? = null
    var client: DynamoDbAsyncClient? = null

    override fun beforeAll(context: ExtensionContext) {
        val currentPath = Paths.get(".")
        val libPath = currentPath.toAbsolutePath()
                .parent
                .resolve("build")
                .resolve("native-libs")


        System.setProperty("sqlite4java.library.path", libPath.toAbsolutePath().toString())
        val port = randomFreePort()
        val dynamoDbServer = ServerRunner.createServerFromCommandLineArgs(arrayOf("-inMemory", "-port", Integer.toString(port)))
        dynamoDbServer.start()

        server = dynamoDbServer
        this.endpoint = "http://localhost:$port"

        System.setProperty("aws.accessKeyId", "test-access-key")
        System.setProperty("aws.secretAccessKey", "test-secret-key")


        val clientBuilder = DynamoDbAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
        clientBuilder.endpointOverride(URI.create(this.endpoint))
        this.client = clientBuilder.build()

    }

    override fun afterAll(context: ExtensionContext) {
        if (this.server != null) {
            this.server!!.stop()
            System.clearProperty("aws.accessKeyId")
            System.clearProperty("aws.secretAccessKey")
        }

    }

    private fun randomFreePort(): Int = ServerSocket(0).use { serverSocket -> return serverSocket.localPort }

}
