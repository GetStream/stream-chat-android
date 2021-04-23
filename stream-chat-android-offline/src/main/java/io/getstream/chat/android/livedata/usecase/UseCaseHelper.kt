package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.CoroutineScope

@Deprecated(
    message = "Do not use this class directly. Use cases are now exposed by ChatDomain directly as functions.",
    replaceWith = ReplaceWith("Replace this property call by obtaining a specific use case directly from ChatDomain."),
    level = DeprecationLevel.WARNING
)
public class UseCaseHelper internal constructor(chatDomain: ChatDomain) {

    private val scope = CoroutineScope(DispatcherProvider.IO)
    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels
     */
    public val replayEventsForActiveChannels: ReplayEventsForActiveChannels = ReplayEventsForActiveChannelsImpl(chatDomain)

    // getting controllers
    /**
     * Get channel controller for cid
     * Returns a channel controller object
     */
    public val getChannelController: GetChannelController = GetChannelControllerImpl(chatDomain)

    /**
     * Watch a channel/ Start listening for events on a channel
     * Returns a channel controller object
     */
    public val watchChannel: WatchChannel = WatchChannelImpl(chatDomain)

    /**
     * Query channels and start listening for changes using events
     * Returns a QueryChannelsController object
     */
    public val queryChannels: QueryChannels = QueryChannelsImpl(chatDomain)

    /**
     * Returns a ThreadController for the specified thread
     */
    public val getThread: GetThread = GetThreadImpl(chatDomain)

    /**
     * Returns a livedata object for the total number of unread messages
     */
    @Suppress("DEPRECATION_ERROR")
    public val getTotalUnreadCount: GetTotalUnreadCount =
        GetTotalUnreadCountImpl(chatDomain, scope)

    /**
     * Returns a livedata object for the number of channels with unread messages
     */
    @Suppress("DEPRECATION_ERROR")
    public val getUnreadChannelCount: GetUnreadChannelCount =
        GetUnreadChannelCountImpl(chatDomain, scope)

    // loading more
    /**
     * Loads older messages for the given channel
     */
    public val loadOlderMessages: LoadOlderMessages = LoadOlderMessagesImpl(chatDomain)

    /**
     * Loads newer messages for the given channel
     */
    public val loadNewerMessages: LoadNewerMessages = LoadNewerMessagesImpl(chatDomain)

    /**
     * Loads a message for a given message id, optionally with a offset of newer and older messages.
     */
    public val loadMessageById: LoadMessageById = LoadMessageByIdImpl(chatDomain)

    /**
     * Load more channels for the given query.
     */
    public val queryChannelsLoadMore: QueryChannelsLoadMore =
        QueryChannelsLoadMoreImpl(chatDomain)

    /**
     * Loads more messages for a thread
     */
    public val threadLoadMore: ThreadLoadMore = ThreadLoadMoreImpl(chatDomain)

    // updating channel data
    /**
     * Create a channel and retry using the retry policy if the request fails
     */
    public val createChannel: CreateChannel = CreateChannelImpl(chatDomain)

    /**
     * Send a message. This message is immediately added to local storage.
     * The API call to create the message is retried using the retry policy
     */
    public val sendMessage: SendMessage = SendMessageImpl(chatDomain)

    /**
     * Cancel an emphemeral message. This message is immediately removed from local storage.
     * The API call to delete the message is retried using the retry policy
     */
    public val cancelMessage: CancelMessage = CancelMessageImpl(chatDomain)

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    public val shuffleGiphy: ShuffleGiphy = ShuffleGiphyImpl(chatDomain)

    /**
     * Sends selected giphy message to the channel.
     * Replaces the original "ephemeral" message in local storage with the one received from backend.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     */
    public val sendGiphy: SendGiphy = SendGiphyImpl(chatDomain)

    /**
     * Edit a message. This message is immediately updated in local storage.
     * The API call to edit the message is retried using the retry policy
     */
    public val editMessage: EditMessage = EditMessageImpl(chatDomain)

    /**
     * Delete a message. This message is immediately marked as deleted (message.deletedAt) in local storage.
     * The API call to delete the message is retried using the retry policy
     */
    public val deleteMessage: DeleteMessage = DeleteMessageImpl(chatDomain)

    /**
     * Send a reaction. This reaction is immediately added to local storage.
     * The API call to send a reaction is retried using the retry policy
     */
    public val sendReaction: SendReaction = SendReactionImpl(chatDomain)

    /**
     * Delete a reaction. This reaction is immediately marked as deleted in local storage.
     * The API call to delete a reaction is retried using the retry policy
     */
    public val deleteReaction: DeleteReaction = DeleteReactionImpl(chatDomain)

    /**
     * Keystroke should be called whenever the user enters something in the message input
     public * It handles the deduplication and removal of typing events automatically
     */
    // TODO: Confirm this
    public val keystroke: Keystroke = KeystrokeImpl(chatDomain)

    /**
     * stopTyping is typically called manually when the message is submitted
     */
    public val stopTyping: StopTyping = StopTypingImpl(chatDomain)

    /**
     * markRead marks all messages on a channel read
     */
    public val markRead: MarkRead = MarkReadImpl(chatDomain)

    /**
     * markAllRead marks all messages on a channel as read
     */
    public val markAllRead: MarkAllRead = MarkAllReadImpl(chatDomain)

    /**
     * hideChannel hides the channel till a new message event is received
     */
    public val hideChannel: HideChannel = HideChannelImpl(chatDomain)

    /**
     * showChannels shows a channel which was previously hidden
     */
    public val showChannel: ShowChannel = ShowChannelImpl(chatDomain)

    /**
     * Leaves a channel and retry using the retry policy if the request fails
     */
    public val leaveChannel: LeaveChannel = LeaveChannelImpl(chatDomain)

    /**
     * Deletes a channel
     */
    public val deleteChannel: DeleteChannel = DeleteChannelImpl(chatDomain)

    public val setMessageForReply: SetMessageForReply = SetMessageForReplyImpl(chatDomain)

    /**
     * Downloads selected attachment
     */
    public val downloadAttachment: DownloadAttachment = DownloadAttachmentImpl(chatDomain)

    /**
     * Performs user search request.
     */
    public val searchUsersByName: SearchUsersByName = SearchUsersByName(chatDomain)

    public val queryMembers: QueryMembers = QueryMembers(chatDomain)
}
