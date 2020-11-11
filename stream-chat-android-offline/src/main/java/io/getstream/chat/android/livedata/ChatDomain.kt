package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.controller.QueryChannelsController
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

    /** the current user on the chatDomain object, same as client.getCurrentUser() */
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


    public val typingUpdates: LiveData<Pair<String, List<User>>>

    /** a helper object which lists all the initialized use cases for the chat domain */
    public val useCases: UseCaseHelper

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

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client, null)

        public constructor(client: ChatClient, user: User?, appContext: Context) : this(appContext, client, user)

        private var database: ChatDatabase? = null

        private var userPresence: Boolean = false
        private var storageEnabled: Boolean = true
        private var recoveryEnabled: Boolean = true
        private var backgroundSyncEnabled: Boolean = true
        private var notificationConfig: NotificationConfig = NotificationConfigUnavailable
        private val syncModule by lazy { SyncProvider(appContext) }

        internal fun database(db: ChatDatabase): Builder {
            this.database = db
            return this
        }

        public fun enableBackgroundSync(): Builder {
            backgroundSyncEnabled = true
            return this
        }

        public fun disableBackgroundSync(): Builder {
            backgroundSyncEnabled = false
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

        // TODO: do we even need this, or is it better to have it at the low level client?
        public fun notificationConfig(notificationConfig: NotificationConfig): Builder {
            this.notificationConfig = notificationConfig
            return this
        }

        public fun build(): ChatDomain {

            storeNotificationConfig(notificationConfig)

            ChatDomain.instance = buildImpl()

            return ChatDomain.instance()
        }

        internal fun buildImpl(): ChatDomainImpl {
            val handler = Handler(Looper.getMainLooper())
            return ChatDomainImpl(client, user, database, handler, storageEnabled, userPresence, recoveryEnabled, backgroundSyncEnabled, appContext)
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
        private var instance: ChatDomain? = null

        @JvmStatic
        public fun instance(): ChatDomain = instance
            ?: throw IllegalStateException("ChatDomain.Builder::build() must be called before obtaining ChatDomain instance")

        public val isInitialized: Boolean
            get() = instance != null
    }
}
