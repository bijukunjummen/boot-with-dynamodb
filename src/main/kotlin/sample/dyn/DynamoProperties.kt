package sample.dyn

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import software.amazon.awssdk.regions.Region

@ConfigurationProperties(prefix = "dynamo")
@Component
data class DynamoProperties(
        var endpoint: String = "",
        var region: String = Region.US_EAST_1.id()
)