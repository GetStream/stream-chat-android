package io.getstream.chat.android.offline.event

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HasOwnUser
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.sync.SyncManager
import io.getstream.chat.android.offline.extensions.mergeReactions
import io.getstream.chat.android.offline.extensions.setMember
import io.getstream.chat.android.offline.extensions.updateReads
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.Date
import java.util.InputMismatchException

internal class EventHandlerImpl(
    private val recoveryEnabled: Boolean,
    private val client: ChatClient,
    private val logic: LogicRegistry,
    private val state: StateRegistry,
    private val mutableGlobalState: GlobalMutableState,
    private val repos: RepositoryFacade,
    private val syncManager: SyncManager,
) {
    private var logger = ChatLogger.get("EventHandler")

    private var eventSubscription: Disposable = EMPTY_DISPOSABLE
    private var handlerEventJob: Deferred<*>? = null

    internal fun initialize(user: User, scope: CoroutineScope) {
        handlerEventJob = scope.async {
            syncManager.loadSyncStateForUser(user.id)
            replayEvensForAllChannels(user)
        }
    }

    /**
     * Start listening to chat events
     */
    internal fun startListening(scope: CoroutineScope) {
        if (eventSubscription.isDisposed) {
            eventSubscription = client.subscribe {
                handlerEventJob = scope.async { handleEvents(listOf(it)) }
            }
        }
    }

    internal fun stopListening() {
        eventSubscription.dispose()
    }

    internal suspend fun replyEventsForActiveChannels(): Result<List<ChatEvent>> {
        return replayEventsForChannels(activeChannelsCid())
    }

    internal fun addNewChannelToReplayEvents(cid: String) {
        val (type, id) = cid.cidToTypeAndId()
        logic.channel(type, id)
    }

    @VisibleForTesting
    internal suspend fun handleEvent(event: ChatEvent) {
        handleConnectEvents(listOf(event))
        handleEventsInternal(listOf(event), isFromSync = false)
    }

    private fun activeChannelsCid(): List<String> {
        return logic.getActiveChannelsLogic().map { it.cid }
    }

    private suspend fun replayEventsForChannels(cids: List<String>): Result<List<ChatEvent>> {
        return queryEvents(cids)
            .onSuccessSuspend { eventList ->
                handleEventsInternal(eventList, isFromSync = true)
            }
    }

    private suspend fun replayEvensForAllChannels(user: User) {
        repos.cacheChannelConfigs()

        // Sync cached channels
        val cachedChannelsCids = repos.selectAllCids()
        replayEventsForChannels(cachedChannelsCids)
    }

    private suspend fun handleEvents(events: List<ChatEvent>) {
        handleConnectEvents(events)
        handleEventsInternal(events, isFromSync = false)
    }

    private suspend fun handleConnectEvents(sortedEvents: List<ChatEvent>) {
        // send out the connect events
        sortedEvents.forEach { event ->
            // connection events are never send on the recovery endpoint, so handle them 1 by 1
            when (event) {
                is DisconnectedEvent -> {
                    mutableGlobalState._connectionState.value = ConnectionState.OFFLINE
                }
                is ConnectedEvent -> {
                    logger.logI("Received ConnectedEvent, marking the domain as online and initialized")
                    updateCurrentUser(event.me)

                    if (recoveryEnabled) {
                        syncManager.connectionRecovered()
                    }

                    // 4. recover missing events
                    val activeChannelCids = activeChannelsCid()
                    if (activeChannelCids.isNotEmpty()) {
                        replayEventsForChannels(activeChannelCids)
                    }
                }
                is HealthEvent -> {
                    syncManager.retryFailedEntities()
                }

                is ConnectingEvent -> {
                    mutableGlobalState._connectionState.value = ConnectionState.CONNECTING
                }

                else -> Unit // Ignore other events
            }
        }
    }

    private suspend fun queryEvents(cids: List<String>): Result<List<ChatEvent>> =
        client.getSyncHistory(cids, syncManager.syncStateFlow.value?.lastSyncedAt ?: Date()).await()

    private suspend fun updateOfflineStorageFromEvents(events: List<ChatEvent>, isFromSync: Boolean) {
        events.sortedBy(ChatEvent::createdAt)

        val batchBuilder = EventBatchUpdate.Builder()
        batchBuilder.addToFetchChannels(events.filterIsInstance<CidEvent>().map { it.cid })

        val users: List<User> = events.filterIsInstance<UserEvent>().map { it.user } +
            events.filterIsInstance<HasOwnUser>().map { it.me }

        // For some reason backend is not sending us the user instance into some events that they should
        // and we are not able to identify which event type is. Gson, because it is using reflection,
        // inject a null instance into property `user` that doesn't allow null values.
        // This is a workaround, while we identify which event type is, that omit null values without
        // break our public API
        @Suppress("USELESS_CAST")
        batchBuilder.addUsers(users)

        // step 1. see which data we need to retrieve from offline storage
        for (event in events) {
            when (event) {
                is MessageReadEvent,
                is MemberAddedEvent,
                is MemberRemovedEvent,
                is NotificationRemovedFromChannelEvent,
                is MemberUpdatedEvent,
                is ChannelUpdatedEvent,
                is ChannelUpdatedByUserEvent,
                is ChannelDeletedEvent,
                is ChannelHiddenEvent,
                is ChannelVisibleEvent,
                is NotificationAddedToChannelEvent,
                is NotificationInvitedEvent,
                is NotificationInviteAcceptedEvent,
                is NotificationInviteRejectedEvent,
                is ChannelTruncatedEvent,
                is HealthEvent,
                is NotificationMutesUpdatedEvent,
                is GlobalUserBannedEvent,
                is UserDeletedEvent,
                is UserPresenceChangedEvent,
                is GlobalUserUnbannedEvent,
                is UserUpdatedEvent,
                is NotificationChannelMutesUpdatedEvent,
                is ConnectedEvent,
                is ConnectingEvent,
                is DisconnectedEvent,
                is ErrorEvent,
                is UnknownEvent,
                is NotificationChannelDeletedEvent,
                is NotificationChannelTruncatedEvent,
                is NotificationMarkReadEvent,
                is TypingStartEvent,
                is TypingStopEvent,
                is ChannelUserBannedEvent,
                is UserStartWatchingEvent,
                is UserStopWatchingEvent,
                is ChannelUserUnbannedEvent,
                is MarkAllReadEvent,
                -> Unit
                is ReactionNewEvent -> batchBuilder.addToFetchMessages(event.reaction.messageId)
                is ReactionDeletedEvent -> batchBuilder.addToFetchMessages(event.reaction.messageId)
                is MessageDeletedEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is MessageUpdatedEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is NewMessageEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is NotificationMessageNewEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is ReactionUpdateEvent -> batchBuilder.addToFetchMessages(event.message.id)
            }
        }
        // actually fetch the data
        val batch = batchBuilder.build(repos, mutableGlobalState._user)

        // step 2. second pass through the events, make a list of what we need to update
        loop@ for (event in events) {
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (event) {
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, event.user)
                    updateTotalUnreadCountsIfNeeded(
                        totalUnreadCount = event.totalUnreadCount,
                        channelUnreadCount = event.unreadChannels,
                        isFromSync = isFromSync,
                        cid = event.cid,
                    )
                    batch.addMessageData(event.cid, event.message, isNewMessage = true)
                    repos.selectChannelWithoutMessages(event.cid)?.copy(hidden = false)
                        ?.let(batch::addChannel)
                }
                is MessageDeletedEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, event.user)
                    batch.addMessageData(event.cid, event.message)
                }
                is MessageUpdatedEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, event.user)
                    batch.addMessageData(event.cid, event.message)
                }
                is NotificationMessageNewEvent -> {
                    event.message.enrichWithCid(event.cid)
                    updateTotalUnreadCountsIfNeeded(
                        totalUnreadCount = event.totalUnreadCount,
                        channelUnreadCount = event.unreadChannels,
                        isFromSync = isFromSync,
                        cid = event.cid,
                    )
                    batch.addMessageData(event.cid, event.message, isNewMessage = true)
                    batch.addChannel(event.channel.copy(hidden = false))
                }
                is NotificationAddedToChannelEvent -> {
                    batch.addChannel(event.channel)
                }
                is NotificationInvitedEvent -> {
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                }
                is NotificationInviteAcceptedEvent -> {
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                    batch.addChannel(event.channel)
                }
                is NotificationInviteRejectedEvent -> {
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                    batch.addChannel(event.channel)
                }
                is ChannelHiddenEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        val updatedChannel = it.apply {
                            hidden = true
                            hiddenMessagesBefore = event.createdAt.takeIf { event.clearHistory }
                        }
                        batch.addChannel(updatedChannel)
                    }
                }
                is ChannelVisibleEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { hidden = false })
                    }
                }
                is NotificationMutesUpdatedEvent -> {
                    updateCurrentUser(event.me)
                }

                is ReactionNewEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, event.user)
                    batch.addMessage(event.message)
                }
                is ReactionDeletedEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, event.user)
                    batch.addMessage(event.message)
                }
                is ReactionUpdateEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, event.user)
                    batch.addMessage(event.message)
                }
                is MemberAddedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.member.user.id, event.member) })
                    }
                }
                is MemberUpdatedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.member.user.id, event.member) })
                    }
                }
                is MemberRemovedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.user.id, null) })
                    }
                }
                is NotificationRemovedFromChannelEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        event.user?.let { user ->
                            channel.setMember(user.id, null)
                        }
                        batch.addChannel(channel)
                    }
                }
                is ChannelUpdatedEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelUpdatedByUserEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelDeletedEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelTruncatedEvent -> {
                    batch.addChannel(event.channel)
                }
                is NotificationChannelDeletedEvent -> {
                    batch.addChannel(event.channel)
                }
                is NotificationChannelMutesUpdatedEvent -> {
                    updateCurrentUser(event.me)
                }
                is NotificationChannelTruncatedEvent -> {
                    batch.addChannel(event.channel)
                }

                // we use syncState to store the last markAllRead date for a given
                // user since it makes more sense to write to the database once instead of N times.
                is MarkAllReadEvent -> {
                    mutableGlobalState._totalUnreadCount.value = event.totalUnreadCount
                    mutableGlobalState._channelUnreadCount.value = event.unreadChannels

                    // only update sync state if the incoming "mark all read" date is newer
                    // this supports using event handler to restore mark all read state in setUser
                    // without redundant db writes.

                    syncManager.updateAllReadStateForDate(event.user.id, event.createdAt)
                }

                // get the channel, update reads, write the channel
                is MessageReadEvent ->
                    batch.getCurrentChannel(event.cid)
                        ?.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                        ?.let(batch::addChannel)

                is NotificationMarkReadEvent -> {
                    updateTotalUnreadCountsIfNeeded(
                        totalUnreadCount = event.totalUnreadCount,
                        channelUnreadCount = event.unreadChannels,
                        isFromSync = isFromSync,
                        cid = event.cid,
                    )
                    batch.getCurrentChannel(event.cid)
                        ?.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                        ?.let(batch::addChannel)
                }
                is GlobalUserBannedEvent -> {
                    batch.addUser(event.user.apply { banned = true })
                }
                is GlobalUserUnbannedEvent -> {
                    batch.addUser(event.user.apply { banned = false })
                }
                is UserUpdatedEvent -> {
                    event.user
                        .takeIf { it.id == mutableGlobalState._user.value?.id }
                        ?.let {
                            updateCurrentUser(it)
                        }
                }
                is TypingStartEvent,
                is TypingStopEvent,
                is HealthEvent,
                is ConnectingEvent,
                is DisconnectedEvent,
                is ErrorEvent,
                is UnknownEvent,
                is ChannelUserBannedEvent,
                is ChannelUserUnbannedEvent,
                is UserDeletedEvent,
                is UserStartWatchingEvent,
                is UserStopWatchingEvent,
                is UserPresenceChangedEvent,
                is ConnectedEvent,
                -> Unit
            }
        }

        // execute the batch
        batch.execute()

        // handle delete and truncate events
        for (event in events) {
            when (event) {
                is NotificationChannelTruncatedEvent -> {
                    repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                }
                is ChannelTruncatedEvent -> {
                    repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                }
                is ChannelDeletedEvent -> {
                    repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                    repos.setChannelDeletedAt(event.cid, event.createdAt)
                }
                is MessageDeletedEvent -> {
                    if (event.hardDelete) {
                        repos.deleteChannelMessage(event.message)
                        repos.evictChannel(event.cid)
                    }
                }
                else -> Unit // Ignore other events
            }
        }
    }

    private suspend fun handleEventsInternal(events: List<ChatEvent>, isFromSync: Boolean) {
        events.forEach { chatEvent ->
            logger.logD("Received event: $chatEvent")
        }

        val sortedEvents = events.sortedBy { it.createdAt }
        updateOfflineStorageFromEvents(sortedEvents, isFromSync)

        // step 3 - forward the events to the active channels
        sortedEvents.filterIsInstance<CidEvent>()
            .groupBy { it.cid }
            .forEach { (cid, eventList) ->
                val (channelType, channelId) = cid.cidToTypeAndId()
                if (logic.isActiveChannel(channelType = channelType, channelId = channelId)) {
                    logic.channel(channelType = channelType, channelId = channelId).handleEvents(eventList)
                }
            }

        // mark all read applies to all channels
        sortedEvents.filterIsInstance<MarkAllReadEvent>().firstOrNull()?.let { markAllRead ->
            handleChannelControllerEvent(markAllRead)
        }

        // mutes are user related, so they have to be propagated to all channels
        sortedEvents.filterIsInstance<NotificationChannelMutesUpdatedEvent>().lastOrNull()?.let { event ->
            handleChannelControllerEvent(event)
        }

        // User presence change applies to all active channels with that user
        sortedEvents.find { it is UserPresenceChangedEvent }?.let { userPresenceChanged ->
            val event = userPresenceChanged as UserPresenceChangedEvent

            state.getActiveChannelStates()
                .filter { channelState ->
                    channelState.members.value
                        .map { member -> member.user.id }
                        .contains(event.user.id)
                }
                .forEach { channelState ->
                    logic.channel(channelType = channelState.channelType, channelId = channelState.channelId)
                        .handleEvent(userPresenceChanged)
                }
        }

        // only afterwards forward to the queryRepo since it borrows some data from the channel
        // queryRepo mainly monitors for the notification added to channel event
        for (queryChannelsLogic in logic.getActiveQueryChannelsLogic()) {
            queryChannelsLogic.handleEvents(events)
        }
    }

    private fun handleChannelControllerEvent(event: ChatEvent) {
        logic.getActiveChannelsLogic().forEach { channelLogic ->
            channelLogic.handleEvent(event)
        }
    }

    /**
     * Updates total unread count and channels unread count if channel has [ChannelCapabilities.READ_EVENTS] and event doesn't
     * come from sync endpoint.
     * @see [shouldUpdateTotalUnreadCounts]
     *
     * @param totalUnreadCount The new total unread messages count.
     * @param channelUnreadCount The new total channels unread count.
     * @param isFromSync Flag to determine if event comes from sync endpoint
     * @param cid CID of the channel.
     */
    private suspend fun updateTotalUnreadCountsIfNeeded(
        totalUnreadCount: Int,
        channelUnreadCount: Int,
        isFromSync: Boolean,
        cid: String,
    ) {
        if (shouldUpdateTotalUnreadCounts(isFromSync, cid)) {
            mutableGlobalState._totalUnreadCount.value = totalUnreadCount
            mutableGlobalState._channelUnreadCount.value = channelUnreadCount
        }
    }

    /**
     * Checks if unread counts should be updated for particular channel.
     * The unread counts shouldn't be updated if the event comes from the sync endpoint, because those ones are not
     * enriched with unread info, or if channel in the DB doesn't contain [ChannelCapabilities.READ_EVENTS] capability.
     *
     * @param isFromSync Flag to determine if event comes from sync endpoint
     * @param cid CID of the channel.
     *
     * @return True if unread counts should be updated
     */
    private suspend fun shouldUpdateTotalUnreadCounts(isFromSync: Boolean, cid: String): Boolean {
        if (isFromSync) {
            return false
        }

        return repos.selectChannels(listOf(cid)).let { channels ->
            val channel = channels.firstOrNull()
            if (channel?.ownCapabilities?.contains(ChannelCapabilities.READ_EVENTS) == true) {
                true
            } else {
                logger.logD("Skipping unread counts update for channel: $cid. ${ChannelCapabilities.READ_EVENTS} capability is missing.")
                false
            }
        }
    }

    private fun Message.enrichWithOwnReactions(batch: EventBatchUpdate, user: User?) {
        ownReactions = if (user != null && mutableGlobalState._user.value?.id != user.id) {
            batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
        } else {
            mergeReactions(
                latestReactions.filter { it.userId == mutableGlobalState._user.value?.id ?: "" },
                batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
            ).toMutableList()
        }
    }

    private suspend fun updateCurrentUser(me: User) {
        val currentUser = mutableGlobalState.user.value
        if (me.id != currentUser?.id) {
            throw InputMismatchException("received connect event for user with id ${me.id} while chat domain is configured for user with id ${currentUser?.id}. create a new ChatDomain when connecting a different user.")
        }

        mutableGlobalState.run {
            _user.value = me
            _mutedUsers.value = me.mutes
            _channelMutes.value = me.channelMutes
            _totalUnreadCount.value = me.totalUnreadCount
            _channelUnreadCount.value = me.unreadChannels
            _banned.value = me.banned
        }

        repos.insertCurrentUser(me)
    }

    companion object {
        val EMPTY_DISPOSABLE = object : Disposable {
            override val isDisposed: Boolean = true
            override fun dispose() {}
        }
    }
}
