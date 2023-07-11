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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import kotlinx.coroutines.launch

/**
 * Function which connects [MessageComposerView] to [MessageComposerViewModel]. As a result the view
 * renders the state delivered by the ViewModel, and the ViewModel intercepts the user's actions automatically.
 *
 * @param view An instance of [MessageComposerView] to bind to the ViewModel.
 * @param lifecycleOwner [LifecycleOwner] of Activity or Fragment hosting the [MessageComposerView]
 * @param sendMessageButtonClickListener Click listener for the send message button.
 * @param textInputChangeListener Text change listener invoked each time after text was changed.
 * @param attachmentSelectionListener Selection listener invoked when attachments are selected.
 * @param attachmentRemovalListener Click listener for the remove attachment button.
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
    sendMessageButtonClickListener: (Message) -> Unit = { sendMessage(it) },
    textInputChangeListener: (String) -> Unit = { setMessageInput(it) },
    attachmentSelectionListener: (List<Attachment>) -> Unit = { addSelectedAttachments(it) },
    attachmentRemovalListener: (Attachment) -> Unit = { removeSelectedAttachment(it) },
    mentionSelectionListener: (User) -> Unit = { selectMention(it) },
    commandSelectionListener: (Command) -> Unit = { selectCommand(it) },
    alsoSendToChannelSelectionListener: (Boolean) -> Unit = { setAlsoSendToChannel(it) },
    dismissActionClickListener: () -> Unit = { dismissMessageActions() },
    commandsButtonClickListener: () -> Unit = { toggleCommandsVisibility() },
    dismissSuggestionsListener: () -> Unit = { dismissSuggestionsPopup() },
    audioRecordButtonHoldListener: () -> Unit = { startRecording() },
    audioRecordButtonLockListener: () -> Unit = { lockRecording() },
    audioRecordButtonCancelListener: () -> Unit = { cancelRecording() },
    audioRecordButtonReleaseListener: () -> Unit = { sendRecording() },
    audioDeleteButtonClickListener: () -> Unit = { cancelRecording() },
    audioStopButtonClickListener: () -> Unit = { stopRecording() },
    audioPlaybackButtonClickListener: () -> Unit = { toggleRecordingPlayback() },
    audioCompleteButtonClickListener: () -> Unit = { completeRecording() },
    audioSliderDragStartListener: (Float) -> Unit = { pauseRecording() },
    audioSliderDragStopListener: (Float) -> Unit = { progress -> seekRecordingTo(progress) },
) {
    view.sendMessageButtonClickListener = { sendMessageButtonClickListener(buildNewMessage()) }
    view.textInputChangeListener = textInputChangeListener
    view.attachmentSelectionListener = attachmentSelectionListener
    view.attachmentRemovalListener = attachmentRemovalListener
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
