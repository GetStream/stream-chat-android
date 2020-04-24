package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl

data class UseCaseHelper(var chatDomainImpl: ChatDomainImpl) {

    // getting controllers
    var watchChannel = WatchChannel(chatDomainImpl)
    var queryChannels = QueryChannels(chatDomainImpl)
    val getThread = GetThread(chatDomainImpl)

    // unread counts
    var getTotalUnreadCount = GetTotalUnreadCount(chatDomainImpl)
    var getUnreadChannelCount = GetUnreadChannelCount(chatDomainImpl)

    // loading more
    var loadOlderMessages = LoadOlderMessages(chatDomainImpl)
    val queryChannelsLoadMore = QueryChannelsLoadMore(chatDomainImpl)
    var threadLoadMore = ThreadLoadMore(chatDomainImpl)

    // updating channel data
    var createChannel = CreateChannel(chatDomainImpl)
    var sendMessage = SendMessage(chatDomainImpl)
    var editMessage = EditMessage(chatDomainImpl)
    var deleteMessage = DeleteMessage(chatDomainImpl)
    var sendReaction = SendReaction(chatDomainImpl)
    var deleteReaction = DeleteReaction(chatDomainImpl)
    var keystroke = Keystroke(chatDomainImpl)
    var stopTyping = StopTyping(chatDomainImpl)
    var markRead = MarkRead(chatDomainImpl)
}
