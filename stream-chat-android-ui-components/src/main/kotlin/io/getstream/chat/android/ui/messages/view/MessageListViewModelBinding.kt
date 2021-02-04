@file:JvmName("MessageListViewModelBinding")

package io.getstream.chat.android.ui.messages.view

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.BlockUser
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.DeleteMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.DownloadAttachment
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.EndRegionReached
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.FlagMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.GiphyActionSelected
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.LastMessageRead
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.MessageReaction
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.MuteUser
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.ReplyMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.RetryMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.ThreadModeEntered
import io.getstream.chat.android.ui.gallery.toAttachment

/**
 * Binds [MessageListView] with [MessageListViewModel].
 * Sets the View's handlers and displays new messages based on the ViewModel's state.
 */
@JvmName("bind")
public fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    channel.observe(lifecycleOwner) {
        view.init(it, currentUser)
    }
    view.setEndRegionReachedHandler { onEvent(EndRegionReached) }
    view.setLastMessageReadHandler { onEvent(LastMessageRead) }
    view.setMessageDeleteHandler { onEvent(DeleteMessage(it)) }
    view.setThreadStartHandler { onEvent(ThreadModeEntered(it)) }
    view.setMessageFlagHandler { onEvent(FlagMessage(it)) }
    view.setGiphySendHandler { message, giphyAction ->
        onEvent(GiphyActionSelected(message, giphyAction))
    }
    view.setMessageRetryHandler { onEvent(RetryMessage(it)) }
    view.setMessageReactionHandler { message, reactionType ->
        onEvent(MessageReaction(message, reactionType, enforceUnique = true))
    }
    view.setUserMuteHandler { onEvent(MuteUser(it)) }
    view.setUserBlockHandler { user, cid -> onEvent(BlockUser(user, cid)) }
    view.setMessageReplyHandler { cid, message -> onEvent(ReplyMessage(cid, message)) }
    view.setAttachmentDownloadHandler { attachment -> onEvent(DownloadAttachment(attachment)) }

    state.observe(lifecycleOwner) { state ->
        when (state) {
            is MessageListViewModel.State.Loading -> {
                view.hideEmptyStateView()
                view.showLoadingView()
            }
            is MessageListViewModel.State.Result -> {
                if (state.messageListItem.items.isEmpty()) {
                    view.showEmptyStateView()
                } else {
                    view.hideEmptyStateView()
                }
                view.displayNewMessages(state.messageListItem)
                view.hideLoadingView()
            }
        }
    }
    loadMoreLiveData.observe(lifecycleOwner, view::setLoadingMore)
    targetMessage.observe(lifecycleOwner, view::scrollToMessage)

    view.setAttachmentReplyOptionClickHandler { result ->
        onEvent(MessageListViewModel.Event.ReplyAttachment(result.cid, result.messageId))
    }
    view.setAttachmentShowInChatOptionClickHandler { result ->
        onEvent(MessageListViewModel.Event.ShowMessage(result.messageId))
    }
    view.setAttachmentDeleteOptionClickHandler { result ->
        onEvent(
            MessageListViewModel.Event.RemoveAttachment(
                result.messageId,
                result.toAttachment()
            )
        )
    }
}
