/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("ChatClientExtensions")

package io.getstream.chat.android.state.extensions

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.internal.validateCidWithResult
import io.getstream.chat.android.client.utils.message.isEphemeral
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.internal.logic
import io.getstream.chat.android.state.extensions.internal.requestsAsState
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.internal.ConfigSingleton
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 * The instance is being initialized after connecting the user!
 *
 * @throws IllegalArgumentException If the state was not initialized yet.
 */
public val ChatClient.state: StateRegistry
    @Throws(IllegalArgumentException::class)
    get() = requireNotNull(StateRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

/**
 * [GlobalState] instance that contains information about the current user, unreads, etc.
 */
public val ChatClient.globalState: GlobalState
    get() = GlobalMutableState.get(clientState)

/**
 * [StatePluginConfig] instance used to configure [io.getstream.chat.android.state.plugin.internal.StatePlugin]
 * or null if the config doesn't exist.
 */
public val ChatClient.stateConfig: StatePluginConfig?
    get() = ConfigSingleton.statePluginConfig

/**
 * Performs [ChatClient.queryChannels] under the hood and returns [QueryChannelsState] associated with the query.
 * The [QueryChannelsState] cannot be created before connecting the user therefore, the method returns a StateFlow
 * that emits a null when the user has not been connected yet and the new value every time the user changes.
 *
 * You can pass option [chatEventHandlerFactory] parameter which will be associated with this query channels request.
 *
 * @see [ChatEventHandler]
 *
 * @param request The request's parameters combined into [QueryChannelsRequest] class.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return A StateFlow object that emits a null when the user has not been connected yet and the new [QueryChannelsState] when the user changes.
 */
@JvmOverloads
public fun ChatClient.queryChannelsAsState(
    request: QueryChannelsRequest,
    chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(clientState),
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): StateFlow<QueryChannelsState?> {
    return getStateOrNull(coroutineScope) {
        requestsAsState(coroutineScope).queryChannels(request, chatEventHandlerFactory)
    }
}

/**
 * Performs [ChatClient.queryChannel] with watch = true under the hood and returns [ChannelState] associated with the query.
 * The [ChannelState] cannot be created before connecting the user therefore, the method returns a StateFlow
 * that emits a null when the user has not been connected yet and the new value every time the user changes.
 *
 * @param cid The full channel id, i.e. "messaging:123"
 * @param messageLimit The number of messages that will be initially loaded.
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return A StateFlow object that emits a null when the user has not been connected yet and the new [ChannelState] when the user changes.
 */
@JvmOverloads
public fun ChatClient.watchChannelAsState(
    cid: String,
    messageLimit: Int,
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): StateFlow<ChannelState?> {
    return getStateOrNull(coroutineScope) {
        requestsAsState(coroutineScope).watchChannel(cid, messageLimit, stateConfig?.userPresence == true)
    }
}

/**
 * Same class of ChatClient.getReplies, but provides the result as [ThreadState]
 *
 * @param messageId The ID of the original message the replies were made to.
 * @param messageLimit The number of messages that will be initially loaded.
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return [ThreadState]
 */
@JvmOverloads
public fun ChatClient.getRepliesAsState(
    messageId: String,
    messageLimit: Int,
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): ThreadState {
    return requestsAsState(coroutineScope).getReplies(messageId, messageLimit)
}

/**
 * Provides an ease-of-use piece of functionality that checks if the user is available or not. If it's not, we don't emit
 * any state, but rather return an empty StateFlow.
 *
 * If the user is set, we fetch the state using the provided operation and provide it to the user.
 */
private fun <T> ChatClient.getStateOrNull(
    coroutineScope: CoroutineScope,
    producer: () -> T,
): StateFlow<T?> {
    return globalState.user.map { it?.id }.distinctUntilChanged().map { userId ->
        if (userId == null) {
            null
        } else {
            producer()
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
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

        when (val cidValidationResult = validateCidWithResult(cid)) {
            is Result.Success -> {
                val (channelType, channelId) = cid.cidToTypeAndId()
                state.mutableChannel(channelType = channelType, channelId = channelId).run {
                    setRepliedMessage(message)
                }
                Result.Success(Unit)
            }
            is Result.Failure -> cidValidationResult
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
            val logger = StreamLog.getLogger("Chat:DownloadAttachment")
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val url = attachment.assetUrl ?: attachment.imageUrl
            val subPath = attachment.name ?: attachment.title

            logger.d { "Downloading attachment. Name: $subPath, Url: $url" }

            downloadManager.enqueue(
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(subPath)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            )
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Failure(ChatError.ThrowableError(message = "Could not download the attachment", cause = exception))
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
        when (val cidValidationResult = validateCidWithResult(cid)) {
            is Result.Success -> {
                val (channelType, channelId) = cid.cidToTypeAndId()
                logic.channel(channelType = channelType, channelId = channelId)
                    .loadOlderMessages(messageLimit = messageLimit)
            }
            is Result.Failure -> cidValidationResult
        }
    }
}

public fun ChatClient.loadNewerMessages(
    channelCid: String,
    baseMessageId: String,
    messageLimit: Int,
): Call<Channel> {
    return CoroutineCall(state.scope) {
        when (val cidValidationResult = validateCidWithResult(channelCid)) {
            is Result.Success -> {
                val (channelType, channelId) = channelCid.cidToTypeAndId()
                logic.channel(channelType = channelType, channelId = channelId)
                    .loadNewerMessages(messageId = baseMessageId, limit = messageLimit)
            }
            is Result.Failure -> cidValidationResult
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
        when (val cidValidationResult = validateCidWithResult(message.cid)) {
            is Result.Success -> {
                try {
                    require(message.isEphemeral()) { "Only ephemeral message can be canceled" }
                    logic.channelFromMessage(message)?.deleteMessage(message)
                    logic.threadFromMessage(message)?.removeLocalMessage(message)
                    repositoryFacade.deleteChannelMessage(message)

                    Result.Success(true)
                } catch (exception: Exception) {
                    Result.Failure(
                        ChatError.ThrowableError(
                            message = "Could not cancel ephemeral message",
                            cause = exception,
                        ),
                    )
                }
            }
            is Result.Failure -> cidValidationResult
        }
    }
}

/**
 * Loads message for a given message id and channel id.
 *
 * @param cid The full channel id i. e. messaging:123.
 * @param messageId The id of the message.
 *
 * @return Executable async [Call] responsible for loading a message.
 */
@CheckResult
public fun ChatClient.loadMessageById(
    cid: String,
    messageId: String,
): Call<Message> {
    return CoroutineCall(state.scope) {
        loadMessageByIdInternal(cid, messageId)
    }
}

private suspend fun ChatClient.loadMessageByIdInternal(
    cid: String,
    messageId: String,
): Result<Message> {
    val cidValidationResult = validateCidWithResult(cid)

    if (cidValidationResult is Result.Failure) {
        return cidValidationResult
    }

    val (channelType, channelId) = cid.cidToTypeAndId()
    val result = logic.channel(channelType = channelType, channelId = channelId)
        .loadMessagesAroundId(messageId)

    return when (result) {
        is Result.Success -> {
            val message = result.value.messages.firstOrNull { message ->
                message.id == messageId
            }

            if (message != null) {
                result.map { message }
            } else {
                Result.Failure(ChatError.GenericError("The message could not be found."))
            }
        }
        is Result.Failure -> Result.Failure(
            ChatError.GenericError("Error while fetching messages from backend. Messages around id: $messageId")
        )
    }
}

/**
 * Loads the newest messages of a channel.
 *
 * @param cid The full channel id i. e. messaging:123.
 * @param messageLimit The number of messages to be loaded.
 * @param userPresence Flag to determine if the SDK is going to receive UserPresenceChanged events.
 * Used by the SDK to indicate if the user is online or not.
 *
 * @return Executable async [Call] responsible for loading the newest messages.
 */
@CheckResult
public fun ChatClient.loadNewestMessages(
    cid: String,
    messageLimit: Int,
    userPresence: Boolean = true,
): Call<Channel> {
    return CoroutineCall(state.scope) {
        when (val cidValidationResult = validateCidWithResult(cid)) {
            is Result.Success -> {
                val (channelType, channelId) = cid.cidToTypeAndId()
                logic.channel(channelType = channelType, channelId = channelId)
                    .watch(messageLimit, userPresence)
            }
            is Result.Failure -> Result.Failure(cidValidationResult.value)
        }
    }
}
