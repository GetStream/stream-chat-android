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

@file:JvmName("MessageComposerViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PollConfig
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
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.pollSubmissionListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.sendMessageButtonClickListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelDefaults.textInputChangeListener
import kotlinx.coroutines.launch

/**
 * Function which connects [MessageComposerView] to [MessageComposerViewModel]. As a result the view
 * renders the state delivered by the ViewModel, and the ViewModel intercepts the user's actions automatically.
 *
 * @param view An instance of [MessageComposerView] to bind to the ViewModel.
 * @param lifecycleOwner [LifecycleOwner] of Activity or Fragment hosting the [MessageComposerView]
 * @param messageBuilder A lambda function to build a new message.
 * @param sendMessageButtonClickListener Click listener for the send message button.
 * @param textInputChangeListener Text change listener invoked each time after text was changed.
 * @param attachmentSelectionListener Selection listener invoked when attachments are selected.
 * @param attachmentRemovalListener Click listener for the remove attachment button.
 * @param pollSubmissionListener Selection listener invoked when a poll is submitted.
 * @param mentionSelectionListener Selection listener invoked when a mention suggestion item is selected.
 * @param commandSelectionListener Selection listener invoked when a command suggestion item is selected.
 * @param alsoSendToChannelSelectionListener Selection listener for the "also send to channel" checkbox.
 * @param dismissActionClickListener Click listener for the dismiss action button.
 * @param commandsButtonClickListener Click listener for the pick commands button.
 * @param dismissSuggestionsListener Click listener invoked when suggestion popup is dismissed.
 * @param audioRecordButtonHoldListener Hold listener invoked when the microphone button gets pressed down.
 * @param audioRecordButtonLockListener Lock listener invoked when the audio recording gets locked.
 * @param audioRecordButtonCancelListener Cancel listener invoked when the audio recording gets cancelled.
 * @param audioRecordButtonReleaseListener Release listener invoked when the microphone button gets released.
 * @param audioDeleteButtonClickListener Click listener for the audio recording delete button.
 * @param audioStopButtonClickListener Click listener for the audio recording stop button.
 * @param audioPlaybackButtonClickListener Click listener for the audio recording playback button.
 * @param audioCompleteButtonClickListener Click listener for the audio recording complete button.
 * @param audioSliderDragStartListener Drag start listener invoked when the audio slider starts being dragged.
 * @param audioSliderDragStopListener Drag stop listener invoked when the audio slider stops being dragged.
 */
@JvmName("bind")
@JvmOverloads
public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    messageBuilder: () -> Message = { buildNewMessage() },
    sendMessageButtonClickListener: (Message) -> Unit = this.sendMessageButtonClickListener,
    textInputChangeListener: (String) -> Unit = this.textInputChangeListener,
    attachmentSelectionListener: (List<Attachment>) -> Unit = this.attachmentSelectionListener,
    attachmentRemovalListener: (Attachment) -> Unit = this.attachmentRemovalListener,
    pollSubmissionListener: (PollConfig) -> Unit = this.pollSubmissionListener,
    mentionSelectionListener: (User) -> Unit = this.mentionSelectionListener,
    commandSelectionListener: (Command) -> Unit = this.commandSelectionListener,
    alsoSendToChannelSelectionListener: (Boolean) -> Unit = this.alsoSendToChannelSelectionListener,
    dismissActionClickListener: () -> Unit = this.dismissActionClickListener,
    commandsButtonClickListener: () -> Unit = this.commandsButtonClickListener,
    dismissSuggestionsListener: () -> Unit = this.dismissSuggestionsListener,
    audioRecordButtonHoldListener: () -> Unit = this.audioRecordButtonHoldListener,
    audioRecordButtonLockListener: () -> Unit = this.audioRecordButtonLockListener,
    audioRecordButtonCancelListener: () -> Unit = this.audioRecordButtonCancelListener,
    audioRecordButtonReleaseListener: () -> Unit = this.audioRecordButtonReleaseListener,
    audioDeleteButtonClickListener: () -> Unit = this.audioDeleteButtonClickListener,
    audioStopButtonClickListener: () -> Unit = this.audioStopButtonClickListener,
    audioPlaybackButtonClickListener: () -> Unit = this.audioPlaybackButtonClickListener,
    audioCompleteButtonClickListener: () -> Unit = this.audioCompleteButtonClickListener,
    audioSliderDragStartListener: (Float) -> Unit = this.audioSliderDragStartListener,
    audioSliderDragStopListener: (Float) -> Unit = this.audioSliderDragStopListener,
) {
    view.sendMessageButtonClickListener = { sendMessageButtonClickListener(messageBuilder()) }
    view.textInputChangeListener = textInputChangeListener
    view.attachmentSelectionListener = attachmentSelectionListener
    view.attachmentRemovalListener = attachmentRemovalListener
    view.pollSubmissionListener = pollSubmissionListener
    view.mentionSelectionListener = mentionSelectionListener
    view.commandSelectionListener = commandSelectionListener
    view.alsoSendToChannelSelectionListener = alsoSendToChannelSelectionListener
    view.dismissActionClickListener = dismissActionClickListener
    view.commandsButtonClickListener = commandsButtonClickListener
    view.dismissSuggestionsListener = dismissSuggestionsListener
    view.audioRecordButtonHoldListener = audioRecordButtonHoldListener
    view.audioRecordButtonLockListener = audioRecordButtonLockListener
    view.audioRecordButtonCancelListener = audioRecordButtonCancelListener
    view.audioRecordButtonReleaseListener = audioRecordButtonReleaseListener
    view.audioDeleteButtonClickListener = audioDeleteButtonClickListener
    view.audioStopButtonClickListener = audioStopButtonClickListener
    view.audioPlaybackButtonClickListener = audioPlaybackButtonClickListener
    view.audioCompleteButtonClickListener = audioCompleteButtonClickListener
    view.audioSliderDragStartListener = audioSliderDragStartListener
    view.audioSliderDragStopListener = audioSliderDragStopListener

    lifecycleOwner.lifecycleScope.launch {
        messageComposerState.collect(view::renderState)
    }
}

