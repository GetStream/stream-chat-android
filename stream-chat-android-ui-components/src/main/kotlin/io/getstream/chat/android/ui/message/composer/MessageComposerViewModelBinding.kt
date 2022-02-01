package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Function which connects [MessageComposerView] to [MessageComposerViewModel]. As a result the view
 * renders the state delivered by view model, and view model intercepts user's actions automatically.
 *
 * @param view An instance of [MessageComposerView] to bind to the ViewModel.
 * @param lifecycleOwner [LifecycleOwner] of Activity or Fragment hosting the [MessageComposerView]
 * @param sendMessageButtonClickListener Click listener for the send message button.
 * @param textInputChangeListener Text change listener invoked each time after text was changed.
 * @param clearInputButtonClickListener Click listener for the clear input button.
 * @param attachmentSelectionListener Selection listener invoked when attachments are selected.
 * @param attachmentRemovalListener Click listener for the remove attachment button.
 * @param mentionSelectionListener Selection listener invoked when a mention suggestion item is selected.
 * @param commandSelectionListener Selection listener invoked when a command suggestion item is selected.
 * @param alsoSendToChannelSelectionListener Selection listener for the "also send to channel" checkbox.
 * @param dismissActionClickListener Click listener for the dismiss action button.
 * @param commandsButtonClickListener Click listener for the pick commands button.
 * @param dismissSuggestionsListener Click listener invoked when suggestion popup is dismissed.
 */
public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    sendMessageButtonClickListener: (Message) -> Unit = { sendMessage(it) },
    textInputChangeListener: (String) -> Unit = { setMessageInput(it) },
    clearInputButtonClickListener: () -> Unit = { setMessageInput("") },
    attachmentSelectionListener: (List<Attachment>) -> Unit = { addSelectedAttachments(it) },
    attachmentRemovalListener: (Attachment) -> Unit = { removeSelectedAttachment(it) },
    mentionSelectionListener: (User) -> Unit = { selectMention(it) },
    commandSelectionListener: (Command) -> Unit = { selectCommand(it) },
    alsoSendToChannelSelectionListener: (Boolean) -> Unit = { setAlsoSendToChannel(it) },
    dismissActionClickListener: () -> Unit = { dismissMessageActions() },
    commandsButtonClickListener: () -> Unit = { toggleCommandsVisibility() },
    dismissSuggestionsListener: () -> Unit = { dismissSuggestionsPopup() },
) {
    view.sendMessageButtonClickListener = { sendMessageButtonClickListener(buildNewMessage()) }
    view.textInputChangeListener = textInputChangeListener
    view.clearInputButtonClickListener = clearInputButtonClickListener
    view.attachmentSelectionListener = attachmentSelectionListener
    view.attachmentRemovalListener = attachmentRemovalListener
    view.mentionSelectionListener = mentionSelectionListener
    view.commandSelectionListener = commandSelectionListener
    view.alsoSendToChannelSelectionListener = alsoSendToChannelSelectionListener
    view.dismissActionClickListener = dismissActionClickListener
    view.commandsButtonClickListener = commandsButtonClickListener
    view.dismissSuggestionsListener = dismissSuggestionsListener

    lifecycleOwner.lifecycleScope.launch {
        messageComposerState.collect {
            view.renderState(it)
        }
    }
}
