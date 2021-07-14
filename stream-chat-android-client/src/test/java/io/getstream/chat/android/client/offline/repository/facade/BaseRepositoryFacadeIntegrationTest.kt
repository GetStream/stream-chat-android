package io.getstream.chat.android.client.offline.repository.facade

import androidx.annotation.CallSuper
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.Mother.randomUser
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.offline.repository.RepositoryFacade
import io.getstream.chat.android.client.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.Executors

internal open class BaseRepositoryFacadeIntegrationTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    protected val currentUser = randomUser()
    protected lateinit var repositoryFacade: RepositoryFacade

    @Before
    @CallSuper
    open fun setup() {
        repositoryFacade = createRepositoryFacade()
    }

    private fun createRepositoryFacade(): RepositoryFacade {
        return RepositoryFacadeBuilder {
            context(ApplicationProvider.getApplicationContext())
            currentUser(currentUser)
            scope(testCoroutines.scope)
            defaultConfig(Config())
            databaseBuilder(testDatabaseBuilder(testCoroutines.dispatcher))
            setOfflineEnabled(false)
        }.build()
    }

    private fun testDatabaseBuilder(dispatcher: CoroutineDispatcher): (RoomDatabase.Builder<*>) -> RoomDatabase.Builder<*> =
        { builder ->
            builder.allowMainThreadQueries()
                // Use a separate thread for Room transactions to avoid deadlocks. This means that tests that run Room
                // transactions can't use testCoroutines.scope.runBlockingTest, and have to simply use runBlocking instead.
                .setTransactionExecutor(Executors.newSingleThreadExecutor())
                .setQueryExecutor(dispatcher.asExecutor())
        }
}