/**
 * Function which connects [MessageComposerView] to [MessageComposerViewModel]. As a result the view
 * renders the state delivered by the ViewModel, and the ViewModel intercepts the user's actions automatically.
 * The main difference with [bindView] is that listeners in this function do not override the default behaviour.
 *
 * @param view An instance of [MessageComposerView] to bind to the ViewModel.
 * @param lifecycleOwner [LifecycleOwner] of Activity or Fragment hosting the [MessageComposerView]
 * @param messageBuilder A lambda function to build a new message.
 * @param sendMessageButtonClickListener Click listener for the send message button.
 * @param textInputChangeListener Text change listener invoked each time after text was changed.
 * @param attachmentSelectionListener Selection listener invoked when attachments are selected.
 * @param attachmentRemovalListener Click listener for the remove attachment button.
 * @param pollSubmissionListener Selection listener invoked when a poll is submitted.
 * @param mentionSelectionListener Selection listener invoked when a mention suggestion item is selected.
 * @param commandSelectionListener Selection listener invoked when a command suggestion item is selected.
 * @param alsoSendToChannelSelectionListener Selection listener for the "also send to channel" checkbox.
 * @param dismissActionClickListener Click listener for the dismiss action button.
 * @param commandsButtonClickListener Click listener for the pick commands button.
 * @param dismissSuggestionsListener Click listener invoked when suggestion popup is dismissed.
 * @param audioRecordButtonHoldListener Hold listener invoked when the microphone button gets pressed down.
 * @param audioRecordButtonLockListener Lock listener invoked when the audio recording gets locked.
 * @param audioRecordButtonCancelListener Cancel listener invoked when the audio recording gets cancelled.
 * @param audioRecordButtonReleaseListener Release listener invoked when the microphone button gets released.
 * @param audioDeleteButtonClickListener Click listener for the audio recording delete button.
 * @param audioStopButtonClickListener Click listener for the audio recording stop button.
 * @param audioPlaybackButtonClickListener Click listener for the audio recording playback button.
 * @param audioCompleteButtonClickListener Click listener for the audio recording complete button.
 * @param audioSliderDragStartListener Drag start listener invoked when the audio slider starts being dragged.
 * @param audioSliderDragStopListener Drag stop listener invoked when the audio slider stops being dragged.
 */
