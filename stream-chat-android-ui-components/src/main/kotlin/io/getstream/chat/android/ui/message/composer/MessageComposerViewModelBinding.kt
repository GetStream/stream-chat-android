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
 * Function which connects [MessageComposerView] to [MessageComposerViewModel]. As a result the view renders the state
 * delivered by view model, and view model intercepts user's actions automatically.
 *
 * @param view Instance of [MessageComposerView]
 * @param lifecycleOwner [LifecycleOwner] of Activity or Fragment hosting the [MessageComposerView]
 * @param sendMessageClickListener Callback invoked when send button is clicked. Default implementation calls view model to send the message.
 * @param textInputChangeListener Callback invoked after the text in message input has changed. Default implementation updates text input value in view model.
 * @param clearButtonClickListener Callback invoked when user dismisses the message. Default implementation clears input value in view model.
 * @param attachmentSelectionListener Callback invoked when user selects list of attachments in attachments picker.
 * @param attachmentRemovalListener Callback invoked when user attempts to remove the attachment.
 * @param mentionSelectionListener Callback invoked when selects one of the mention suggestions.
 * @param commandSelectionListener Callback invoked when selects one of the command suggestions.
 * @param alsoSendToChannelSelectionListener Callback invoked when user selects one of the command suggestions.
 * @param dismissActionClickListener Callback invoked when user cancels the current action.
 */
public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    sendMessageClickListener: (Message) -> Unit = { sendMessage(it) },
    textInputChangeListener: (String) -> Unit = { setMessageInput(it) },
    clearButtonClickListener: () -> Unit = { setMessageInput("") },
    attachmentSelectionListener: (List<Attachment>) -> Unit = { addSelectedAttachments(it) },
    attachmentRemovalListener: (Attachment) -> Unit = { removeSelectedAttachment(it) },
    mentionSelectionListener: (User) -> Unit = { selectMention(it) },
    commandSelectionListener: (Command) -> Unit = { selectCommand(it) },
    alsoSendToChannelSelectionListener: (Boolean) -> Unit = { setAlsoSendToChannel(it) },
    dismissActionClickListener: () -> Unit = { dismissMessageActions() },
) {
    view.sendMessageClickListener = {
        val message = buildNewMessage()
        sendMessageClickListener(message)
    }

    view.textInputChangeListener = textInputChangeListener

    view.clearButtonClickListener = clearButtonClickListener

    view.attachmentSelectionListener = attachmentSelectionListener

    view.attachmentRemovalListener = attachmentRemovalListener

    view.mentionSelectionListener = mentionSelectionListener

    view.commandSelectionListener = commandSelectionListener

    view.alsoSendToChannelSelectionListener = alsoSendToChannelSelectionListener

    view.dismissActionClickListener = dismissActionClickListener

    lifecycleOwner.lifecycleScope.launch {
        messageComposerState.collect {
            view.renderState(it)
        }
    }

    lifecycleOwner.lifecycleScope.launch {
        availableCommands.collect {
            view.availableCommands = it
        }
    }
}
