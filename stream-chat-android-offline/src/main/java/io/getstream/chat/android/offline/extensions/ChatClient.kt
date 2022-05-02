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

package io.getstream.chat.android.offline.extensions

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
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryProvider
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.extensions.internal.isEphemeral
import io.getstream.chat.android.offline.extensions.internal.logic
import io.getstream.chat.android.offline.extensions.internal.requestsAsState
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.offline.utils.internal.validateCidWithResult
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
    get() = GlobalMutableState.getOrCreate()

/**
 * Performs [ChatClient.queryChannels] under the hood and returns [QueryChannelsState] associated with the query.
 * The [QueryChannelsState] cannot be created before connecting the user therefore, the method returns a StateFlow
 * that emits a null when the user has not been connected yet and the new value every time the user changes.
 *
 * @param request The request's parameters combined into [QueryChannelsRequest] class.
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return A StateFlow object that emits a null when the user has not been connected yet and the new [QueryChannelsState] when the user changes.
 */
@JvmOverloads
public fun ChatClient.queryChannelsAsState(
    request: QueryChannelsRequest,
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): StateFlow<QueryChannelsState?> {
    return getStateOrNull(coroutineScope) {
        requestsAsState(coroutineScope).queryChannels(request)
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
        requestsAsState(coroutineScope).watchChannel(cid, messageLimit)
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

                val repositoryProvider = RepositoryProvider.get()
                val channelRepository = repositoryProvider.get(MessageRepository::class.java)

                val (channelType, channelId) = message.cid.cidToTypeAndId()
                logic.channel(channelType = channelType, channelId = channelId).removeLocalMessage(message)
                channelRepository.deleteChannelMessage(message)

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
                    val repositoryProvider = RepositoryProvider.get()

                    repositoryProvider.get(MessageRepository::class.java).selectMessage(messageId)?.let(::Result)
                        ?: Result(ChatError("Error while fetching message from backend. Message id: $messageId"))
                } catch (exception: Exception) {
                    Result.error(exception)
                }
            }
        } else {
            cidValidationResult
        }
    }
}