@JvmName("bindDefaults")
@JvmOverloads
public fun MessageComposerViewModel.bindViewDefaults(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    messageBuilder: () -> Message = { buildNewMessage() },
    sendMessageButtonClickListener: ((Message) -> Unit)? = null,
    textInputChangeListener: ((String) -> Unit)? = null,
    attachmentSelectionListener: ((List<Attachment>) -> Unit)? = null,
    attachmentRemovalListener: ((Attachment) -> Unit)? = null,
    pollSubmissionListener: ((PollConfig) -> Unit)? = null,
    mentionSelectionListener: ((User) -> Unit)? = null,
    commandSelectionListener: ((Command) -> Unit)? = null,
    alsoSendToChannelSelectionListener: ((Boolean) -> Unit)? = null,
    dismissActionClickListener: (() -> Unit)? = null,
    commandsButtonClickListener: (() -> Unit)? = null,
    dismissSuggestionsListener: (() -> Unit)? = null,
    audioRecordButtonHoldListener: (() -> Unit)? = null,
    audioRecordButtonLockListener: (() -> Unit)? = null,
    audioRecordButtonCancelListener: (() -> Unit)? = null,
    audioRecordButtonReleaseListener: (() -> Unit)? = null,
    audioDeleteButtonClickListener: (() -> Unit)? = null,
    audioStopButtonClickListener: (() -> Unit)? = null,
    audioPlaybackButtonClickListener: (() -> Unit)? = null,
    audioCompleteButtonClickListener: (() -> Unit)? = null,
    audioSliderDragStartListener: ((Float) -> Unit)? = null,
    audioSliderDragStopListener: ((Float) -> Unit)? = null,
) {
    bindView(
        view = view,
        lifecycleOwner = lifecycleOwner,
        messageBuilder = messageBuilder,
        sendMessageButtonClickListener = this.sendMessageButtonClickListener and sendMessageButtonClickListener,
        textInputChangeListener = this.textInputChangeListener and textInputChangeListener,
        attachmentSelectionListener = this.attachmentSelectionListener and attachmentSelectionListener,
        attachmentRemovalListener = this.attachmentRemovalListener and attachmentRemovalListener,
        pollSubmissionListener = this.pollSubmissionListener and pollSubmissionListener,
        mentionSelectionListener = this.mentionSelectionListener and mentionSelectionListener,
        commandSelectionListener = this.commandSelectionListener and commandSelectionListener,
        alsoSendToChannelSelectionListener = this.alsoSendToChannelSelectionListener and alsoSendToChannelSelectionListener,
        dismissActionClickListener = this.dismissActionClickListener and dismissActionClickListener,
        commandsButtonClickListener = this.commandsButtonClickListener and commandsButtonClickListener,
        dismissSuggestionsListener = this.dismissSuggestionsListener and dismissSuggestionsListener,
        audioRecordButtonHoldListener = this.audioRecordButtonHoldListener and audioRecordButtonHoldListener,
        audioRecordButtonLockListener = this.audioRecordButtonLockListener and audioRecordButtonLockListener,
        audioRecordButtonCancelListener = this.audioRecordButtonCancelListener and audioRecordButtonCancelListener,
        audioRecordButtonReleaseListener = this.audioRecordButtonReleaseListener and audioRecordButtonReleaseListener,
        audioDeleteButtonClickListener = this.audioDeleteButtonClickListener and audioDeleteButtonClickListener,
        audioStopButtonClickListener = this.audioStopButtonClickListener and audioStopButtonClickListener,
        audioPlaybackButtonClickListener = this.audioPlaybackButtonClickListener and audioPlaybackButtonClickListener,
        audioCompleteButtonClickListener = this.audioCompleteButtonClickListener and audioCompleteButtonClickListener,
        audioSliderDragStartListener = this.audioSliderDragStartListener and audioSliderDragStartListener,
        audioSliderDragStopListener = this.audioSliderDragStopListener and audioSliderDragStopListener,
    )
}

private infix fun <T> ((T) -> Unit).and(that: ((T) -> Unit)?): (T) -> Unit = when (that) {
    null -> this
    else -> {
        {
            this(it)
            that(it)
        }
    }
}

private infix fun (() -> Unit).and(that: (() -> Unit)?): () -> Unit = when (that) {
    null -> this
    else -> {
        {
            this()
            that()
        }
    }
}

internal object MessageComposerViewModelDefaults {
    val MessageComposerViewModel.sendMessageButtonClickListener: (Message) -> Unit
        get() = {
            sendMessage(it)
        }
    val MessageComposerViewModel.textInputChangeListener: (String) -> Unit get() = { setMessageInput(it) }
    val MessageComposerViewModel.attachmentSelectionListener: (List<Attachment>) -> Unit
        get() = {
            addSelectedAttachments(
                it,
            )
        }
    val MessageComposerViewModel.pollSubmissionListener: (PollConfig) -> Unit get() = { createPoll(it) }
    val MessageComposerViewModel.attachmentRemovalListener: (Attachment) -> Unit get() = { removeSelectedAttachment(it) }
    val MessageComposerViewModel.mentionSelectionListener: (User) -> Unit get() = { selectMention(it) }
    val MessageComposerViewModel.commandSelectionListener: (Command) -> Unit get() = { selectCommand(it) }
    val MessageComposerViewModel.alsoSendToChannelSelectionListener: (Boolean) -> Unit get() = { setAlsoSendToChannel(it) }
    val MessageComposerViewModel.dismissActionClickListener: () -> Unit get() = { dismissMessageActions() }
    val MessageComposerViewModel.commandsButtonClickListener: () -> Unit get() = { toggleCommandsVisibility() }
    val MessageComposerViewModel.dismissSuggestionsListener: () -> Unit get() = { dismissSuggestionsPopup() }
    val MessageComposerViewModel.audioRecordButtonHoldListener: () -> Unit get() = { startRecording() }
    val MessageComposerViewModel.audioRecordButtonLockListener: () -> Unit get() = { lockRecording() }
    val MessageComposerViewModel.audioRecordButtonCancelListener: () -> Unit get() = { cancelRecording() }
    val MessageComposerViewModel.audioRecordButtonReleaseListener: () -> Unit get() = { sendRecording() }
    val MessageComposerViewModel.audioDeleteButtonClickListener: () -> Unit get() = { cancelRecording() }
    val MessageComposerViewModel.audioStopButtonClickListener: () -> Unit get() = { stopRecording() }
    val MessageComposerViewModel.audioPlaybackButtonClickListener: () -> Unit get() = { toggleRecordingPlayback() }
    val MessageComposerViewModel.audioCompleteButtonClickListener: () -> Unit get() = { completeRecording() }
    val MessageComposerViewModel.audioSliderDragStartListener: (Float) -> Unit get() = { pauseRecording() }
    val MessageComposerViewModel.audioSliderDragStopListener: (Float) -> Unit get() = { seekRecordingTo(it) }
}
