package sample.dyn

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import sample.dyn.config.DbMigrations
import sample.dyn.migrator.DynamoMigrator
import sample.dyn.rules.LocalDynamoExtension

class DynamoMigrationTests {
    @Test
    fun testMigrations() {
        val migrator: DynamoMigrator = DynamoMigrator(localDynamoExtension.syncClient!!)
        val migrations: DbMigrations = DbMigrations(migrator)
        migrator.migrate(listOf(migrations.hotelTableDefinition()))
    }

    companion object {
        @RegisterExtension
        @JvmField
        val localDynamoExtension = LocalDynamoExtension()
    }
}