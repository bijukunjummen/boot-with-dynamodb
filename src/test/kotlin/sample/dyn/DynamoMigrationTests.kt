package sample.dyn

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import sample.dyn.config.DbMigrator
import sample.dyn.rules.LocalDynamoExtension

class DynamoMigrationTests {
    @Test
    fun testMigrations() {
        val migrator = DbMigrator(localDynamoExtension.syncClient!!)
        migrator.migrate()
    }

    companion object {
        @RegisterExtension
        @JvmField
        val localDynamoExtension = LocalDynamoExtension()
    }
}