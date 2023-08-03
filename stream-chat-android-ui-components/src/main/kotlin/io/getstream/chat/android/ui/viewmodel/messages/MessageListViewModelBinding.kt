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

@file:JvmName("MessageListViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.feature.gallery.toAttachment
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.BottomEndRegionReached
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.DeleteMessage
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.DownloadAttachment
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.EndRegionReached
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.FlagMessage
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.GiphyActionSelected
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.LastMessageRead
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.MessageReaction
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.ReplyMessage
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.RetryMessage
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel.Event.ThreadModeEntered

/**
 * Binds [MessageListView] with [MessageListViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to the ViewModel.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 *
 * @param view The [MessageListView] to bind the ViewModel to.
 * @param lifecycleOwner Current owner of the lifecycle in which the events are handled.
 */
@JvmName("bind")
public fun MessageListViewModel.bindView(
    view: MessageListView,
    lifecycleOwner: LifecycleOwner,
) {
    deletedMessageVisibility.observe(lifecycleOwner) {
        view.setDeletedMessageVisibility(it)
    }

    channel.observe(lifecycleOwner) {
        view.init(it)
    }
    view.setEndRegionReachedHandler { onEvent(EndRegionReached) }
    view.setBottomEndRegionReachedHandler { messageId -> onEvent(BottomEndRegionReached(messageId)) }
    view.setLastMessageReadHandler { onEvent(LastMessageRead) }
    view.setMessageDeleteHandler { onEvent(DeleteMessage(it, hard = false)) }
    view.setThreadStartHandler { onEvent(ThreadModeEntered(it)) }
    view.setMessageFlagHandler { onEvent(FlagMessage(it, view::handleFlagMessageResult)) }
    view.setMessagePinHandler { onEvent(MessageListViewModel.Event.PinMessage(it)) }
    view.setMessageUnpinHandler { onEvent(MessageListViewModel.Event.UnpinMessage(it)) }
    view.setGiphySendHandler { giphyAction ->
        onEvent(GiphyActionSelected(giphyAction))
    }
    view.setMessageRetryHandler { onEvent(RetryMessage(it)) }
    view.setMessageReactionHandler { message, reactionType ->
        onEvent(MessageReaction(message, reactionType))
    }
    view.setMessageReplyHandler { cid, message -> onEvent(ReplyMessage(cid, message)) }
    view.setAttachmentDownloadHandler { downloadAttachmentCall ->
        PermissionChecker().checkWriteStoragePermissions(view) {
            onEvent(DownloadAttachment(downloadAttachmentCall))
        }
    }
    view.setReplyMessageClickListener { replyTo ->
        onEvent(
            MessageListViewModel.Event.ShowMessage(
                messageId = replyTo.id,
                parentMessageId = replyTo.parentId,
            ),
        )
    }
    view.setOnScrollToBottomHandler { scrollToBottom { view.scrollToBottom() } }

    ownCapabilities.observe(lifecycleOwner) {
        view.setOwnCapabilities(it)
    }

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
            MessageListViewModel.State.NavigateUp -> Unit // Not handled here
        }
    }
    loadMoreLiveData.observe(lifecycleOwner, view::setLoadingMore)
    targetMessage.observe(lifecycleOwner, view::scrollToMessage)
    insideSearch.observe(lifecycleOwner, view::shouldRequestMessagesAtBottom)
    unreadCount.observe(lifecycleOwner, view::setUnreadCount)

    view.setAttachmentReplyOptionClickHandler { result ->
        onEvent(MessageListViewModel.Event.ReplyAttachment(result.cid, result.messageId))
    }
    view.setAttachmentShowInChatOptionClickHandler { result ->
        onEvent(
            MessageListViewModel.Event.ShowMessage(
                messageId = result.messageId,
                parentMessageId = result.parentId,
            ),
        )
    }
    view.setAttachmentDeleteOptionClickHandler { result ->
        onEvent(
            MessageListViewModel.Event.RemoveAttachment(
                result.messageId,
                result.toAttachment(),
            ),
        )
    }
    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError(it)
        },
    )
}
