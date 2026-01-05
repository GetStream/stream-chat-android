/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api.state

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.StateConfig
import io.getstream.chat.android.client.api.event.ChatEventHandler
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.internal.state.extensions.internal.logic
import io.getstream.chat.android.client.internal.state.extensions.internal.parseAttachmentNameFromUrl
import io.getstream.chat.android.client.internal.state.extensions.internal.requestsAsState
import io.getstream.chat.android.client.internal.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin
import io.getstream.chat.android.client.utils.internal.validateCidWithResult
import io.getstream.chat.android.client.utils.message.isEphemeral
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Message
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "Chat:Client-StatePlugin"

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 * The instance is being initialized after connecting the user!
 *
 * @throws IllegalArgumentException If the state was not initialized yet.
 */
public val ChatClient.state: StateRegistry
    @Throws(IllegalArgumentException::class)
    get() = resolveDependency<StatePlugin, StateRegistry>()

/**
 * [GlobalState] instance that contains information about the current user, unreads, etc.
 *
 * @throws IllegalArgumentException If the GlobalState was not initialized yet.
 */
public val ChatClient.globalState: GlobalState
    @Throws(IllegalArgumentException::class)
    get() = resolveDependency<StatePlugin, GlobalState>()

/**
 * Retrieves a [Flow] holding the [GlobalState] object, which emits only if the user is connected, and the [ChatClient]
 * is in [InitializationState.COMPLETE] state.
 */
public val ChatClient.globalStateFlow: Flow<GlobalState>
    get() = clientState.initializationState
        .onEach {
            if (it == InitializationState.NOT_INITIALIZED) {
                StreamLog.w(TAG) { "ChatClient::connectUser() must be called to ensure the globalState is initialized" }
            }
        }
        .filter { it == InitializationState.COMPLETE }
        .map { globalState }

/**
 * [StateConfig] instance used to configure the [StatePlugin].
 *
 * @throws IllegalArgumentException If the StatePluginConfig was not initialized yet.
 */
internal val ChatClient.stateConfig: StateConfig
    get() = resolveDependency<StreamStatePluginFactory, StateConfig>()

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
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create
 * [ChatEventHandler].
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return A StateFlow object that emits a null when the user has not been connected yet and the new
 * [QueryChannelsState] when the user changes.
 */
@JvmOverloads
public fun ChatClient.queryChannelsAsState(
    request: QueryChannelsRequest,
    chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(clientState),
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): StateFlow<QueryChannelsState?> {
    StreamLog.d(TAG) { "[queryChannelsAsState] request: $request" }
    return getStateOrNull(coroutineScope) {
        requestsAsState(coroutineScope).queryChannels(request, chatEventHandlerFactory)
    }
}

/**
 * Performs [ChatClient.queryChannel] with watch = true under the hood and returns [ChannelState] associated with the
 * query.
 * The [ChannelState] cannot be created before connecting the user therefore, the method returns a StateFlow
 * that emits a null when the user has not been connected yet and the new value every time the user changes.
 *
 * @param cid The full channel id, i.e. "messaging:123"
 * @param messageLimit The number of messages that will be initially loaded.
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return A StateFlow object that emits a null when the user has not been connected yet and the new [ChannelState] when
 * the user changes.
 */
@JvmOverloads
public fun ChatClient.watchChannelAsState(
    cid: String,
    messageLimit: Int,
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): StateFlow<ChannelState?> {
    StreamLog.i(TAG) { "[watchChannelAsState] cid: $cid, messageLimit: $messageLimit" }
    return getStateOrNull(coroutineScope) {
        requestsAsState(coroutineScope).watchChannel(cid, messageLimit, stateConfig.userPresence)
    }
}

/**
 * Performs [ChatClient.queryThreadsResult] under the hood and returns [QueryThreadsState].
 * The [QueryThreadsState] cannot be created before connecting the user therefore, the method returns a StateFlow
 * that emits a null when the user has not been connected yet and the new value every time the user changes.
 *
 * @param request The [QueryThreadsRequest] used to perform the query threads operation.
 * @return A [StateFlow] emitting changes in the [QueryThreadsState].
 */
