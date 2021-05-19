package io.getstream.chat.android.offline.integration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.asExecutor
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.Executors

internal open class BaseRepositoryFacadeIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var chatDatabase: ChatDatabase
    protected lateinit var repositoryFacade: RepositoryFacade

    @Before
    fun setup() {
        chatDatabase = createChatDatabase()
        repositoryFacade = createRepositoryFacade()
    }

    @After
    fun tearDown() {
        chatDatabase.close()
    }

    private fun createChatDatabase(): ChatDatabase {
        return Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ChatDatabase::class.java)
            .allowMainThreadQueries()
            // Use a separate thread for Room transactions to avoid deadlocks. This means that tests that run Room
            // transactions can't use testCoroutines.scope.runBlockingTest, and have to simply use runBlocking instead.
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .setQueryExecutor(testCoroutines.dispatcher.asExecutor())
            .build()
    }

    private fun createRepositoryFacade(): RepositoryFacade {
        return RepositoryFacadeBuilder {
            context(ApplicationProvider.getApplicationContext())
            scope(testCoroutines.scope)
            defaultConfig(Config())
            database(chatDatabase)
            setOfflineEnabled(false)
        }.build()
    }
}
