package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl

class UseCaseHelper(chatDomainImpl: ChatDomainImpl) {

    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels
     */
    val replayEventsForActiveChannels: ReplayEventsForActiveChannels = ReplayEventsForActiveChannelsImpl(chatDomainImpl)

    // getting controllers
    /**
     * Watch a channel/ Start listening for events on a channel
     * Returns a channel controller object
     */
    val watchChannel: WatchChannel = WatchChannelImpl(chatDomainImpl)
    /**
     * Query channels and start listening for changes using events
     * Returns a QueryChannelsController object
     */
    val queryChannels: QueryChannels = QueryChannelsImpl(chatDomainImpl)
    /**
     * Returns a ThreadController for the specified thread
     */
    val getThread: GetThread = GetThreadImpl(chatDomainImpl)

    // unread counts
    /**
     * Returns a livedata object for the total number of unread messages
     */
    val getTotalUnreadCount: GetTotalUnreadCount = GetTotalUnreadCountImpl(chatDomainImpl)
    /**
     * Returns a livedata object for the number of channels with unread messages
     */
    val getUnreadChannelCount: GetUnreadChannelCount = GetUnreadChannelCountImpl(chatDomainImpl)

    // loading more
    /**
     * Loads older messages for the given channel
     */
    val loadOlderMessages: LoadOlderMessages = LoadOlderMessagesImpl(chatDomainImpl)

    /**
     * Loads newer messages for the given channel
     */
    val loadNewerMessages: LoadNewerMessages = LoadNewerMessagesImpl(chatDomainImpl)

    /**
     * Load more channels for the given query.
     */
    val queryChannelsLoadMore: QueryChannelsLoadMore = QueryChannelsLoadMoreImpl(chatDomainImpl)
    /**
     * Loads more messages for a thread
     */
    val threadLoadMore: ThreadLoadMore = ThreadLoadMoreImpl(chatDomainImpl)

    // updating channel data
    /**
     * Create a channel and retry using the retry policy if the request fails
     */
    val createChannel: CreateChannel = CreateChannelImpl(chatDomainImpl)
    /**
     * Send a message. This message is immediately added to local storage.
     * The API call to create the message is retried using the retry policy
     */
    val sendMessage: SendMessage = SendMessageImpl(chatDomainImpl)
    /**
     * Cancel an emphemeral message. This message is immediately removed from local storage.
     * The API call to delete the message is retried using the retry policy
     */
    val cancelMessage: CancelMessage = CancelMessage(chatDomainImpl)
    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    val shuffleGiphy: ShuffleGiphy = ShuffleGiphy(chatDomainImpl)
    /**
     * Sends selected giphy message to the channel.
     * Replaces the original "ephemeral" message in local storage with the one received from backend.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    val sendGiphy: SendGiphy = SendGiphy(chatDomainImpl)
    /**
     * Send a message with attachments.
     */
    val sendMessageWithAttachments: SendMessageWithAttachments = SendMessageWithAttachmentsImpl(chatDomainImpl)
    /**
     * Edit a message. This message is immediately updated in local storage.
     * The API call to edit the message is retried using the retry policy
     */
    val editMessage: EditMessage = EditMessageImpl(chatDomainImpl)
    /**
     * Delete a message. This message is immediately marked as deleted (message.deletedAt) in local storage.
     * The API call to delete the message is retried using the retry policy
     */
    val deleteMessage: DeleteMessage = DeleteMessageImpl(chatDomainImpl)
    /**
     * Send a reaction. This reaction is immediately added to local storage.
     * The API call to send a reaction is retried using the retry policy
     */
    val sendReaction: SendReaction = SendReactionImpl(chatDomainImpl)
    /**
     * Delete a reaction. This reaction is immediately marked as deleted in local storage.
     * The API call to delete a reaction is retried using the retry policy
     */
    val deleteReaction: DeleteReaction = DeleteReactionImpl(chatDomainImpl)
    /**
     * Keystroke should be called whenever the user enters something in the message input
     * It handles the deduplication and removal of typing events automatically
     */
    // TODO: Confirm this
    val keystroke: Keystroke = KeystrokeImpl(chatDomainImpl)
    /**
     * stopTyping is typically called manually when the message is submitted
     */
    val stopTyping: StopTyping = StopTypingImpl(chatDomainImpl)
    /**
     * markRead marks all messages on a channel read
     */
    val markRead: MarkRead = MarkReadImpl(chatDomainImpl)
    /**
     * hideChannel hides the channel till a new message event is received
     */
    val hideChannel: HideChannel = HideChannelImpl(chatDomainImpl)
    /**
     * showChannels shows a channel which was previously hidden
     */
    val showChannel: ShowChannel = ShowChannelImpl(chatDomainImpl)
}
