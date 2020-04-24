package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomain

data class UseCaseHelper(var chatDomain: ChatDomain) {

    // getting controllers
    var watchChannel = WatchChannel(chatDomain)
    var queryChannels = QueryChannels(chatDomain)
    val getThread = GetThread(chatDomain)

    // unread counts
    var getTotalUnreadCount = GetTotalUnreadCount(chatDomain)
    var getUnreadChannelCount = GetUnreadChannelCount(chatDomain)

    // loading more
    var loadOlderMessages = LoadOlderMessages(chatDomain)
    val queryChannelsLoadMore = QueryChannelsLoadMore(chatDomain)
    var threadLoadMore = ThreadLoadMore(chatDomain)

    // updating channel data
    var createChannel = CreateChannel(chatDomain)
    var sendMessage = SendMessage(chatDomain)
    var editMessage = EditMessage(chatDomain)
    var deleteMessage = DeleteMessage(chatDomain)
    var sendReaction = SendReaction(chatDomain)
    var deleteReaction = DeleteReaction(chatDomain)
    var keystroke = Keystroke(chatDomain)
    var stopTyping = StopTyping(chatDomain)
    var markRead = MarkRead(chatDomain)
}
