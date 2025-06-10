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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.typing.TypingUpdatesBuffer
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for handling the composing and sending of messages.
 *
 * It relays all its core actions to a shared data source, as a central place for all the Composer logic.
 * Additionally, all the core data that can be reused across our SDKs is available through shared data sources, while
 * implementation-specific data is stored in respective in the [ViewModel].
 *
 * @param messageComposerController The controller used to relay all the actions and fetch all the state.
 */
public class MessageComposerViewModel(
    private val messageComposerController: MessageComposerController,
) : ViewModel() {

    /**
     * One-shot focus events used to request focus on the message input field.
     */
    public val focusRequestFlow: SharedFlow<Unit> = messageComposerController.focusRequestFlow

    /**
     * The full UI state that has all the required data.
     */
    public val messageComposerState: StateFlow<MessageComposerState> = messageComposerController.state

    /**
     * UI state of the current composer input.
     */
    public val input: MutableStateFlow<String> = messageComposerController.input

    /**
     * If the message will be shown in the channel after it is sent.
     */
    public val alsoSendToChannel: MutableStateFlow<Boolean> = messageComposerController.alsoSendToChannel

    /**
     * Represents the remaining time until the user is allowed to send the next message.
     */
    public val cooldownTimer: MutableStateFlow<Int> = messageComposerController.cooldownTimer

    /**
     * Represents the currently selected attachments, that are shown within the composer UI.
     */
    public val selectedAttachments: MutableStateFlow<List<Attachment>> = messageComposerController.selectedAttachments

    /**
     * Represents the list of validation errors for the current text input and the currently selected attachments.
     */
    public val validationErrors: MutableStateFlow<List<ValidationError>> = messageComposerController.validationErrors

    /**
     * Represents the list of users that can be used to autocomplete the current mention input.
     */
    public val mentionSuggestions: MutableStateFlow<List<User>> = messageComposerController.mentionSuggestions

    /**
     * Represents the list of commands to be displayed in the command suggestion list popup.
     */
    public val commandSuggestions: MutableStateFlow<List<Command>> = messageComposerController.commandSuggestions

    /**
     * Represents the list of links that can be previewed.
     */
    public val linkPreviews: MutableStateFlow<List<LinkPreview>> = messageComposerController.linkPreviews

    /**
     * Current message mode, either [MessageMode.Normal] or [MessageMode.MessageThread]. Used to determine if we're
     * sending a thread reply or a regular message.
     */
    public val messageMode: MutableStateFlow<MessageMode> = messageComposerController.messageMode

    /**
     * Gets the active [Edit] or [Reply] action, whichever is last, to show on the UI.
     */
    public val lastActiveAction: Flow<MessageAction?> = messageComposerController.lastActiveAction

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [ChannelCapabilities].
     */
    public val ownCapabilities: StateFlow<Set<String>> = messageComposerController.ownCapabilities

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String): Unit = messageComposerController.setMessageInput(value)

    /**
     * Called when the "Also send as a direct message" checkbox is checked or unchecked.
     *
     * @param alsoSendToChannel If the message will be shown in the channel after it is sent.
     */
    public fun setAlsoSendToChannel(alsoSendToChannel: Boolean): Unit =
        messageComposerController.setAlsoSendToChannel(alsoSendToChannel)

    /**
     * Called when the message mode changes and the internal state needs to be updated.
     *
     * This affects the business logic.
     *
     * @param messageMode The current message mode.
     */
    public fun setMessageMode(messageMode: MessageMode): Unit = messageComposerController.setMessageMode(messageMode)

    /**
     * Handles the selected [messageAction].
     *
     * @param messageAction The newly selected action.
     */
    public fun performMessageAction(messageAction: MessageAction): Unit =
        messageComposerController.performMessageAction(messageAction)

    /**
     * Dismisses all message actions from the UI and clears the input based on the internal state.
     */
    public fun dismissMessageActions(): Unit = messageComposerController.dismissMessageActions()

    /**
     * Stores the selected attachments from the attachment picker. These will be shown in the UI,
     * within the composer component. We upload and send these attachments once the user taps on the
     * send button.
     *
     * @param attachments The attachments to store and show in the composer.
     */
    public fun addSelectedAttachments(attachments: List<Attachment>): Unit =
        messageComposerController.addSelectedAttachments(attachments)

    /**
     * Removes a selected attachment from the list, when the user taps on the cancel/delete button.
     *
     * This will update the UI to remove it from the composer component.
     *
     * @param attachment The attachment to remove.
     */
    public fun removeSelectedAttachment(attachment: Attachment): Unit =
        messageComposerController.removeSelectedAttachment(attachment)

    /**
     * Creates a poll with the given [pollConfig].
     *
     * @param pollConfig Configuration for creating a poll.
     */
    public fun createPoll(pollConfig: PollConfig) {
        messageComposerController.createPoll(pollConfig = pollConfig)
    }

    /**
     * Sends a given message using our Stream API. Based on the internal state, we either edit an existing message,
     * or we send a new message, using our API.
     *
     * It also dismisses any current message actions.
     *
     * @param message The message to send.
     */
    public fun sendMessage(
        message: Message,
        callback: Call.Callback<Message> = Call.Callback { /* no-op */ },
    ): Unit = messageComposerController.sendMessage(message, callback)

    /**
     * Builds a new [Message] to send to our API. Based on the internal state, we use the current action's message and
     * apply the given changes.
     *
     * If we're not editing a message, we'll fill in the required data for the message.
     *
     * @param message Message text.
     * @param attachments Message attachments.
     *
     * @return [Message] object, with all the data required to send it to the API.
     */
    public fun buildNewMessage(
        message: String = input.value,
        attachments: List<Attachment> = emptyList(),
    ): Message = messageComposerController.buildNewMessage(message, attachments)

    /**
     * Updates the UI state when leaving the thread, to switch back to the [MessageMode.Normal] message mode, by
     * calling [setMessageMode].
     *
     * It also dismisses any currently active message actions, such as [Edit] and [Reply], as the
     * user left the relevant thread.
     */
    public fun leaveThread(): Unit = messageComposerController.leaveThread()

    /**
     * Autocompletes the current text input with the mention from the selected user.
     *
     * @param user The user that is used to autocomplete the mention.
     */
    public fun selectMention(user: User): Unit = messageComposerController.selectMention(user)

    /**
     * Switches the message composer to the command input mode.
     *
     * @param command The command that was selected in the command suggestion list popup.
     */
    public fun selectCommand(command: Command): Unit = messageComposerController.selectCommand(command)

    /**
     * Toggles the visibility of the command suggestion list popup.
     */
    public fun toggleCommandsVisibility(): Unit = messageComposerController.toggleCommandsVisibility()

    /**
     * Sets the typing updates buffer.
     */
    public fun setTypingUpdatesBuffer(buffer: TypingUpdatesBuffer) {
        messageComposerController.typingUpdatesBuffer = buffer
    }

    /**
     * Clears the input and the current state of the composer.
     */
    public fun clearData(): Unit = messageComposerController.clearData()

    public fun startRecording(offset: Pair<Float, Float>): Unit = messageComposerController.startRecording(offset)

    public fun holdRecording(offset: Pair<Float, Float>): Unit = messageComposerController.holdRecording(offset)

    public fun lockRecording(): Unit = messageComposerController.lockRecording()

    public fun cancelRecording(): Unit = messageComposerController.cancelRecording()

    public fun stopRecording(): Unit = messageComposerController.stopRecording()

    public fun toggleRecordingPlayback(): Unit = messageComposerController.toggleRecordingPlayback()

    public fun completeRecording(): Unit = messageComposerController.completeRecording()

    public fun pauseRecording(): Unit = messageComposerController.pauseRecording()

    public fun seekRecordingTo(progress: Float): Unit = messageComposerController.seekRecordingTo(progress)

    public fun sendRecording() {
        completeRecording()
        sendMessage(buildNewMessage(input.value, selectedAttachments.value))
    }

    /**
     * Disposes the inner [MessageComposerController].
     */
    override fun onCleared() {
        super.onCleared()
        messageComposerController.onCleared()
    }
}
