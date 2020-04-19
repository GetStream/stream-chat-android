package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomain

data class UseCaseHelper(var chatDomain: ChatDomain) {
    var getTotalUnreadCount = GetTotalUnreadCount(chatDomain)
    var getUnreadChannelCount = GetUnreadChannelCount(chatDomain)
    var createChannel = CreateChannel(chatDomain)
    var queryChannels = QueryChannels(chatDomain)
    val queryChannelsLoadMore = QueryChannelsLoadMore(chatDomain)
    val getThread = GetThread(chatDomain)
    var deleteMessage = DeleteMessage(chatDomain)
    var deleteReaction = DeleteReaction(chatDomain)
    var editMessage = EditMessage(chatDomain)
    var keystroke = Keystroke(chatDomain)
    var sendMessage = SendMessage(chatDomain)
    var sendReaction = SendReaction(chatDomain)
    var stopTyping =  StopTyping(chatDomain)
    var watchChannel = WatchChannel(chatDomain)
    var threadLoadMore = ThreadLoadMore(chatDomain)

}