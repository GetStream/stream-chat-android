package io.getstream.chat.android.livedata

import android.content.Context
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.ChatDomain.Builder as OfflineChatDomainBuilder

/**
 * The ChatDomain is the main entry point for all livedata & offline operations on chat.
 */
public sealed interface ChatDomain {

    // region use-case functions

    // updating channel data

    // end region

    public data class Builder(
        private val appContext: Context,
        private val client: ChatClient,
    ) {

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client)

        private val offlineChatDomainBuilder: OfflineChatDomainBuilder = OfflineChatDomainBuilder(appContext, client)

        public fun enableBackgroundSync(): Builder = apply {
            offlineChatDomainBuilder.enableBackgroundSync()
        }

        public fun disableBackgroundSync(): Builder = apply {
            offlineChatDomainBuilder.disableBackgroundSync()
        }

        public fun recoveryEnabled(): Builder = apply {
            offlineChatDomainBuilder.recoveryEnabled()
        }

        public fun recoveryDisabled(): Builder = apply {
            offlineChatDomainBuilder.recoveryDisabled()
        }

        public fun userPresenceEnabled(): Builder = apply {
            offlineChatDomainBuilder.userPresenceEnabled()
        }

        public fun userPresenceDisabled(): Builder = apply {
            offlineChatDomainBuilder.userPresenceDisabled()
        }

        public fun build(): ChatDomain {
            instance?.run {
                Log.e(
                    "Chat",
                    "[ERROR] You have just re-initialized ChatDomain, old configuration has been overridden [ERROR]"
                )
            }
            instance = buildImpl()

            return instance()
        }

        internal fun buildImpl(): ChatDomainImpl {
            return ChatDomainImpl()
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