public fun ChatClient.queryThreadsAsState(
    request: QueryThreadsRequest,
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): StateFlow<QueryThreadsState?> {
    return getStateOrNull(coroutineScope) {
        requestsAsState(coroutineScope).queryThreads(request)
    }
}

/**
 * Same class of ChatClient.getReplies, but provides the result as [ThreadState]
 *
 * @param messageId The ID of the original message the replies were made to.
 * @param messageLimit The number of messages that will be initially loaded.
 * @param olderToNewer The flag that determines the order of the messages.
 * @param coroutineScope The [CoroutineScope] used for executing the request.
 *
 * @return [ThreadState]
 */
@JvmOverloads
public suspend fun ChatClient.getRepliesAsState(
    messageId: String,
    messageLimit: Int,
    olderToNewer: Boolean,
    coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
): ThreadState {
    StreamLog.d(TAG) {
        "[getRepliesAsState] messageId: $messageId, messageLimit: $messageLimit, olderToNewer: $olderToNewer"
    }
    return requestsAsState(coroutineScope).getReplies(messageId, messageLimit, olderToNewer)
}

/**
 * Returns thread replies in the form of [ThreadState], however, unlike [getRepliesAsState]
 * it will return it only after the API call made to get replies has ended. Thread state
 * will be returned regardless if the API call has succeeded or failed, the only difference is
 * in how up to date the replies in the thread state are.
 *
 * @param messageId The ID of the original message the replies were made to.
 * @param messageLimit The number of messages that will be initially loaded.
 * @param olderToNewer The flag that determines the order of the messages.
 *
 * @return [ThreadState] wrapped inside a [Call].
 */
@InternalStreamChatApi
public suspend fun ChatClient.awaitRepliesAsState(
    messageId: String,
    messageLimit: Int,
    olderToNewer: Boolean,
): ThreadState {
    StreamLog.d(TAG) { "[awaitRepliesAsState] messageId: $messageId, messageLimit: $messageLimit" }
    return coroutineScope {
        requestsAsState(scope = this).awaitReplies(messageId, messageLimit, olderToNewer)
    }
}

/**
 * Provides an ease-of-use piece of functionality that checks if the user is available or not. If it's not, we don't
 * emit any state, but rather return an empty StateFlow.
 *
 * If the user is set, we fetch the state using the provided operation and provide it to the user.
 */
