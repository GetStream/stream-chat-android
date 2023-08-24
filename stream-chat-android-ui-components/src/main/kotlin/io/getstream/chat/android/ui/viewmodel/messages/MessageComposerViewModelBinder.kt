/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

public class MessageComposerViewModelBinder private constructor(
    private val vm: MessageComposerViewModel,
) {

    public companion object {
        @JvmStatic
        public fun with(
            vm: MessageComposerViewModel,
        ): MessageComposerViewModelBinder = MessageComposerViewModelBinder(vm)
    }

    private var sendMessageButtonClickListener: (Message) -> Unit = vm.sendMessageButtonClickListener
    private var textInputChangeListener: (String) -> Unit = vm.textInputChangeListener
    private var attachmentSelectionListener: (List<Attachment>) -> Unit = vm.attachmentSelectionListener
    private var attachmentRemovalListener: (Attachment) -> Unit  = vm.attachmentRemovalListener
    private var mentionSelectionListener: (User) -> Unit  = vm.mentionSelectionListener
    private var commandSelectionListener: (Command) -> Unit  = vm.commandSelectionListener
    private var alsoSendToChannelSelectionListener: (Boolean) -> Unit  = vm.alsoSendToChannelSelectionListener
    private var dismissActionClickListener: () -> Unit  = vm.dismissActionClickListener
    private var commandsButtonClickListener: () -> Unit  = vm.commandsButtonClickListener
    private var dismissSuggestionsListener: () -> Unit  = vm.dismissSuggestionsListener
    private var audioRecordButtonHoldListener: () -> Unit  = vm.audioRecordButtonHoldListener
    private var audioRecordButtonLockListener: () -> Unit  = vm.audioRecordButtonLockListener
    private var audioRecordButtonCancelListener: () -> Unit  = vm.audioRecordButtonCancelListener
    private var audioRecordButtonReleaseListener: () -> Unit  = vm.audioRecordButtonReleaseListener
    private var audioDeleteButtonClickListener: () -> Unit  = vm.audioDeleteButtonClickListener
    private var audioStopButtonClickListener: () -> Unit  = vm.audioStopButtonClickListener
    private var audioPlaybackButtonClickListener: () -> Unit  = vm.audioPlaybackButtonClickListener
    private var audioCompleteButtonClickListener: () -> Unit  = vm.audioCompleteButtonClickListener
    private var audioSliderDragStartListener: (Float) -> Unit  = vm.audioSliderDragStartListener
    private var audioSliderDragStopListener: (Float) -> Unit  = vm.audioSliderDragStopListener

    public fun onSendMessageButtonClick(listener: (Message) -> Unit): MessageComposerViewModelBinder {
        sendMessageButtonClickListener = listener
        return this
    }

    public fun onTextInputChange(listener: (String) -> Unit): MessageComposerViewModelBinder {
        textInputChangeListener = listener
        return this
    }

    public fun onAttachmentSelection(listener: (List<Attachment>) -> Unit): MessageComposerViewModelBinder {
        attachmentSelectionListener = listener
        return this
    }

    public fun onAttachmentRemoval(listener: (Attachment) -> Unit): MessageComposerViewModelBinder {
        attachmentRemovalListener = listener
        return this
    }

    public fun onMentionSelection(listener: (User) -> Unit): MessageComposerViewModelBinder {
        mentionSelectionListener = listener
        return this
    }

    public fun onCommandSelection(listener: (Command) -> Unit): MessageComposerViewModelBinder {
        commandSelectionListener = listener
        return this
    }

    public fun onAlsoSendToChannelSelection(listener: (Boolean) -> Unit): MessageComposerViewModelBinder {
        alsoSendToChannelSelectionListener = listener
        return this
    }

    public fun onDismissActionClick(listener: () -> Unit): MessageComposerViewModelBinder {
        dismissActionClickListener = listener
        return this
    }

    public fun onCommandsButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        commandsButtonClickListener = listener
        return this
    }

    public fun onDismissSuggestions(listener: () -> Unit): MessageComposerViewModelBinder {
        dismissSuggestionsListener = listener
        return this
    }

    public fun onAudioRecordButtonHold(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonHoldListener = listener
        return this
    }

    public fun onAudioRecordButtonLock(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonLockListener = listener
        return this
    }

    public fun onAudioRecordButtonCancel(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonCancelListener = listener
        return this
    }

    public fun onAudioRecordButtonRelease(listener: () -> Unit): MessageComposerViewModelBinder {
        audioRecordButtonReleaseListener = listener
        return this
    }

    public fun onAudioDeleteButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioDeleteButtonClickListener = listener
        return this
    }

    public fun onAudioStopButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioStopButtonClickListener = listener
        return this
    }

    public fun onAudioPlaybackButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioPlaybackButtonClickListener = listener
        return this
    }

    public fun onAudioCompleteButtonClick(listener: () -> Unit): MessageComposerViewModelBinder {
        audioCompleteButtonClickListener = listener
        return this
    }

    public fun onAudioSliderDragStart(listener: (Float) -> Unit): MessageComposerViewModelBinder {
        audioSliderDragStartListener = listener
        return this
    }

    public fun onAudioSliderDragStop(listener: (Float) -> Unit): MessageComposerViewModelBinder {
        audioSliderDragStopListener = listener
        return this
    }

    public fun bindView(view: MessageComposerView, lifecycleOwner: LifecycleOwner) {
        vm.bindView(
            view = view,
            lifecycleOwner = lifecycleOwner,
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
