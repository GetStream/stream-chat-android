package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl

public class UseCaseHelper internal constructor(chatDomainImpl: ChatDomainImpl) {

    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels
     */
    public val replayEventsForActiveChannels: ReplayEventsForActiveChannels =
        ReplayEventsForActiveChannelsImpl(chatDomainImpl)

    // getting controllers
    /**
     * Get channel controller for cid
     * Returns a channel controller object
     */
    public val getChannelController: GetChannelController = GetChannelControllerImpl(chatDomainImpl)

    /**
     * Watch a channel/ Start listening for events on a channel
     * Returns a channel controller object
     */
    public val watchChannel: WatchChannel = WatchChannelImpl(chatDomainImpl)

    /**
     * Query channels and start listening for changes using events
     * Returns a QueryChannelsController object
     */
    public val queryChannels: QueryChannels = QueryChannelsImpl(chatDomainImpl)

    /**
     * Returns a ThreadController for the specified thread
     */
    public val getThread: GetThread = GetThreadImpl(chatDomainImpl)

    // unread counts
    /**
     * Returns a livedata object for the total number of unread messages
     */
    public val getTotalUnreadCount: GetTotalUnreadCount = GetTotalUnreadCountImpl(chatDomainImpl)

    /**
     * Returns a livedata object for the number of channels with unread messages
     */
    @Suppress("DEPRECATION_ERROR")
    public val getUnreadChannelCount: GetUnreadChannelCount = GetUnreadChannelCountImpl(chatDomainImpl)

    // loading more
    /**
     * Loads older messages for the given channel
     */
    public val loadOlderMessages: LoadOlderMessages = LoadOlderMessagesImpl(chatDomainImpl)

    /**
     * Loads newer messages for the given channel
     */
    public val loadNewerMessages: LoadNewerMessages = LoadNewerMessagesImpl(chatDomainImpl)

    /**
     * Loads a message for a given message id, optionally with a offset of newer and older messages.
     */
    public val loadMessageById: LoadMessageById = LoadMessageByIdImpl(chatDomainImpl)

    /**
     * Load more channels for the given query.
     */
    public val queryChannelsLoadMore: QueryChannelsLoadMore = QueryChannelsLoadMoreImpl(chatDomainImpl)

    /**
     * Loads more messages for a thread
     */
    public val threadLoadMore: ThreadLoadMore = ThreadLoadMoreImpl(chatDomainImpl)

    // updating channel data
    /**
     * Create a channel and retry using the retry policy if the request fails
     */
    public val createChannel: CreateChannel = CreateChannelImpl(chatDomainImpl)

    /**
     * Send a message. This message is immediately added to local storage.
     * The API call to create the message is retried using the retry policy
     */
    public val sendMessage: SendMessage = SendMessageImpl(chatDomainImpl)

    /**
     * Cancel an emphemeral message. This message is immediately removed from local storage.
     * The API call to delete the message is retried using the retry policy
     */
    public val cancelMessage: CancelMessage = CancelMessageImpl(chatDomainImpl)

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    public val shuffleGiphy: ShuffleGiphy = ShuffleGiphyImpl(chatDomainImpl)

    /**
     * Sends selected giphy message to the channel.
     * Replaces the original "ephemeral" message in local storage with the one received from backend.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    public val sendGiphy: SendGiphy = SendGiphyImpl(chatDomainImpl)

    /**
     * Edit a message. This message is immediately updated in local storage.
     * The API call to edit the message is retried using the retry policy
     */
    public val editMessage: EditMessage = EditMessageImpl(chatDomainImpl)

    /**
     * Delete a message. This message is immediately marked as deleted (message.deletedAt) in local storage.
     * The API call to delete the message is retried using the retry policy
     */
    public val deleteMessage: DeleteMessage = DeleteMessageImpl(chatDomainImpl)

    /**
     * Send a reaction. This reaction is immediately added to local storage.
     * The API call to send a reaction is retried using the retry policy
     */
    public val sendReaction: SendReaction = SendReactionImpl(chatDomainImpl)

    /**
     * Delete a reaction. This reaction is immediately marked as deleted in local storage.
     * The API call to delete a reaction is retried using the retry policy
     */
    public val deleteReaction: DeleteReaction = DeleteReactionImpl(chatDomainImpl)

    /**
     * Keystroke should be called whenever the user enters something in the message input
     public * It handles the deduplication and removal of typing events automatically
     */
    // TODO: Confirm this
    public val keystroke: Keystroke = KeystrokeImpl(chatDomainImpl)

    /**
     * stopTyping is typically called manually when the message is submitted
     */
    public val stopTyping: StopTyping = StopTypingImpl(chatDomainImpl)

    /**
     * markRead marks all messages on a channel read
     */
    public val markRead: MarkRead = MarkReadImpl(chatDomainImpl)

    /**
     * markAllRead marks all messages on a channel as read
     */
    public val markAllRead: MarkAllRead = MarkAllReadImpl(chatDomainImpl)

    /**
     * hideChannel hides the channel till a new message event is received
     */
    public val hideChannel: HideChannel = HideChannelImpl(chatDomainImpl)

    /**
     * showChannels shows a channel which was previously hidden
     */
    public val showChannel: ShowChannel = ShowChannelImpl(chatDomainImpl)

    /**
     * Leaves a channel and retry using the retry policy if the request fails
     */
    public val leaveChannel: LeaveChannel = LeaveChannelImpl(chatDomainImpl)

    /**
     * Deletes a channel
     */
    public val deleteChannel: DeleteChannel = DeleteChannelImpl(chatDomainImpl)

    public val setMessageForReply: SetMessageForReply = SetMessageForReplyImpl(chatDomainImpl)

    /**
     * Downloads selected attachment
     */
    public val downloadAttachment: DownloadAttachment = DownloadAttachmentImpl(chatDomainImpl)
    /**
     * Performs user search request.
     */
    public val searchUsersByName: SearchUsersByName = SearchUsersByName(chatDomainImpl)

    public val queryMembers: QueryMembers = QueryMembers(chatDomainImpl)
}
