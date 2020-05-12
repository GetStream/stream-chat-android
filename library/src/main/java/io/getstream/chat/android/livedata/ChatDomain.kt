package io.getstream.chat.android.livedata

import android.content.Context
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy

/**
 * The ChatDomain is the main entry point for all livedata & offline operations on chat
 *
 * Use cases are exposed via chatDomain.useCases
 */
interface ChatDomain {
    /** The current user object */
    var currentUser: User
    /** if offline is enabled */
    var offlineEnabled: Boolean
    /** if we want to track user presence */
    var userPresence: Boolean
    /** if the client connection has been initialized */
    val initialized: LiveData<Boolean>
    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    val online: LiveData<Boolean>
    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    val totalUnreadCount: LiveData<Int>
    /**
     * the number of unread channels for the current user
     */
    val channelUnreadCount: LiveData<Int>
    /**
     * The error event livedata object is triggered when errors in the underlying components occure.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.observe(this, EventObserver {
     *       // create a toast
     *   })
     *
     */
    val errorEvents: LiveData<Event<ChatError>>
    /**
     * list of users that you've muted
     */
    val mutedUsers: LiveData<List<Mute>>
    /**
     * if the current user is banned or not
     */
    val banned: LiveData<Boolean>
    /** The retry policy for retrying failed requests */
    var retryPolicy: RetryPolicy
    /** a helper object which lists all the initialized use cases for the chat domain */
    var useCases: UseCaseHelper

    suspend fun disconnect()
    fun isOnline(): Boolean
    fun isOffline(): Boolean
    fun isInitialized(): Boolean
    fun getActiveQueries(): List<QueryChannelsControllerImpl>
    fun clean()
    fun getChannelConfig(channelType: String): Config

    data class Builder(
        private var appContext: Context,
        private var client: ChatClient,
        private var user: User
    ) {

        private var database: ChatDatabase? = null

        private var userPresence: Boolean = false
        private var offlineEnabled: Boolean = true
        private var recoveryEnabled: Boolean = true

        fun database(db: ChatDatabase): Builder {
            this.database = db
            return this
        }

        fun offlineEnabled(): Builder {
            this.offlineEnabled = true
            return this
        }

        fun offlineDisabled(): Builder {
            this.offlineEnabled = false
            return this
        }

        fun recoveryEnabled(): Builder {
            this.recoveryEnabled = true
            return this
        }

        fun recoveryDisabled(): Builder {
            this.recoveryEnabled = false
            return this
        }

        fun userPresenceEnabled(): Builder {
            this.userPresence = true
            return this
        }

        fun userPresenceDisabled(): Builder {
            this.userPresence = false
            return this
        }

        internal fun buildImpl(): ChatDomainImpl {
            val chatDomain = ChatDomainImpl(appContext, client, user, offlineEnabled, userPresence, recoveryEnabled, database)

            return chatDomain
        }

        fun build(): ChatDomain {
            val chatDomainImpl = buildImpl()
            val chatDomain: ChatDomain = chatDomainImpl
            instance = chatDomain
            return chatDomain
        }
    }

    companion object {

        private lateinit var instance: ChatDomain

        @JvmStatic
        fun instance(): ChatDomain {
            return instance
        }
    }

    fun getVersion(): String

}
