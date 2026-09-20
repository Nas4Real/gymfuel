package com.gymfuel.app.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymfuel.app.core.data.local.GymFuelDatabase
import java.io.IOException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GymFuelMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation,
        GymFuelDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Before
    @After
    fun removeTestDatabase() {
        instrumentation.targetContext.deleteDatabase(TEST_DATABASE)
    }

    @Test
    @Throws(IOException::class)
    fun migration2To3_addsOwnerScopedProfileSchemaWithoutDroppingHistory() {
        helper.createDatabase(TEST_DATABASE, 2).close()

        helper.runMigrationsAndValidate(
            TEST_DATABASE,
            3,
            true,
            GymFuelDatabase.MIGRATION_2_3,
        ).use { database ->
            database.query("SELECT COUNT(*) FROM user_profiles").use { cursor ->
                cursor.moveToFirst()
                check(cursor.getInt(0) == 0)
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "gymfuel-migration-test"
    }
}
