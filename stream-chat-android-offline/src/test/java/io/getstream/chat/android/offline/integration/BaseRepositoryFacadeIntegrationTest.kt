package io.getstream.chat.android.offline.integration

import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacadeBuilder
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
internal open class BaseRepositoryFacadeIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var chatDatabase: ChatDatabase

    protected val currentUser = randomUser()
    protected lateinit var repositoryFacade: RepositoryFacade

    @Before
    @CallSuper
    open fun setup() {
        chatDatabase = createRoomDB(testCoroutines.ioDispatcher)
        repositoryFacade = createRepositoryFacade()
    }

    @After
    fun tearDown() {
        chatDatabase.close()
    }

    private fun createRepositoryFacade(): RepositoryFacade {
        return RepositoryFacadeBuilder {
            context(ApplicationProvider.getApplicationContext())
            currentUser(currentUser)
            scope(testCoroutines.scope)
            defaultConfig(Config())
            database(chatDatabase)
            setOfflineEnabled(false)
        }.build()
    }
}
