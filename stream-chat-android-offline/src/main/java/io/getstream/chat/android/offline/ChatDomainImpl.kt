package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.service.sync.OfflineSyncFirebaseMessagingHandler

/**
 * The Chat Domain exposes StateFlow objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * chatDomain.channel(type, id) returns a controller object with channel specific state flow objects
 * chatDomain.queryChannels(query) returns a state flow object for the specific queryChannels query
 *
 * chatDomain.online state flow object indicates if you're online or not
 * chatDomain.totalUnreadCount state flow object returns the current unread count for this user
 * chatDomain.muted the list of muted users
 * chatDomain.banned if the current user is banned or not
 * chatDomain.channelUnreadCount state flow object returns the number of unread channels for this user
 * chatDomain.errorEvents events for errors that happen while interacting with the chat
 *
 */
internal class ChatDomainImpl internal constructor(
    internal var client: ChatClient,
    @VisibleForTesting
    private val mainHandler: Handler,
    internal var recoveryEnabled: Boolean = true,
    override var userPresence: Boolean = false,
    internal var backgroundSyncEnabled: Boolean = false,
    internal var appContext: Context,
) : ChatDomain {

    private val offlineSyncFirebaseMessagingHandler = OfflineSyncFirebaseMessagingHandler()

    init {
        // Todo: Move this!!
        if (backgroundSyncEnabled) {
            client.setPushNotificationReceivedListener { channelType, channelId ->
                offlineSyncFirebaseMessagingHandler.syncMessages(appContext, "$channelType:$channelId")
            }
        }
    }
// region use-case functions

// end region
}
