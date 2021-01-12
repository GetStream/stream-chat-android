@file:JvmName("MessageListViewModelBinding")

package io.getstream.chat.android.ui.messages.view

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.AttachmentDownload
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.BlockUser
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.DeleteMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.EndRegionReached
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.FlagMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.GiphyActionSelected
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.LastMessageRead
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.MessageReaction
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.MuteUser
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.ReplyMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.RetryMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.ThreadModeEntered
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R

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
    view.setOnMessageDeleteHandler { onEvent(DeleteMessage(it)) }
    view.setOnStartThreadHandler { onEvent(ThreadModeEntered(it)) }
    view.setOnMessageFlagHandler { onEvent(FlagMessage(it)) }
    view.setOnSendGiphyHandler { message, giphyAction ->
        onEvent(GiphyActionSelected(message, giphyAction))
    }
    view.setOnMessageRetryHandler { onEvent(RetryMessage(it)) }
    view.setOnMessageReactionHandler { message, reactionType ->
        onEvent(MessageReaction(message, reactionType))
    }
    view.setOnMuteUserHandler { onEvent(MuteUser(it)) }
    view.setOnBlockUserHandler { user, channel -> onEvent(BlockUser(user, channel)) }
    view.setOnReplyMessageHandler { cid, message -> onEvent(ReplyMessage(cid, message)) }
    view.setOnAttachmentDownloadHandler { attachment -> onEvent(AttachmentDownload(attachment)) }

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
                view.displayNewMessage(state.messageListItem)
                view.hideLoadingView()
            }
        }
    }
    loadMoreLiveData.observe(lifecycleOwner, view::setLoadingMore)
    targetMessage.observe(lifecycleOwner, view::scrollToMessage)
}

/**
 * Binds [Title] with [MessageListViewModel].
 * Sets the View's handlers and displays new messages based on the ViewModel's state.
 */
@JvmName("bind")
// public fun MessageListViewModel.bindView(headerView: MessagesHeaderView, lifecycleOwner: LifecycleOwner) {
//     state.observe(lifecycleOwner) { state ->
//         if (state is MessageListViewModel.State.Result && state.messageListItem.isThread) {
//             headerView.setTitle(headerView.context.getString(R.string.stream_ui_title_thread_reply))
//             headerView.setOnlineStateSubtitle(threadSubtitle(headerView.context, state.messageListItem))
//         } else {
//
//         }
//     }
// }

private fun threadSubtitle(context: Context, messageWrapper: MessageListItemWrapper): String {
    val users = threadUsers(messageWrapper)

    return if (users.size == 1) {
        String.format(context.getString(R.string.stream_ui_subtitle_thread_reply_single_user), users[0].name)
    } else {
        String.format(context.getString(R.string.stream_ui_subtitle_thread_reply_many_users), users.size)
    }
}

private fun threadUsers(messageWrapper: MessageListItemWrapper) : List<User> {
    return messageWrapper.items
        .filterIsInstance<MessageListItem.MessageItem>()
        .map { messageItem -> messageItem.message.user }
}
