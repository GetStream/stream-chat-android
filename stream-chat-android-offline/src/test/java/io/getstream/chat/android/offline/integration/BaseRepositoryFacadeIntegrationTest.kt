package io.getstream.chat.android.offline.integration

import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.offline.repository.RepositoryFacade
import io.getstream.chat.android.client.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.testDatabaseBuilder
import io.getstream.chat.android.test.TestCoroutineRule
import org.junit.Before
import org.junit.Rule

internal open class BaseRepositoryFacadeIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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
}
