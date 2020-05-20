package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl

data class UseCaseHelper(var chatDomainImpl: ChatDomainImpl) {

    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels
     */
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
    /**
     * Returns a ThreadController for the specified thread
     */
    val getThread: GetThread = GetThreadImpl(chatDomainImpl)

    // unread counts
    /**
     * Returns a livedata object for the total number of unread messages
     */
    var getTotalUnreadCount: GetTotalUnreadCount = GetTotalUnreadCountImpl(chatDomainImpl)
    /**
     * Returns a livedata object for the number of channels with unread messages
     */
    var getUnreadChannelCount: GetUnreadChannelCount = GetUnreadChannelCountImpl(chatDomainImpl)

    // loading more
    /**
     * Loads older messages for the given channel
     */
    var loadOlderMessages: LoadOlderMessages = LoadOlderMessagesImpl(chatDomainImpl)

    /**
     * Loads newer messages for the given channel
     */
    var loadNewerMessages: LoadNewerMessages = LoadNewerMessagesImpl(chatDomainImpl)

    /**
     * Load more channels for the given query.
     */
    val queryChannelsLoadMore: QueryChannelsLoadMore = QueryChannelsLoadMoreImpl(chatDomainImpl)
    /**
     * Loads more messages for a thread
     */
    var threadLoadMore: ThreadLoadMore = ThreadLoadMoreImpl(chatDomainImpl)

    // updating channel data
    /**
     * Create a channel and retry using the retry policy if the request fails
     */
    // TODO: create channel needs more coverage. especially around immediately showing up in queryChannels
    var createChannel: CreateChannel = CreateChannelImpl(chatDomainImpl)
    /**
     * Send a message. This message is immediately added to local storage.
     * The API call to create the message is retried using the retry policy
     */
    var sendMessage: SendMessage = SendMessageImpl(chatDomainImpl)
    /**
     * Edit a message. This message is immediately updated in local storage.
     * The API call to edit the message is retried using the retry policy
     */
    var editMessage: EditMessage = EditMessageImpl(chatDomainImpl)
    /**
     * Delete a message. This message is immediately marked as deleted (message.deletedAt) in local storage.
     * The API call to delete the message is retried using the retry policy
     */
    var deleteMessage: DeleteMessage = DeleteMessageImpl(chatDomainImpl)
    /**
     * Send a reaction. This reaction is immediately added to local storage.
     * The API call to send a reaction is retried using the retry policy
     */
    var sendReaction: SendReaction = SendReactionImpl(chatDomainImpl)
    /**
     * Delete a reaction. This reaction is immediately marked as deleted in local storage.
     * The API call to delete a reaction is retried using the retry policy
     */
    var deleteReaction: DeleteReaction = DeleteReactionImpl(chatDomainImpl)
    /**
     * Keystroke should be called whenever the user enters something in the message input
     * It handles the deduplication and removal of typing events automatically
     */
    // TODO: Confirm this
    var keystroke: Keystroke = KeystrokeImpl(chatDomainImpl)
    /**
     * stopTyping is typically called manually when the message is submitted
     */
    var stopTyping: StopTyping = StopTypingImpl(chatDomainImpl)
    /**
     * markRead marks all messages on a channel read
     */
    var markRead: MarkRead = MarkReadImpl(chatDomainImpl)
    /**
     * hideChannel hides the channel till a new message event is received
     */
    var hideChannel: HideChannel = HideChannelImpl(chatDomainImpl)
    /**
     * showChannels shows a channel which was previously hidden
     */
    var showChannel: ShowChannel = ShowChannelImpl(chatDomainImpl)
}
