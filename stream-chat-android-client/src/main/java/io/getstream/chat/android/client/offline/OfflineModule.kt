package io.getstream.chat.android.client.offline

import android.content.Context
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.offline.repository.RepositoryFacade
import io.getstream.chat.android.client.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.client.offline.repository.database.ChatDatabase
import kotlinx.coroutines.CoroutineScope

public class OfflineModule(
    private val scope: CoroutineScope,
    private val appContext: Context,
    private val defaultConfig: Config,
) {

    internal var database: ChatDatabase? = null

    private var repositoryFacade: RepositoryFacade = createNoOpsRepo()

    public fun buildRepo(user: User, offlineEnabled: Boolean = true) {
        repositoryFacade = RepositoryFacadeBuilder {
            context(appContext)
            database(database)
            currentUser(user)
            scope(scope)
            defaultConfig(defaultConfig)
            setOfflineEnabled(offlineEnabled)
        }.build()
    }

    private fun createNoOpsRepo() = RepositoryFacadeBuilder {
        context(appContext)
        scope(scope)
        defaultConfig(defaultConfig)
        setOfflineEnabled(false)
    }.build()
}
