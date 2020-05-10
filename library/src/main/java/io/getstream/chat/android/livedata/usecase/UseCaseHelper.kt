package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl

data class UseCaseHelper(var chatDomainImpl: ChatDomainImpl) {

    // replaying events
    var replayEventsForActiveChannels: ReplayEventsForActiveChannels = ReplayEventsForActiveChannelsImpl(chatDomainImpl)

    // getting controllers
    /**
     * Watch a channel/ Start listening for events on a channel
     * Returns a channel controller object
     */
    var watchChannel: WatchChannel = WatchChannelImpl(chatDomainImpl)
    /**
     * Query channels and start listening for changes using events
     * Returns a QueryChannelsController object
     */
    var queryChannels: QueryChannels = QueryChannelsImpl(chatDomainImpl)
    val getThread: GetThread = GetThreadImpl(chatDomainImpl)

    // unread counts
    var getTotalUnreadCount: GetTotalUnreadCount = GetTotalUnreadCountImpl(chatDomainImpl)
    var getUnreadChannelCount: GetUnreadChannelCount = GetUnreadChannelCountImpl(chatDomainImpl)

    // loading more
    var loadOlderMessages: LoadOlderMessages = LoadOlderMessagesImpl(chatDomainImpl)
    /**
     * Load more channels for the given query.
     */
    val queryChannelsLoadMore: QueryChannelsLoadMore = QueryChannelsLoadMoreImpl(chatDomainImpl)
    var threadLoadMore: ThreadLoadMore = ThreadLoadMoreImpl(chatDomainImpl)

    // updating channel data
    /**
     * Create a channel and retry using the retry policy if the request fails
     */
    var createChannel: CreateChannel = CreateChannelImpl(chatDomainImpl)
    var sendMessage: SendMessage = SendMessageImpl(chatDomainImpl)
    var editMessage: EditMessage = EditMessageImpl(chatDomainImpl)
    var deleteMessage: DeleteMessage = DeleteMessageImpl(chatDomainImpl)
    var sendReaction: SendReaction = SendReactionImpl(chatDomainImpl)
    var deleteReaction: DeleteReaction = DeleteReactionImpl(chatDomainImpl)
    var keystroke: Keystroke = KeystrokeImpl(chatDomainImpl)
    var stopTyping: StopTyping = StopTypingImpl(chatDomainImpl)
    var markRead: MarkRead = MarkReadImpl(chatDomainImpl)
    var hideChannel: HideChannel = HideChannelImpl(chatDomainImpl)
    var showChannel: ShowChannel = ShowChannelImpl(chatDomainImpl)
}
