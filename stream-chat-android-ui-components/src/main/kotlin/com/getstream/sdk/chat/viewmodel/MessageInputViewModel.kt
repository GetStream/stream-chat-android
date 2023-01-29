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

package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.getstream.sdk.chat.utils.extensions.isModerationFailed
import com.getstream.sdk.chat.utils.typing.DefaultTypingUpdatesBuffer
import com.getstream.sdk.chat.utils.typing.TypingUpdatesBuffer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.extensions.setMessageForReply
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

/**
 * ViewModel class for MessageInputView. Responsible for sending and updating chat messages.
 * Can be bound to the view using the MessageInputViewModel.bindView function.
 *
 * @param cid The full channel id, i.e. "messaging:123".
 * @param chatClient Entry point for most of the chat SDK
 * such as the current user, connection state, unread counts etc.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
public class MessageInputViewModel @JvmOverloads constructor(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    public val channelState: Flow<ChannelState> =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = DEFAULT_MESSAGES_LIMIT,
            coroutineScope = viewModelScope
        ).filterNotNull()

    /**
     * A list of [Channel] members.
     */
    public val members: LiveData<List<Member>> = channelState.flatMapLatest { it.members }.asLiveData()

    /**
     * List of available commands.
     */
    public val commands: LiveData<List<Command>> =
        channelState.flatMapLatest { it.channelConfig }.map { config ->
            config.commands
        }.asLiveData()

    /**
     * The cooldown interval for the given channel.
     */
    public val cooldownInterval: LiveData<Int> =
        channelState.flatMapLatest { it.channelData }.map { channelData ->
            channelData.cooldown
        }.asLiveData()

    /**
     * The maximum length of a message that can be typed in the message input.
     */
    public val maxMessageLength: LiveData<Int> =
        channelState.flatMapLatest { it.channelConfig }.map { config ->
            config.maxMessageLength
        }.asLiveData()

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
     */
    public val ownCapabilities: LiveData<Set<String>> = channelState.flatMapLatest { it.channelData }
        .map { it.ownCapabilities }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = setOf()
        ).asLiveData()

    /**
     * Buffers typing updates.
     *
     * @see [DefaultTypingUpdatesBuffer]
     */
    public var typingUpdatesBuffer: TypingUpdatesBuffer = DefaultTypingUpdatesBuffer(
        onTypingStarted = ::keystroke,
        onTypingStopped = ::stopTyping,
        coroutineScope = viewModelScope
    )

    /**
     * Holds the message the user is currently replying to,
     * if the user is replying to a message.
     */
    public val repliedMessage: LiveData<Message?> = channelState.flatMapLatest { it.repliedMessage }.asLiveData()

    /**
     * Emits true if the message is a direct message between two users.
     *
     * Combining channel data with user information is necessary in order
     * to avoid crashes for users who initialize components before setting
     * the user.
     */
    public val isDirectMessage: LiveData<Boolean> =
        channelState.flatMapLatest { it.channelData }.map {
            val state = channelState.lastOrNull()

            state?.toChannel()?.isDirectMessaging() ?: false
        }.asLiveData()

    /**
     * Signals that we are currently in thread mode if the value is non-null.
     * If the value is null we are in normal mode.
     */
    private var activeThread = MutableLiveData<Message?>()

    /**
     * The message to be edited.
     */
    private val _messageToEdit: MutableLiveData<Message?> = MutableLiveData()

    /**
     * The message to be edited.
     */
    public val messageToEdit: LiveData<Message?> = _messageToEdit

    /**
     * A list of selected mentions.
     */
    private val selectedMentions = mutableSetOf<User>()

    /**
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger = StreamLog.getLogger("Chat:MessageInputViewModel")

    /**
     * Sets thread mode.
     *
     * @param parentMessage The original message on which the thread is based on.
     */
    public fun setActiveThread(parentMessage: Message) {
        activeThread.postValue(parentMessage)
    }

    /**
     * Gets the currently active thread.
     */
    public fun getActiveThread(): LiveData<Message?> {
        return activeThread
    }

    /**
     * Resets currently active thread.
     */
    public fun resetThread() {
        activeThread.postValue(null)
    }

    /**
     * Stores the selected mention to a [Set], user for populating the mentioned user IDs.
     *
     * @param user The selected user to mention.
     */
    public fun selectMention(user: User) {
        this.selectedMentions += user
    }

    /**
     * Sends a regular message to the channel.
     *
     * @param messageText The current message text.
     * @param messageTransformer Transformer that applies custom changes to the message, before being sent.
     */
    public fun sendMessage(messageText: String, messageTransformer: Message.() -> Unit = { }) {
        val message = Message(
            cid = cid,
            text = messageText,
            mentionedUsersIds = filterMentions(selectedMentions, messageText)
        )
        activeThread.value?.let { message.parentId = it.id }
        stopTyping()

        sendMessageInternal(message.apply(messageTransformer))
    }

    /**
     * Sends a message with non-custom attachments.
     *
     * @param messageText The current message text.
     * @param attachmentsWithMimeTypes Attachments that we support out of the box.
     * @param messageTransformer Transformer that applies custom changes to the message, before being sent.
     */
    public fun sendMessageWithAttachments(
        messageText: String,
        attachmentsWithMimeTypes: List<Pair<File, String?>>,
        messageTransformer: Message.() -> Unit = { },
    ) {
        // Send message should not be cancelled when viewModel.onCleared is called
        val attachments = attachmentsWithMimeTypes.map { (file, mimeType) ->
            Attachment(upload = file, mimeType = mimeType)
        }.toMutableList()

        val message = Message(
            cid = cid,
            text = messageText,
            attachments = attachments,
            mentionedUsersIds = filterMentions(selectedMentions, messageText)
        ).apply(messageTransformer)
        sendMessageInternal(message)
    }

    private fun sendMessageInternal(message: Message) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        chatClient.sendMessage(channelType, channelId, message)
            .enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not send message with cid: ${message.cid}. " +
                            "Error message: ${chatError.message}. Cause message: ${chatError.cause?.message}"
                    }
                }
            )
    }

    /**
     * Sends a message with custom attachments.
     *
     * @param messageText The current message text.
     * @param customAttachments Attachments that are custom built by the user.
     * @param messageTransformer Transformer that applies custom changes to the message, before being sent.
     */
    @ExperimentalStreamChatApi
    public fun sendMessageWithCustomAttachments(
        messageText: String,
        customAttachments: List<Attachment>,
        messageTransformer: Message.() -> Unit = { },
    ) {
        val message = Message(
            cid = cid,
            text = messageText,
            attachments = customAttachments.toMutableList(),
            mentionedUsersIds = filterMentions(selectedMentions, messageText)
        ).apply(messageTransformer)
        sendMessageInternal(message)
    }

    /**
     * Filters the current input and the mentions the user selected from the suggestion list. Removes any mentions which
     * are selected but no longer present in the input.
     *
     * @param selectedMentions The set of selected users from the suggestion list.
     * @param message The current message input.
     *
     * @return [MutableList] of user IDs of mentioned users.
     */
    private fun filterMentions(selectedMentions: Set<User>, message: String): MutableList<String> {
        val text = message.lowercase()

        val remainingMentions = selectedMentions.filter {
            text.contains("@${it.name.lowercase()}")
        }.map { it.id }

        this.selectedMentions.clear()
        return remainingMentions.toMutableList()
    }

    /**
     * Updates the message in the channel with the new data. If the edited message was moderated message will delete the
     * old message and send a new one.
     *
     * @param message The Message updated with the new information, that we need to send.
     */
    public fun editMessage(message: Message) {
        val updatedMessage = message.copy(mentionedUsersIds = filterMentions(selectedMentions, message.text))
        stopTyping()

        if (message.isModerationFailed(chatClient)) {
            onEditModeratedMessage(message)
        } else {
            chatClient.updateMessage(updatedMessage).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not edit message with cid: ${updatedMessage.cid}. " +
                            "Error message: ${chatError.message}. Cause message: ${chatError.cause?.message}"
                    }
                }
            )
        }
    }

    /**
     * If the edited message was a moderated message then we delete the old one from the local cache/database and send
     * a new message to the API.
     *
     * @param message The [Message] updated with the new information that we need to send.
     */
    @OptIn(ExperimentalStreamChatApi::class)
    private fun onEditModeratedMessage(message: Message) {
        chatClient.deleteMessage(message.id, true).enqueue()
        if (message.attachments.isNotEmpty()) {
            // Works for both custom and default attachments, reason being that it doesn't do any parsing of the
            // attachments
            sendMessageWithCustomAttachments(message.text, message.attachments) {
                parentId = message.parentId
            }
        } else {
            sendMessage(message.text) {
                parentId = message.parentId
            }
        }
    }

    /**
     * Sets the message to be edited.
     *
     * @param message The Message to edit.
     */
    public fun postMessageToEdit(message: Message?) {
        _messageToEdit.postValue(message)
    }

    /**
     * First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke.
     */
    @Synchronized
    public fun keystroke() {
        val parentId = activeThread.value?.id
        val (channelType, channelId) = cid.cidToTypeAndId()
        ChatClient.instance().keystroke(channelType, channelId, parentId).enqueue(
            onError = { chatError ->
                logger.e {
                    "Could not send keystroke cid: $cid. " +
                        "Error message: ${chatError.message}. Cause message: ${chatError.cause?.message}"
                }
            }
        )
    }

    /**
     * Sets last typing to null and sends the typing.stop event.
     */
    public fun stopTyping() {
        val parentId = activeThread.value?.id
        val (channelType, channelId) = cid.cidToTypeAndId()
        ChatClient.instance().stopTyping(channelType, channelId, parentId).enqueue(
            onError = { chatError ->
                logger.e {
                    "Could not send stop typing event with cid: $cid. " +
                        "Error message: ${chatError.message}. Cause message: ${chatError.cause?.message}"
                }
            }
        )
    }

    /**
     * Cancels the reply.
     */
    public fun dismissReply() {
        if (repliedMessage.value != null) {
            ChatClient.instance().setMessageForReply(cid, null).enqueue()
        }
    }

    /**
     * Queries the backend for channel members whose username contains the string represented by the argument
     * [contains].
     *
     * @param contains The string for which we are querying the backend in order to see if it is contained
     * within a member's username.
     *
     * @return A list of users whose username contains the string represented by [contains] or an empty list in case
     * no usernames contain the given string.
     */
    internal suspend fun queryMembersByUserNameContains(
        contains: String,
    ): List<User> {
        logger.v { "[queryMembersByUserNameContains] Querying the backend for members." }

        val (channelType, channelId) = cid.cidToTypeAndId()

        val result = chatClient.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = QUERY_MEMBERS_REQUEST_OFFSET,
            limit = QUERY_MEMBERS_REQUEST_MEMBER_LIMIT,
            filter = Filters.autocomplete(
                fieldName = "name",
                value = contains
            ),
            sort = QuerySortByField(),
            members = listOf()
        ).await()

        return if (result.isSuccess) {
            result.data().map { it.user }.filter { it.name.contains(contains, true) }
        } else {
            val error = result.error()

            logger.e {
                "[queryMembersByUserNameContains] Could not query members: " +
                    "${error.message}"
            }

            emptyList()
        }
    }

    /**
     * Performs hygiene events such as clearing typing updates
     * when the used leaves the messages screen.
     */
    override fun onCleared() {
        super.onCleared()
        typingUpdatesBuffer.clear()
    }

    private companion object {

        /**
         * The default limit for messages that will be requested.
         */
        private const val DEFAULT_MESSAGES_LIMIT: Int = 30

        /**
         * Pagination offset for the member query.
         */
        private const val QUERY_MEMBERS_REQUEST_OFFSET: Int = 0

        /**
         * The upper limit of members the query is allowed to return.
         */
        private const val QUERY_MEMBERS_REQUEST_MEMBER_LIMIT: Int = 30
    }
}