private fun <T> ChatClient.getStateOrNull(
    coroutineScope: CoroutineScope,
    producer: suspend () -> T,
): StateFlow<T?> {
    return clientState.initializationState.combine(clientState.user.map { it?.id }) { initializationState, userId ->
        userId to (initializationState == InitializationState.COMPLETE)
    }.distinctUntilChanged()
        .mapLatest {
            val (userId, initializationReady) = it
            when {
                userId == null || !initializationReady -> null
                else -> producer()
            }
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)
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
    return CoroutineCall(inheritScope { Job(it) }) {
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
 * @param context The context used to access the [DownloadManager].
 * @param attachment The attachment to download.
 * @param generateDownloadUri The function that generates the download URI for the attachment.
 * @param interceptRequest The function that intercepts the [DownloadManager.Request] before it's enqueued.
 *
 * @return Executable async [Call] downloading attachment.
 */
@Suppress("TooGenericExceptionCaught")
@CheckResult
public fun ChatClient.downloadAttachment(
    context: Context,
    attachment: Attachment,
    generateDownloadUri: (Attachment) -> Uri,
    interceptRequest: DownloadManager.Request.() -> Unit,
): Call<Unit> {
    return CoroutineCall(inheritScope { Job(it) }) {
        val logger by taggedLogger("Chat:DownloadAttachment")

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = generateDownloadUri(attachment)
            val subPath = attachment.name ?: attachment.title ?: attachment.parseAttachmentNameFromUrl()
                ?: createAttachmentFallbackName()

            logger.d { "Downloading attachment. Name: $subPath, Uri: $uri" }

            downloadManager.enqueue(
                DownloadManager.Request(uri)
                    .setTitle(subPath)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .apply(interceptRequest),
            )
            Result.Success(Unit)
        } catch (exception: Exception) {
            logger.d { "Downloading attachment failed. Error: ${exception.message}" }
            Result.Failure(Error.ThrowableError(message = "Could not download the attachment", cause = exception))
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
    StreamLog.d(TAG) { "[loadOlderMessages] cid: $cid, messageLimit: $messageLimit" }
    return CoroutineCall(inheritScope { Job(it) }) {
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
    StreamLog.d(TAG) {
        "[loadNewerMessages] cid: $channelCid, " +
            "messageLimit: $messageLimit, baseMessageId: $baseMessageId"
    }
    return CoroutineCall(inheritScope { Job(it) }) {
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
 * Loads messages around the given message id.
 *
 * @param cid The full channel id i.e. "messaging:123".
 * @param messageId The id of the message around which we want to load messages.
 *
 * @return The channel wrapped in [Call]. This channel contains messages around the requested message.
 */
public fun ChatClient.loadMessagesAroundId(
    cid: String,
    messageId: String,
): Call<Channel> {
    StreamLog.d(TAG) { "[loadMessagesAroundId] cid: $cid, messageId: $messageId" }
    return CoroutineCall(inheritScope { Job(it) }) {
        when (val cidValidationResult = validateCidWithResult(cid)) {
            is Result.Success -> {
                val (channelType, channelId) = cid.cidToTypeAndId()
                logic.channel(channelType = channelType, channelId = channelId)
                    .loadMessagesAroundId(messageId)
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
@Suppress("TooGenericExceptionCaught")
public fun ChatClient.cancelEphemeralMessage(message: Message): Call<Boolean> {
    return CoroutineCall(inheritScope { Job(it) }) {
        when (val cidValidationResult = validateCidWithResult(message.cid)) {
            is Result.Success -> {
                try {
                    require(message.isEphemeral()) { "Only ephemeral message can be canceled" }
                    logic.channelFromMessage(message)?.deleteMessage(message)
                    logic.getActiveQueryThreadsLogic().forEach { it.deleteMessage(message) }
                    logic.threadFromMessage(message)?.removeLocalMessage(message)
                    repositoryFacade.deleteChannelMessage(message)

                    Result.Success(true)
                } catch (exception: Exception) {
                    Result.Failure(
                        Error.ThrowableError(
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
 * Attempts to fetch the message from offline cache before making an API call.
 *
 * @param messageId The id of the message we are fetching.
 *
 * @return The message with the corresponding iID wrapped inside a [Call].
 */
@CheckResult
public fun ChatClient.getMessageUsingCache(
    messageId: String,
): Call<Message> {
    return CoroutineCall(inheritScope { Job(it) }) {
        val message = logic.getMessageById(messageId) ?: logic.getMessageByIdFromDb(messageId)

        if (message != null) {
            Result.Success(message)
        } else {
            getMessage(messageId).await()
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
    StreamLog.d(TAG) { "[loadMessageById] cid: $cid, messageId: $messageId" }
    return CoroutineCall(inheritScope { Job(it) }) {
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
                Result.Failure(Error.GenericError("The message could not be found."))
            }
        }
        is Result.Failure -> Result.Failure(
            Error.GenericError("Error while fetching messages from backend. Messages around id: $messageId"),
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
    StreamLog.d(TAG) { "[loadNewestMessages] cid: $cid, messageLimit: $messageLimit, userPresence: $userPresence" }
    return CoroutineCall(inheritScope { Job(it) }) {
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

/**
 * Creates a fallback name for attachments without [Attachment.name] or [Attachment.title] properties.
 * Fallback names are generated in the following manner: "attachment_2022-16-12_12-15-06".
 */
private fun createAttachmentFallbackName(): String {
    val dateString = SimpleDateFormat(ATTACHMENT_FALLBACK_NAME_DATE_FORMAT, Locale.getDefault())
        .format(Date())
        .toString()

    return "attachment_$dateString"
}

/**
 * Date format pattern used for creating fallback names for attachments without [Attachment.name] or [Attachment.title]
 * properties
 */
private const val ATTACHMENT_FALLBACK_NAME_DATE_FORMAT: String = "yyyy-MM-dd_HH-mm-ss"
