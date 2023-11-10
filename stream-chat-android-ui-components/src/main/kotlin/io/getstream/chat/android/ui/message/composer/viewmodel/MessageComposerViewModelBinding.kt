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

package io.getstream.chat.android.ui.message.composer.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.message.composer.MessageComposerView
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.alsoSendToChannelSelectionListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.attachmentRemovalListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.attachmentSelectionListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.commandSelectionListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.commandsButtonClickListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.dismissActionClickListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.dismissSuggestionsListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.mentionSelectionListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.sendMessageButtonClickListener
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelDefaults.textInputChangeListener
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
 */
@JvmName("bind")
@JvmOverloads
@ExperimentalStreamChatApi
public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    sendMessageButtonClickListener: (Message) -> Unit = this.sendMessageButtonClickListener,
    textInputChangeListener: (String) -> Unit = this.textInputChangeListener,
    attachmentSelectionListener: (List<Attachment>) -> Unit = this.attachmentSelectionListener,
    attachmentRemovalListener: (Attachment) -> Unit = this.attachmentRemovalListener,
    mentionSelectionListener: (User) -> Unit = this.mentionSelectionListener,
    commandSelectionListener: (Command) -> Unit = this.commandSelectionListener,
    alsoSendToChannelSelectionListener: (Boolean) -> Unit = this.alsoSendToChannelSelectionListener,
    dismissActionClickListener: () -> Unit = this.dismissActionClickListener,
    commandsButtonClickListener: () -> Unit = this.commandsButtonClickListener,
    dismissSuggestionsListener: () -> Unit = this.dismissSuggestionsListener,
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
 */
@JvmName("bindDefaults")
@JvmOverloads
@ExperimentalStreamChatApi
public fun MessageComposerViewModel.bindViewDefaults(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    sendMessageButtonClickListener: ((Message) -> Unit)? = null,
    textInputChangeListener: ((String) -> Unit)? = null,
    attachmentSelectionListener: ((List<Attachment>) -> Unit)? = null,
    attachmentRemovalListener: ((Attachment) -> Unit)? = null,
    mentionSelectionListener: ((User) -> Unit)? = null,
    commandSelectionListener: ((Command) -> Unit)? = null,
    alsoSendToChannelSelectionListener: ((Boolean) -> Unit)? = null,
    dismissActionClickListener: (() -> Unit)? = null,
    commandsButtonClickListener: (() -> Unit)? = null,
    dismissSuggestionsListener: (() -> Unit)? = null,
) {
    bindView(
        view = view,
        lifecycleOwner = lifecycleOwner,
        sendMessageButtonClickListener = this.sendMessageButtonClickListener and sendMessageButtonClickListener,
        textInputChangeListener = this.textInputChangeListener and textInputChangeListener,
        attachmentSelectionListener = this.attachmentSelectionListener and attachmentSelectionListener,
        attachmentRemovalListener = this.attachmentRemovalListener and attachmentRemovalListener,
        mentionSelectionListener = this.mentionSelectionListener and mentionSelectionListener,
        commandSelectionListener = this.commandSelectionListener and commandSelectionListener,
        alsoSendToChannelSelectionListener = this.alsoSendToChannelSelectionListener and alsoSendToChannelSelectionListener,
        dismissActionClickListener = this.dismissActionClickListener and dismissActionClickListener,
        commandsButtonClickListener = this.commandsButtonClickListener and commandsButtonClickListener,
        dismissSuggestionsListener = this.dismissSuggestionsListener and dismissSuggestionsListener,
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

private object MessageComposerViewModelDefaults {
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
    val MessageComposerViewModel.attachmentRemovalListener: (Attachment) -> Unit get() = { removeSelectedAttachment(it) }
    val MessageComposerViewModel.mentionSelectionListener: (User) -> Unit get() = { selectMention(it) }
    val MessageComposerViewModel.commandSelectionListener: (Command) -> Unit get() = { selectCommand(it) }
    val MessageComposerViewModel.alsoSendToChannelSelectionListener: (Boolean) -> Unit get() = { setAlsoSendToChannel(it) }
    val MessageComposerViewModel.dismissActionClickListener: () -> Unit get() = { dismissMessageActions() }
    val MessageComposerViewModel.commandsButtonClickListener: () -> Unit get() = { toggleCommandsVisibility() }
    val MessageComposerViewModel.dismissSuggestionsListener: () -> Unit get() = { dismissSuggestionsPopup() }
}
