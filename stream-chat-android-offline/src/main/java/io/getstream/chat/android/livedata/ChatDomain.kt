package io.getstream.chat.android.livedata

import android.content.Context
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.service.sync.BackgroundSyncConfig
import io.getstream.chat.android.livedata.service.sync.NotificationConfigStore.Companion.NotificationConfigUnavailable
import io.getstream.chat.android.livedata.service.sync.SyncProvider
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy

/**
 * The ChatDomain is the main entry point for all livedata & offline operations on chat
 *
 * Use cases are exposed via chatDomain.useCases
 */
public interface ChatDomain {
    /** The current user object */
    public var currentUser: User

    /** if offline is enabled */
    public var offlineEnabled: Boolean

    /** if we want to track user presence */
    public var userPresence: Boolean

    /** if the client connection has been initialized */
    public val initialized: LiveData<Boolean>

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    public val online: LiveData<Boolean>

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    public val totalUnreadCount: LiveData<Int>

    /**
     * the number of unread channels for the current user
     */
    public val channelUnreadCount: LiveData<Int>

    /**
     * The error event livedata object is triggered when errors in the underlying components occure.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.observe(this, EventObserver {
     *       // create a toast
     *   })
     *
     */
    public val errorEvents: LiveData<Event<ChatError>>

    /**
     * list of users that you've muted
     */
    public val muted: LiveData<List<Mute>>

    /**
     * if the current user is banned or not
     */
    public val banned: LiveData<Boolean>

    /** The retry policy for retrying failed requests */
    public var retryPolicy: RetryPolicy

    /** a helper object which lists all the initialized use cases for the chat domain */
    public var useCases: UseCaseHelper

    public suspend fun disconnect()
    public fun isOnline(): Boolean
    public fun isOffline(): Boolean
    public fun isInitialized(): Boolean
    public fun getActiveQueries(): List<QueryChannelsController>
    public fun clean()
    public fun getChannelConfig(channelType: String): Config
    public fun getVersion(): String

    public data class Builder(
        private var appContext: Context,
        private var client: ChatClient,
        private var user: User? = null
    ) {

        public constructor(client: ChatClient, user: User?) : this(client.appContext, client, user)

        private val factory: ChatDomainFactory = ChatDomainFactory()

        private var database: ChatDatabase? = null

        private var userPresence: Boolean = false
        private var storageEnabled: Boolean = true
        private var recoveryEnabled: Boolean = true
        private var backgroundSyncConfig: BackgroundSyncConfig = BackgroundSyncConfig.UNAVAILABLE
        private var notificationConfig: NotificationConfig = NotificationConfigUnavailable
        private val syncModule by lazy { SyncProvider(appContext) }

        internal fun database(db: ChatDatabase): Builder {
            this.database = db
            return this
        }

        public fun setUser(user: User) {
            this.user = user
        }

        public fun setConfig(config: ChatClient.OfflineConfig) {
            userPresence = config.userPresence
            storageEnabled = config.storageEnabled
            // TODO: finish this method
        }

        public fun backgroundSyncEnabled(apiKey: String, userToken: String): Builder {
            // TODO: Consider exposing apiKey and userToken by ChatClient to make this public function more friendly
            if (apiKey.isEmpty() || user?.id.isNullOrEmpty() || userToken.isEmpty()) {
                this.backgroundSyncConfig = BackgroundSyncConfig.UNAVAILABLE
                throw IllegalArgumentException("apiKey, userToken must not be empty")
            } else {
                this.backgroundSyncConfig = BackgroundSyncConfig(apiKey, user!!.id, userToken)
            }
            return this
        }

        public fun backgroundSyncDisabled(): Builder {
            this.backgroundSyncConfig = BackgroundSyncConfig.UNAVAILABLE
            return this
        }

        public fun offlineEnabled(): Builder {
            this.storageEnabled = true
            return this
        }

        public fun offlineDisabled(): Builder {
            this.storageEnabled = false
            return this
        }

        public fun recoveryEnabled(): Builder {
            this.recoveryEnabled = true
            return this
        }

        public fun recoveryDisabled(): Builder {
            this.recoveryEnabled = false
            return this
        }

        public fun userPresenceEnabled(): Builder {
            this.userPresence = true
            return this
        }

        public fun userPresenceDisabled(): Builder {
            this.userPresence = false
            return this
        }

        public fun notificationConfig(notificationConfig: NotificationConfig): Builder {
            this.notificationConfig = notificationConfig
            return this
        }

        public fun build(): ChatDomain {
            if (backgroundSyncConfig != BackgroundSyncConfig.UNAVAILABLE) {
                storeBackgroundSyncConfig(backgroundSyncConfig)
                storeNotificationConfig(notificationConfig)
            }
            instance = buildImpl()
            return instance
        }

        internal fun buildImpl(): ChatDomainImpl {
            val u = checkNotNull(user) {"user needs to be set before calling build"}
            return factory.create(appContext, client, u, database, storageEnabled, userPresence, recoveryEnabled)
        }

        private fun storeBackgroundSyncConfig(backgroundSyncConfig: BackgroundSyncConfig) {
            if (BackgroundSyncConfig.UNAVAILABLE != backgroundSyncConfig) {
                syncModule.encryptedBackgroundSyncConfigStore.apply {
                    put(backgroundSyncConfig)
                }
            }
        }

        private fun storeNotificationConfig(notificationConfig: NotificationConfig) {
            if (NotificationConfigUnavailable != notificationConfig) {
                syncModule.notificationConfigStore.apply {
                    put(notificationConfig)
                }
            }
        }
    }

    public companion object {
        private lateinit var instance: ChatDomain

        @JvmStatic
        public fun instance(): ChatDomain {
            return instance
        }
    }
}
