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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.alsoSendToChannelSelectionListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.attachmentRemovalListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.attachmentSelectionListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioCompleteButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioDeleteButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioPlaybackButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioRecordButtonCancelListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioRecordButtonHoldListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioRecordButtonLockListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioRecordButtonReleaseListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioSliderDragStartListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioSliderDragStopListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.audioStopButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.commandSelectionListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.commandsButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.dismissActionClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.dismissSuggestionsListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.mentionSelectionListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.sendMessageButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.textInputChangeListener

/**
 * A binder class responsible for binding the [MessageComposerViewModel] with the user interface components.
 * It provides flexibility by allowing custom listeners for various message composer events.
 *
 * @property vm The [MessageComposerViewModel] instance to be bound.
 */
public class MessageComposerViewModelBinder private constructor(
    private val vm: MessageComposerViewModel,
) {

    public companion object {

        /**
         * Creates a new instance of [MessageComposerViewModelBinder] with a given [MessageComposerViewModel].
         */
        @JvmStatic
        public fun with(
            vm: MessageComposerViewModel,
        ): MessageComposerViewModelBinder = MessageComposerViewModelBinder(vm)
    }

    private var messageBuilder: () -> Message = { vm.buildNewMessage() }
    private var sendMessageButtonClickListener: (Message) -> Unit = vm.sendMessageButtonClickListener
    private var textInputChangeListener: (String) -> Unit = vm.textInputChangeListener
    private var attachmentSelectionListener: (List<Attachment>) -> Unit = vm.attachmentSelectionListener
    private var attachmentRemovalListener: (Attachment) -> Unit = vm.attachmentRemovalListener
    private var mentionSelectionListener: (User) -> Unit = vm.mentionSelectionListener
    private var commandSelectionListener: (Command) -> Unit = vm.commandSelectionListener
    private var alsoSendToChannelSelectionListener: (Boolean) -> Unit = vm.alsoSendToChannelSelectionListener
    private var dismissActionClickListener: () -> Unit = vm.dismissActionClickListener
    private var commandsButtonClickListener: () -> Unit = vm.commandsButtonClickListener
    private var dismissSuggestionsListener: () -> Unit = vm.dismissSuggestionsListener
    private var audioRecordButtonHoldListener: () -> Unit = vm.audioRecordButtonHoldListener
    private var audioRecordButtonLockListener: () -> Unit = vm.audioRecordButtonLockListener
    private var audioRecordButtonCancelListener: () -> Unit = vm.audioRecordButtonCancelListener
    private var audioRecordButtonReleaseListener: () -> Unit = vm.audioRecordButtonReleaseListener
    private var audioDeleteButtonClickListener: () -> Unit = vm.audioDeleteButtonClickListener
    private var audioStopButtonClickListener: () -> Unit = vm.audioStopButtonClickListener
    private var audioPlaybackButtonClickListener: () -> Unit = vm.audioPlaybackButtonClickListener
    private var audioCompleteButtonClickListener: () -> Unit = vm.audioCompleteButtonClickListener
    private var audioSliderDragStartListener: (Float) -> Unit = vm.audioSliderDragStartListener
    private var audioSliderDragStopListener: (Float) -> Unit = vm.audioSliderDragStopListener

    /**
     * Sets the message builder that is invoked when the send message button is clicked.
     */
    public fun messageBuilder(builder: () -> Message): MessageComposerViewModelBinder {
        messageBuilder = builder
        return this
    }

    /**
     * Sets the click listener for the send message button.
     *
     * @param listener Click listener to be set.
     */
    public fun onSendMessageButtonClick(listener: (Message) -> Unit): MessageComposerViewModelBinder {
        sendMessageButtonClickListener = listener
        return this
    }

    /**
     * Sets the text change listener that is invoked each time after text was changed.
     *
     * @param listener Text change listener to be set.
     */
    public fun onTextInputChange(listener: (String) -> Unit): MessageComposerViewModelBinder {
        textInputChangeListener = listener
        return this
    }

    /**
     * Sets the selection listener that is invoked when attachments are selected.
     *
     * @param listener Attachment selection listener to be set.
     */
    public fun onAttachmentSelection(listener: (List<Attachment>) -> Unit): MessageComposerViewModelBinder {
        attachmentSelectionListener = listener
        return this
    }

    /**
     * Sets the click listener for the remove attachment button.
     *
     * @param listener Attachment removal click listener to be set.
     */
    public fun onAttachmentRemoval(listener: (Attachment) -> Unit): MessageComposerViewModelBinder {
        attachmentRemovalListener = listener
        return this
    }

    /**
     * Sets the selection listener invoked when a mention suggestion item is selected.
     *
     * @param listener Mention selection listener to be set.
     */
    public fun onMentionSelection(listener: (User) -> Unit): MessageComposerViewModelBinder {
        mentionSelectionListener = listener
        return this
    }

    /**
     * Sets the selection listener invoked when a command suggestion item is selected.
     *
     * @param listener Command selection listener to be set.
     */
    public fun onCommandSelection(listener: (Command) -> Unit): MessageComposerViewModelBinder {
        commandSelectionListener = listener
        return this
    }

    /**
     * Sets the selection listener for the "also send to channel" checkbox.
     *
     * @param listener Checkbox selection listener to be set.
     */
    public fun onAlsoSendToChannelSelection(listener: (Boolean) -> Unit): MessageComposerViewModelBinder {
        alsoSendToChannelSelectionListener = listener
        return this
    }

    /**
     * Sets the click listener for the dismiss action button.
     *
     * @param listener Dismiss action click listener to be set.
     */
    public fun onDismissActionClick(listener: () -> Unit): MessageComposerViewModelBinder {
        dismissActionClickListener = listener
        return this
    }

    /**
     * Sets the click listener for the pick commands button.
     *
     * @param listener Pick commands click listener to be set.
     */
    public fun onCommandsButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        commandsButtonClickListener = listener
        return this
    }

    /**
     * Sets the click listener invoked when suggestion popup is dismissed.
     *
     * @param listener Suggestion popup dismiss listener to be set.
     */
    public fun onDismissSuggestions(listener: () -> Unit): MessageComposerViewModelBinder {
        dismissSuggestionsListener = listener
        return this
    }

    /**
     * Sets the hold listener invoked when the microphone button gets pressed down.
     *
     * @param listener Microphone button hold listener to be set.
     */
    public fun onAudioRecordButtonHold(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonHoldListener = listener
        return this
    }

    /**
     * Sets the lock listener invoked when the audio recording gets locked.
     *
     * @param listener Audio record lock listener to be set.
     */
    public fun onAudioRecordButtonLock(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonLockListener = listener
        return this
    }

    /**
     * Sets the cancel listener invoked when the audio recording gets cancelled.
     *
     * @param listener Audio record cancel listener to be set.
     */
    public fun onAudioRecordButtonCancel(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonCancelListener = listener
        return this
    }

    /**
     * Sets the release listener invoked when the microphone button gets released.
     *
     * @param listener Microphone button release listener to be set.
     */
    public fun onAudioRecordButtonRelease(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonReleaseListener = listener
        return this
    }

    /**
     * Sets the click listener for the audio recording delete button.
     *
     * @param listener Audio recording delete button click listener to be set.
     */
    public fun onAudioDeleteButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioDeleteButtonClickListener = listener
        return this
    }

    /**
     * Sets the click listener for the audio recording stop button.
     *
     * @param listener Audio recording stop button click listener to be set.
     */
    public fun onAudioStopButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioStopButtonClickListener = listener
        return this
    }

    /**
     * Sets the click listener for the audio recording playback button.
     *
     * @param listener Audio recording playback button click listener to be set.
     */
    public fun onAudioPlaybackButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioPlaybackButtonClickListener = listener
        return this
    }

    /**
     * Sets the click listener for the audio recording complete button.
     *
     * @param listener Audio recording complete button click listener to be set.
     */
    public fun onAudioCompleteButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioCompleteButtonClickListener = { listener() }
        return this
    }

    /**
     * Sets the drag start listener that is invoked when the audio slider starts being dragged.
     *
     * @param listener Audio slider drag start listener to be set.
     */
    public fun onAudioSliderDragStart(listener: (Float) -> Unit): MessageComposerViewModelBinder {
        audioSliderDragStartListener = listener
        return this
    }

    /**
     * Sets the drag stop listener that is invoked when the audio slider stops being dragged.
     *
     * @param listener Audio slider drag stop listener to be set.
     */
    public fun onAudioSliderDragStop(listener: (Float) -> Unit): MessageComposerViewModelBinder {
        audioSliderDragStopListener = listener
        return this
    }

    @JvmName("bind")
    public fun bindView(view: MessageComposerView, lifecycleOwner: LifecycleOwner) {
        vm.bindView(
            view = view,
            lifecycleOwner = lifecycleOwner,
            messageBuilder = messageBuilder,
            sendMessageButtonClickListener = sendMessageButtonClickListener,
            textInputChangeListener = textInputChangeListener,
            attachmentSelectionListener = attachmentSelectionListener,
            attachmentRemovalListener = attachmentRemovalListener,
            mentionSelectionListener = mentionSelectionListener,
            commandSelectionListener = commandSelectionListener,
            alsoSendToChannelSelectionListener = alsoSendToChannelSelectionListener,
            dismissActionClickListener = dismissActionClickListener,
            commandsButtonClickListener = commandsButtonClickListener,
            dismissSuggestionsListener = dismissSuggestionsListener,
            audioRecordButtonHoldListener = audioRecordButtonHoldListener,
            audioRecordButtonLockListener = audioRecordButtonLockListener,
            audioRecordButtonCancelListener = audioRecordButtonCancelListener,
            audioRecordButtonReleaseListener = audioRecordButtonReleaseListener,
            audioDeleteButtonClickListener = audioDeleteButtonClickListener,
            audioStopButtonClickListener = audioStopButtonClickListener,
            audioPlaybackButtonClickListener = audioPlaybackButtonClickListener,
            audioCompleteButtonClickListener = audioCompleteButtonClickListener,
            audioSliderDragStartListener = audioSliderDragStartListener,
            audioSliderDragStopListener = audioSliderDragStopListener,
        )
    }
}
