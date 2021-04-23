package io.getstream.chat.android.offline.usecase

import io.getstream.chat.android.offline.ChatDomainImpl

public class UseCaseHelper internal constructor(chatDomainImpl: ChatDomainImpl) {

    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels
     */
    public val replayEventsForActiveChannels: ReplayEventsForActiveChannels =
        ReplayEventsForActiveChannels(chatDomainImpl)

    // getting controllers
    /**
     * Get channel controller for cid
     * Returns a channel controller object
     */
    public val getChannelController: GetChannelController = GetChannelController(chatDomainImpl)

    /**
     * Watch a channel/ Start listening for events on a channel
     * Returns a channel controller object
     */
    public val watchChannel: WatchChannel = WatchChannel(chatDomainImpl)

    /**
     * Query channels and start listening for changes using events
     * Returns a QueryChannelsController object
     */
    public val queryChannels: QueryChannels = QueryChannels(chatDomainImpl)

    /**
     * Returns a ThreadController for the specified thread
     */
    public val getThread: GetThread = GetThread(chatDomainImpl)

    // unread counts
    /**
     * Returns a livedata object for the total number of unread messages
     */
    public val getTotalUnreadCount: GetTotalUnreadCount = GetTotalUnreadCount(chatDomainImpl)

    /**
     * Returns a livedata object for the number of channels with unread messages
     */
    public val getUnreadChannelCount: GetUnreadChannelCount = GetUnreadChannelCount(chatDomainImpl)

    // loading more
    /**
     * Loads older messages for the given channel
     */
    public val loadOlderMessages: LoadOlderMessages = LoadOlderMessages(chatDomainImpl)

    /**
     * Loads newer messages for the given channel
     */
    public val loadNewerMessages: LoadNewerMessages = LoadNewerMessages(chatDomainImpl)

    /**
     * Loads a message for a given message id, optionally with a offset of newer and older messages.
     */
    public val loadMessageById: LoadMessageById = LoadMessageById(chatDomainImpl)

    /**
     * Load more channels for the given query.
     */
    public val queryChannelsLoadMore: QueryChannelsLoadMore = QueryChannelsLoadMore(chatDomainImpl)

    /**
     * Loads more messages for a thread
     */
    public val threadLoadMore: ThreadLoadMore = ThreadLoadMore(chatDomainImpl)

    // updating channel data
    /**
     * Create a channel and retry using the retry policy if the request fails
     */
    public val createChannel: CreateChannel = CreateChannel(chatDomainImpl)

    /**
     * Send a message. This message is immediately added to local storage.
     * The API call to create the message is retried using the retry policy
     */
    public val sendMessage: SendMessage = SendMessage(chatDomainImpl)

    /**
     * Cancel an emphemeral message. This message is immediately removed from local storage.
     * The API call to delete the message is retried using the retry policy
     */
    public val cancelMessage: CancelMessage = CancelMessage(chatDomainImpl)

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    public val shuffleGiphy: ShuffleGiphy = ShuffleGiphy(chatDomainImpl)

    /**
     * Sends selected giphy message to the channel.
     * Replaces the original "ephemeral" message in local storage with the one received from backend.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    public val sendGiphy: SendGiphy = SendGiphy(chatDomainImpl)

    /**
     * Edit a message. This message is immediately updated in local storage.
     * The API call to edit the message is retried using the retry policy
     */
    public val editMessage: EditMessage = EditMessage(chatDomainImpl)

    /**
     * Delete a message. This message is immediately marked as deleted (message.deletedAt) in local storage.
     * The API call to delete the message is retried using the retry policy
     */
    public val deleteMessage: DeleteMessage = DeleteMessage(chatDomainImpl)

    /**
     * Send a reaction. This reaction is immediately added to local storage.
     * The API call to send a reaction is retried using the retry policy
     */
    public val sendReaction: SendReaction = SendReaction(chatDomainImpl)

    /**
     * Delete a reaction. This reaction is immediately marked as deleted in local storage.
     * The API call to delete a reaction is retried using the retry policy
     */
    public val deleteReaction: DeleteReaction = DeleteReaction(chatDomainImpl)

    /**
     * Keystroke should be called whenever the user enters something in the message input
     public * It handles the deduplication and removal of typing events automatically
     */
    // TODO: Confirm this
    public val keystroke: Keystroke = Keystroke(chatDomainImpl)

    /**
     * stopTyping is typically called manually when the message is submitted
     */
    public val stopTyping: StopTyping = StopTyping(chatDomainImpl)

    /**
     * markRead marks all messages on a channel read
     */
    public val markRead: MarkRead = MarkRead(chatDomainImpl)

    /**
     * markAllRead marks all messages on a channel as read
     */
    public val markAllRead: MarkAllRead = MarkAllRead(chatDomainImpl)

    /**
     * hideChannel hides the channel till a new message event is received
     */
    public val hideChannel: HideChannel = HideChannel(chatDomainImpl)

    /**
     * showChannels shows a channel which was previously hidden
     */
    public val showChannel: ShowChannel = ShowChannel(chatDomainImpl)

    /**
     * Leaves a channel and retry using the retry policy if the request fails
     */
    public val leaveChannel: LeaveChannel = LeaveChannel(chatDomainImpl)

    /**
     * Deletes a channel
     */
    public val deleteChannel: DeleteChannel = DeleteChannel(chatDomainImpl)

    public val setMessageForReply: SetMessageForReply = SetMessageForReply(chatDomainImpl)

    /**
     * Downloads selected attachment
     */
    public val downloadAttachment: DownloadAttachment = DownloadAttachment(chatDomainImpl)
    /**
     * Performs user search request.
     */
    public val searchUsersByName: SearchUsersByName = SearchUsersByName(chatDomainImpl)

    public val queryMembers: QueryMembers = QueryMembers(chatDomainImpl)
}
