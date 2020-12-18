package com.getstream.sdk.chat.view

import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

public interface IMessageListView {
    public fun init(channel: Channel, currentUser: User)
    public fun setEndRegionReachedHandler(endRegionReachedHandler: () -> Unit)
    public fun setLastMessageReadHandler(lastMessageReadHandler: () -> Unit)
    public fun setOnMessageEditHandler(onMessageEditHandler: (Message) -> Unit)
    public fun setOnMessageDeleteHandler(onMessageDeleteHandler: (Message) -> Unit)
    public fun setOnStartThreadHandler(onStartThreadHandler: (Message) -> Unit)
    public fun setOnMessageFlagHandler(onMessageFlagHandler: (Message) -> Unit)
    public fun setOnSendGiphyHandler(onSendGiphyHandler: (Message, GiphyAction) -> Unit)
    public fun setOnMessageRetryHandler(onMessageRetryHandler: (Message) -> Unit)
    public fun showEmptyStateView()
    public fun hideEmptyStateView()
    public fun showLoadingView()
    public fun hideLoadingView()
    public fun displayNewMessage(listItem: MessageListItemWrapper)
    public fun setLoadingMore(loadingMore: Boolean)
}
