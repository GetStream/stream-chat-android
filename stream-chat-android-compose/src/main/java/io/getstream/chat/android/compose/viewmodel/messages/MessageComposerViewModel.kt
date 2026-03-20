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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.CreatePollParams
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageInput
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.utils.typing.TypingUpdatesBuffer
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for handling the composing and sending of messages.
 *
 * Delegates all state management and business logic to [MessageComposerController],
 * including persistence of picker selections and edit-mode state across process death.
 *
 * @param messageComposerController The controller used to relay all the actions and fetch all the state.
 * @param storageHelper Resolves deferred attachment files before sending.
 */
public class MessageComposerViewModel(
    private val messageComposerController: MessageComposerController,
    private val storageHelper: AttachmentStorageHelper,
) : ViewModel() {

    /**
     * Emits each time the message input field should request focus (e.g. after a command is selected).
     */
    public val inputFocusEvents: SharedFlow<Unit> = messageComposerController.inputFocusEvents

    /**
     * The full UI state that has all the required data.
     */
    public val messageComposerState: StateFlow<MessageComposerState> = messageComposerController.state

    /**
     * UI state of the current composer input.
     */
    public val messageInput: StateFlow<MessageInput> = messageComposerController.messageInput

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
     * Adds [attachments] to the staged attachment list.
     *
     * Attachments are keyed by URI string, preserving insertion order.
     *
     * @param attachments The attachments to add.
     */
    public fun addAttachments(attachments: List<Attachment>) {
        messageComposerController.addAttachments(attachments)
    }

    /**
     * Removes [attachment] from the staged attachment list.
     *
     * @param attachment The attachment to remove.
     */
    public fun removeAttachment(attachment: Attachment) {
        messageComposerController.removeAttachment(attachment)
    }

    /**
     * Removes all staged attachments whose URI string key is contained in [uris].
     *
     * @param uris The URI string keys to remove.
     */
    internal fun removeAttachmentsByUris(uris: Set<String>) {
        messageComposerController.removeAttachmentsByUris(uris)
    }

    /**
     * Removes all staged attachments.
     *
     * Call this when the attachments are consumed — for example, after a message is sent,
     * a poll is created, or a command is selected.
     */
    public fun clearAttachments() {
        messageComposerController.clearAttachments()
    }

    /**
     * Creates a poll with the given [createPollParams].
     *
     * @param createPollParams Configuration for creating a poll.
     */
    public fun createPoll(createPollParams: CreatePollParams) {
        messageComposerController.createPoll(createPollParams = createPollParams)
    }

    /**
     * Sends a given message using our Stream API. Based on the internal state, we either edit an existing message,
     * or we send a new message, using our API.
     *
     * Deferred attachments (those without a local file) are resolved on a background thread
     * before the message is handed off to the controller.
     *
     * It also dismisses any current message actions.
     *
     * @param message The message to send.
     * @param callback Invoked when the API call completes.
     */
    public fun sendMessage(
        message: Message,
        callback: Call.Callback<Message> = Call.Callback { /* no-op */ },
    ) {
        messageComposerController.sendMessage(
            message = message,
            callback = callback,
            resolveAttachments = storageHelper::resolveAttachmentFiles,
        )
    }

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
        message: String = messageInput.value.text,
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
     * Autocompletes the current text input with the mention from the selected mention.
     *
     * IMPORTANT: The SDK supports only user mentions (see [Mention.User]). Custom mentions are purely visual, and will
     * not be submitted to the server.
     *
     * @param mention The mention that is used for the autocomplete.
     */
    public fun selectMention(mention: Mention): Unit = messageComposerController.selectMention(mention)

    /**
     * Switches the message composer to the command input mode.
     *
     * @param command The command that was selected in the command suggestion list popup.
     */
    public fun selectCommand(command: Command): Unit = messageComposerController.selectCommand(command)

    /**
     * @see [MessageComposerController.clearActiveCommand]
     */
    public fun clearActiveCommand(): Unit = messageComposerController.clearActiveCommand()

    /**
     * Toggles the visibility of the command suggestion list popup.
     */
    public fun toggleCommandsVisibility(): Unit = messageComposerController.toggleCommandsVisibility()

    /**
     * Sets the typing updates buffer.
     *
     * @param buffer The buffer to use for typing updates.
     */
    public fun setTypingUpdatesBuffer(buffer: TypingUpdatesBuffer) {
        messageComposerController.typingUpdatesBuffer = buffer
    }

    /**
     * Clears the input and the current state of the composer.
     */
    public fun clearData(): Unit = messageComposerController.clearData()

    public fun startRecording(): Unit = messageComposerController.startRecording()

    public fun holdRecording(offset: Pair<Float, Float>): Unit = messageComposerController.holdRecording(offset)

    public fun lockRecording(): Unit = messageComposerController.lockRecording()

    public fun cancelRecording(): Unit = messageComposerController.cancelRecording()

    public fun stopRecording(): Unit = messageComposerController.stopRecording()

    public fun toggleRecordingPlayback(): Unit = messageComposerController.toggleRecordingPlayback()

    public fun completeRecording(): Unit = messageComposerController.completeRecording()

    public fun pauseRecording(): Unit = messageComposerController.pauseRecording()

    public fun seekRecordingTo(progress: Float): Unit = messageComposerController.seekRecordingTo(progress)

    public fun sendRecording(): Unit = messageComposerController.sendRecording()

    /**
     * @see [MessageComposerController.cancelLinkPreview]
     */
    public fun cancelLinkPreview() {
        messageComposerController.cancelLinkPreview()
    }

    /**
     * Disposes the inner [MessageComposerController].
     */
    override fun onCleared() {
        super.onCleared()
        messageComposerController.onCleared()
    }
}
