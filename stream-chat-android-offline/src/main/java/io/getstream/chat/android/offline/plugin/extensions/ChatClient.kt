@file:JvmName("ChatClientExtensions")

package io.getstream.chat.android.offline.plugin.extensions

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.internal.extensions.isEphemeral
import io.getstream.chat.android.offline.internal.repository.RepositoryFacade
import io.getstream.chat.android.offline.internal.utils.validateCidWithResult
import io.getstream.chat.android.offline.plugin.extensions.internal.logic
import io.getstream.chat.android.offline.plugin.extensions.internal.requestsAsState
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.CoroutineScope

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 */
public val ChatClient.state: StateRegistry
    get() = requireNotNull(StateRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

/**
 * [GlobalState] instance that contains information about the current user, unreads, etc.
 */
public val ChatClient.globalState: GlobalState
    get() = GlobalMutableState.getOrCreate()

/**
 * Same class of ChatClient.queryChannels, but provides the result as [QueryChannelsState]
 *
 * @param request [QueryChannelsRequest]
 * @return [QueryChannelsRequest]
 */
@JvmOverloads
public fun ChatClient.queryChannelsAsState(
    request: QueryChannelsRequest,
    coroutineScope: CoroutineScope = state.scope,
): QueryChannelsState {
    return requestsAsState(coroutineScope).queryChannels(request)
}

/**
 * Same class of ChatClient.queryChannel, but provides the result as [ChannelState]
 *
 * @param cid
 * @return [ChannelState]
 */
@JvmOverloads
public fun ChatClient.watchChannelAsState(
    cid: String,
    limit: Int,
    coroutineScope: CoroutineScope = state.scope,
): ChannelState {
    return requestsAsState(coroutineScope).watchChannel(cid, limit)
}

/**
 * Same class of ChatClient.getReplies, but provides the result as [ThreadState]
 *
 * @param cid
 * @return [ThreadState]
 */
@JvmOverloads
public fun ChatClient.getRepliesAsState(
    cid: String,
    limit: Int,
    coroutineScope: CoroutineScope = state.scope,
): ThreadState {
    return requestsAsState(coroutineScope).getReplies(cid, limit)
}

/**
 * Set the reply state for the channel.
 *
 * @param cid CID of the channel where reply state is being set.
 * @param message The message we want reply to. The null value means dismiss reply state.
 *
 * @return Executable async [Call].
 */
@CheckResult
public fun ChatClient.setMessageForReply(cid: String, message: Message?): Call<Unit> {
    return CoroutineCall(state.scope) {
        val cidValidationResult = validateCidWithResult<Unit>(cid)

        if (cidValidationResult.isSuccess) {
            val (channelType, channelId) = cid.cidToTypeAndId()
            state.channel(channelType = channelType, channelId = channelId).toMutableState().run {
                _repliedMessage.value = message
            }
            Result(Unit)
        } else {
            cidValidationResult
        }
    }
}

/**
 * Downloads the selected attachment to the "Download" folder in the public external storage directory.
 *
 * @param attachment The attachment to download.
 *
 * @return Executable async [Call] downloading attachment.
 */
@CheckResult
public fun ChatClient.downloadAttachment(context: Context, attachment: Attachment): Call<Unit> {
    return CoroutineCall(state.scope) {
        try {
            val logger = ChatLogger.get("DownloadAttachment")
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val url = attachment.assetUrl ?: attachment.imageUrl
            val subPath = attachment.name ?: attachment.title

            logger.logD("Downloading attachment. Name: $subPath, Url: $url")

            downloadManager.enqueue(
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(subPath)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            )
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.error(exception)
        }
    }
}

/**
 * Loads older messages for the channel.
 *
 * @param cid The full channel id i.e. "messaging:123".
 * @param messageLimit How many new messages to load.
 *
 * @return The channel wrapped in [Call]. This channel contains older requested messages.
 */
public fun ChatClient.loadOlderMessages(cid: String, messageLimit: Int): Call<Channel> {
    return CoroutineCall(state.scope) {
        val cidValidationResult = validateCidWithResult<Channel>(cid)

        if (cidValidationResult.isSuccess) {
            val (channelType, channelId) = cid.cidToTypeAndId()
            logic.channel(channelType = channelType, channelId = channelId)
                .loadOlderMessages(messageLimit = messageLimit)
        } else {
            cidValidationResult
        }
    }
}

/**
 * Cancels the message of "ephemeral" type.
 * Removes the message from local storage and state.
 *
 * @param message The `ephemeral` message to cancel.
 *
 * @return Executable async [Call] responsible for canceling ephemeral message.
 */
public fun ChatClient.cancelEphemeralMessage(message: Message): Call<Boolean> {
    return CoroutineCall(state.scope) {
        val cidValidationResult = validateCidWithResult<Boolean>(message.cid)

        if (cidValidationResult.isSuccess) {
            try {
                require(message.isEphemeral()) { "Only ephemeral message can be canceled" }

                val repos = RepositoryFacade.get()
                val (channelType, channelId) = message.cid.cidToTypeAndId()
                logic.channel(channelType = channelType, channelId = channelId).removeLocalMessage(message)
                repos.deleteChannelMessage(message)

                Result.success(true)
            } catch (exception: Exception) {
                Result.error(exception)
            }
        } else {
            cidValidationResult
        }
    }
}

/**
 * Loads message for a given message id and channel id.
 *
 * @param cid The full channel id i. e. messaging:123.
 * @param messageId The id of the message.
 * @param olderMessagesOffset How many new messages to load before the requested message.
 * @param newerMessagesOffset How many new messages to load after the requested message.
 *
 * @return Executable async [Call] responsible for loading a message.
 */
@CheckResult
public fun ChatClient.loadMessageById(
    cid: String,
    messageId: String,
    olderMessagesOffset: Int,
    newerMessagesOffset: Int,
): Call<Message> {
    return CoroutineCall(state.scope) {
        val cidValidationResult = validateCidWithResult<Message>(cid)

        if (cidValidationResult.isSuccess) {
            val result = getMessage(messageId).await()

            if (result.isSuccess) {
                val message = result.data()
                val (channelType, channelId) = cid.cidToTypeAndId()

                logic.channel(channelType = channelType, channelId = channelId).run {
                    storeMessageLocally(listOf(message))
                    upsertMessages(listOf(message))
                    loadOlderMessages(newerMessagesOffset, messageId)
                    loadNewerMessages(messageId, olderMessagesOffset)
                }
                result
            } else {
                try {
                    RepositoryFacade.get().selectMessage(messageId)?.let { message ->
                        Result(message)
                    } ?: Result(ChatError("Error while fetching message from backend. Message id: $messageId"))
                } catch (exception: Exception) {
                    Result.error(exception)
                }
            }
        } else {
            cidValidationResult
        }
    }
}
