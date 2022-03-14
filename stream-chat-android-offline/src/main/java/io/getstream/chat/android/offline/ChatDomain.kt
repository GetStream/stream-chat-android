package io.getstream.chat.android.offline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState

/**
 * The ChatDomain is the main entry point for all flow & offline operations on chat.
 */
public sealed interface ChatDomain {

    /** if we want to track user presence */
    public var userPresence: Boolean

    public data class Builder(
        private val appContext: Context,
        private val client: ChatClient,
    ) {

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client)

        private var handler: Handler = Handler(Looper.getMainLooper())

        private var userPresence: Boolean = false
        private var storageEnabled: Boolean = true
        private var recoveryEnabled: Boolean = true
        private var backgroundSyncEnabled: Boolean = true
        private var globalMutableState = GlobalMutableState.getOrCreate()

        @VisibleForTesting
        internal fun handler(handler: Handler) = apply {
            this.handler = handler
        }

        public fun enableBackgroundSync(): Builder {
            backgroundSyncEnabled = true
            return this
        }

        public fun disableBackgroundSync(): Builder {
            backgroundSyncEnabled = false
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

        internal fun globalMutableState(globalMutableState: GlobalMutableState): Builder = apply {
            this.globalMutableState = globalMutableState
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

        @SuppressLint("VisibleForTests")
        internal fun buildImpl(): ChatDomainImpl {
            return ChatDomainImpl(
                client,
                handler,
                recoveryEnabled,
                userPresence,
                backgroundSyncEnabled,
                appContext,
            )
        }
    }

    public companion object {
        @VisibleForTesting
        internal var instance: ChatDomain? = null

        @JvmStatic
        public fun instance(): ChatDomain = instance
            ?: throw IllegalStateException("ChatDomain.Builder::build() must be called before obtaining ChatDomain instance")

        public val isInitialized: Boolean
            get() = instance != null
    }
}
