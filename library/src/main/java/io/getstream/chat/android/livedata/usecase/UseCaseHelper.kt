package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl

data class UseCaseHelper(var chatDomainImpl: ChatDomainImpl) {

    // getting controllers
    var watchChannel: WatchChannel = WatchChannelImpl(chatDomainImpl)
    var queryChannels: QueryChannels = QueryChannelsImpl(chatDomainImpl)
    val getThread : GetThread = GetThreadImpl(chatDomainImpl)

    // unread counts
    var getTotalUnreadCount: GetTotalUnreadCount = GetTotalUnreadCountImpl(chatDomainImpl)
    var getUnreadChannelCount: GetUnreadChannelCount = GetUnreadChannelCountImpl(chatDomainImpl)

    // loading more
    var loadOlderMessages: LoadOlderMessages = LoadOlderMessagesImpl(chatDomainImpl)
    val queryChannelsLoadMore : QueryChannelsLoadMore = QueryChannelsLoadMoreImpl(chatDomainImpl)
    var threadLoadMore : ThreadLoadMore = ThreadLoadMoreImpl(chatDomainImpl)

    // updating channel data
    var createChannel : CreateChannel = CreateChannelImpl(chatDomainImpl)
    var sendMessage : SendMessage = SendMessageImpl(chatDomainImpl)
    var editMessage : EditMessage = EditMessageImpl(chatDomainImpl)
    var deleteMessage : DeleteMessage = DeleteMessageImpl(chatDomainImpl)
    var sendReaction : SendReaction = SendReactionImpl(chatDomainImpl)
    var deleteReaction : DeleteReaction = DeleteReactionImpl(chatDomainImpl)
    var keystroke : Keystroke = KeystrokeImpl(chatDomainImpl)
    var stopTyping : StopTyping = StopTypingImpl(chatDomainImpl)
    var markRead : MarkRead = MarkReadImpl(chatDomainImpl)
}
