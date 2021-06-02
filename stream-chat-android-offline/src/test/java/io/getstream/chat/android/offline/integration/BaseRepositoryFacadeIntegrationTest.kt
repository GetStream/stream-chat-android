package io.getstream.chat.android.offline.integration

import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.test.TestCoroutineRule
import org.junit.After
import org.junit.Before
import org.junit.Rule

internal open class BaseRepositoryFacadeIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var chatDatabase: ChatDatabase
    protected lateinit var repositoryFacade: RepositoryFacade

    @Before
    @CallSuper
    open fun setup() {
        chatDatabase = createRoomDB(testCoroutines.dispatcher)
        repositoryFacade = createRepositoryFacade()
    }

    @After
    fun tearDown() {
        chatDatabase.close()
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
