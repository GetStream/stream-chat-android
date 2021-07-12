package io.getstream.chat.android.client.offline

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.offline.repository.RepositoryFacade
import io.getstream.chat.android.client.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

public class OfflineModule(private val appContext: Context, private val persist: Boolean) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + DispatcherProvider.IO)
    private val defaultConfig = Config(isConnectEvents = true, isMutes = true)

    public var repositoryFacade: RepositoryFacade = createNoOpsRepo()
        private set

    internal fun init(chatClient: ChatClient) {
        chatClient.preSetUserListeners.add(::buildRepo)
        chatClient.disconnectListeners.add { dispose() }
    }

    private fun buildRepo(user: User) {
        repositoryFacade = RepositoryFacadeBuilder {
            context(appContext)
            currentUser(user)
            scope(scope)
            defaultConfig(defaultConfig)
            setOfflineEnabled(persist)
        }.build()
    }

    private fun dispose() {
        job.cancelChildren()
    }

    private fun createNoOpsRepo() = RepositoryFacadeBuilder {
        context(appContext)
        scope(scope)
        defaultConfig(defaultConfig)
        setOfflineEnabled(false)
    }.build()
}
