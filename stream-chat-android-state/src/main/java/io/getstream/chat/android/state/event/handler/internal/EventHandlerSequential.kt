/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.state.event.handler.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatEventListener
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
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HasMessage
import io.getstream.chat.android.client.events.HasOwnUser
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
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.addMember
import io.getstream.chat.android.client.extensions.internal.addMembership
import io.getstream.chat.android.client.extensions.internal.mergeReactions
import io.getstream.chat.android.client.extensions.internal.removeMember
import io.getstream.chat.android.client.extensions.internal.removeMembership
import io.getstream.chat.android.client.extensions.internal.updateMember
import io.getstream.chat.android.client.extensions.internal.updateMemberBanned
import io.getstream.chat.android.client.extensions.internal.updateMembership
import io.getstream.chat.android.client.extensions.internal.updateMembershipBanned
import io.getstream.chat.android.client.extensions.internal.updateReads
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.event.handler.internal.batch.BatchEvent
import io.getstream.chat.android.state.event.handler.internal.batch.SocketEventCollector
import io.getstream.chat.android.state.event.handler.internal.model.SelfUserFull
import io.getstream.chat.android.state.event.handler.internal.model.SelfUserPart
import io.getstream.chat.android.state.event.handler.internal.utils.updateCurrentUser
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.log.StreamLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.InputMismatchException

private const val TAG = "Chat:EventHandlerSeq"
private const val TAG_SOCKET = "Chat:SocketEvent"
private const val EVENTS_BUFFER = 100

/**
 * Processes events sequentially. That means a new event will not be processed
 * until the previous event processing is not completed.
 */
@Suppress("LongParameterList", "TooManyFunctions")
internal class EventHandlerSequential(
    private val currentUserId: UserId,
    private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val mutableGlobalState: MutableGlobalState,
    private val repos: RepositoryFacade,
    private val sideEffect: suspend () -> Unit,
    private val syncedEvents: Flow<List<ChatEvent>>,
    scope: CoroutineScope,
) : EventHandler {

    private val logger = StreamLog.getLogger(TAG)
    private val scope = scope + SupervisorJob() + CoroutineExceptionHandler { context, throwable ->
        logger.e(throwable) { "[uncaughtCoroutineException] throwable: $throwable, context: $context" }
    }

    private val mutex = Mutex()
    private val socketEvents = MutableSharedFlow<ChatEvent>(extraBufferCapacity = EVENTS_BUFFER)
    private val socketEventCollector = SocketEventCollector(scope) { batchEvent ->
        handleBatchEvent(batchEvent)
    }

    private var eventsDisposable: Disposable = EMPTY_DISPOSABLE

    init {
        logger.d { "<init> no args" }
    }

    /**
     * Start listening to chat events.
     */
    override fun startListening() {
        val isDisposed = eventsDisposable.isDisposed
        logger.i { "[startListening] isDisposed: $isDisposed, currentUserId: $currentUserId" }
        if (isDisposed) {
            val initJob = scope.launch {
                repos.cacheChannelConfigs()
                logger.v { "[startListening] initialization completed" }
            }
            scope.launch {
                syncedEvents.collect {
                    logger.i { "[onSyncEventsReceived] events.size: ${it.size}" }
                    handleBatchEvent(
                        BatchEvent(sortedEvents = it, isFromHistorySync = true)
                    )
                }
            }
            scope.launch {
                socketEvents.collect { event ->
                    initJob.join()
                    sideEffect()
                    socketEventCollector.collect(event)
                }
            }
            eventsDisposable = subscribeForEvents { event ->
                if (socketEvents.tryEmit(event)) {
                    StreamLog.v(TAG_SOCKET) { "[onSocketEventReceived] event.type: ${event.type}" }
                } else {
                    StreamLog.e(TAG_SOCKET) { "[onSocketEventReceived] failed to emit socket event: $event" }
                }
            }
        }
    }

    /**
     * Stop listening for events.
     */
    override fun stopListening() {
        logger.i { "[stopListening] no args" }
        eventsDisposable.dispose()
        scope.coroutineContext.job.cancelChildren()
    }

    private suspend fun handleChatEvents(eventList: List<ChatEvent>, queryChannelsLogic: QueryChannelsLogic) {
        eventList.forEach { event -> handleChatEvent(event, queryChannelsLogic) }
    }

    private suspend fun handleChatEvent(event: ChatEvent, queryChannelsLogic: QueryChannelsLogic) {
        // update the info for that channel from the channel repo
        logger.i { "[handleEvent] event: $event" }

        when (val handlingResult = queryChannelsLogic.parseChatEventResult(event)) {
            is EventHandlingResult.Add -> queryChannelsLogic.addChannel(handlingResult.channel)
            is EventHandlingResult.WatchAndAdd -> queryChannelsLogic.watchAndAddChannel(handlingResult.cid)
            is EventHandlingResult.Remove -> queryChannelsLogic.removeChannel(handlingResult.cid)
            is EventHandlingResult.Skip -> Unit
        }

        if (event is MarkAllReadEvent) {
            queryChannelsLogic.refreshAllChannelsState()
        }

        if (event is CidEvent) {
            // skip events that are typically not impacting the query channels overview
            if (event is UserStartWatchingEvent || event is UserStopWatchingEvent) {
                return
            }

            queryChannelsLogic.refreshChannelState(event.cid)
        }

        if (event is UserPresenceChangedEvent) {
            queryChannelsLogic.refreshMembersStateForUser(event.user)
        }
    }

    /**
     * For testing purpose only. Simulates socket event handling.
     */
    @VisibleForTesting
    override suspend fun handleEvents(vararg events: ChatEvent) {
        val batchEvent = BatchEvent(sortedEvents = events.toList(), isFromHistorySync = false)
        handleBatchEvent(batchEvent)
    }

    private suspend fun handleBatchEvent(event: BatchEvent) = mutex.withLock {
        try {
            logger.d {
                "[handleBatchEvent] >>> id: ${event.id}, fromSocket: ${event.isFromSocketConnection}" +
                    ", size: ${event.size}, event.types: '${event.sortedEvents.joinToString { it.type }}'"
            }
            updateGlobalState(event)
            updateChannelsState(event)
            updateOfflineStorage(event)
            updateThreadState(event)
            logger.v { "[handleBatchEvent] <<< id: ${event.id}" }
        } catch (e: Throwable) {
            logger.e(e) { "[handleBatchEvent] failed(${event.id}): ${e.message}" }
        }
    }

    private suspend fun updateGlobalState(batchEvent: BatchEvent) {
        logger.v { "[updateGlobalState] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" }
        batchEvent.sortedEvents.forEach { event: ChatEvent ->
            // connection events are never send on the recovery endpoint, so handle them 1 by 1
            when (event) {
                is ConnectedEvent -> if (batchEvent.isFromSocketConnection) {
                    event.me.id mustBe currentUserId
                    mutableGlobalState.updateCurrentUser(SelfUserFull(event.me))
                }
                is NotificationMutesUpdatedEvent -> {
                    event.me.id mustBe currentUserId
                    mutableGlobalState.updateCurrentUser(SelfUserFull(event.me))
                }
                is NotificationChannelMutesUpdatedEvent -> {
                    event.me.id mustBe currentUserId
                    mutableGlobalState.updateCurrentUser(SelfUserFull(event.me))
                }
                is UserUpdatedEvent -> if (event.user.id == currentUserId) {
                    mutableGlobalState.updateCurrentUser(SelfUserPart(event.user))
                }
                is MarkAllReadEvent -> {
                    mutableGlobalState.setTotalUnreadCount(event.totalUnreadCount)
                    mutableGlobalState.setChannelUnreadCount(event.unreadChannels)
                }
                is NotificationMessageNewEvent -> if (batchEvent.isFromSocketConnection) {
                    // can we somehow get rid of repos usage here?
                    if (repos.hasReadEventsCapability(event.cid)) {
                        mutableGlobalState.setTotalUnreadCount(event.totalUnreadCount)
                        mutableGlobalState.setChannelUnreadCount(event.unreadChannels)
                    }
                }
                is NotificationMarkReadEvent -> if (batchEvent.isFromSocketConnection) {
                    // can we somehow get rid of repos usage here?
                    if (repos.hasReadEventsCapability(event.cid)) {
                        mutableGlobalState.setTotalUnreadCount(event.totalUnreadCount)
                        mutableGlobalState.setChannelUnreadCount(event.unreadChannels)
                    }
                }
                is NewMessageEvent -> if (batchEvent.isFromSocketConnection) {
                    // can we somehow get rid of repos usage here?
                    if (repos.hasReadEventsCapability(event.cid)) {
                        mutableGlobalState.setTotalUnreadCount(event.totalUnreadCount)
                        mutableGlobalState.setChannelUnreadCount(event.unreadChannels)
                    }
                }
                else -> Unit
            }
        }
    }

    private suspend fun updateChannelsState(batchEvent: BatchEvent) {
        logger.v { "[updateChannelsState] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" }
        val sortedEvents: List<ChatEvent> = batchEvent.sortedEvents

        // step 3 - forward the events to the active channels
        sortedEvents.filterIsInstance<CidEvent>()
            .groupBy { it.cid }
            .forEach { (cid, events) ->
                val (channelType, channelId) = cid.cidToTypeAndId()
                if (logicRegistry.isActiveChannel(channelType = channelType, channelId = channelId)) {
                    val channelLogic: ChannelLogic = logicRegistry.channel(
                        channelType = channelType,
                        channelId = channelId
                    )
                    channelLogic.handleEvents(events)
                }
            }

        // mark all read applies to all channels
        sortedEvents.filterIsInstance<MarkAllReadEvent>().firstOrNull()?.let { markAllRead ->
            logicRegistry.getActiveChannelsLogic().forEach { channelLogic: ChannelLogic ->
                channelLogic.handleEvent(markAllRead)
            }
        }

        // mutes are user related, so they have to be propagated to all channels
        sortedEvents.filterIsInstance<NotificationChannelMutesUpdatedEvent>().lastOrNull()?.let { event ->
            logicRegistry.getActiveChannelsLogic().forEach { channelLogic: ChannelLogic ->
                channelLogic.handleEvent(event)
            }
        }

        // User presence change applies to all active channels with that user
        sortedEvents.find { it is UserPresenceChangedEvent }?.let { userPresenceChanged ->
            val event = userPresenceChanged as UserPresenceChangedEvent

            stateRegistry.getActiveChannelStates()
                .filter { channelState -> channelState.members.containsWithUserId(event.user.id) }
                .forEach { channelState ->
                    val channelLogic: ChannelLogic = logicRegistry.channel(
                        channelType = channelState.channelType,
                        channelId = channelState.channelId
                    )
                    channelLogic.handleEvent(userPresenceChanged)
                }
        }

        // only afterwards forward to the queryRepo since it borrows some data from the channel
        // queryRepo mainly monitors for the notification added to channel event
        logicRegistry.getActiveQueryChannelsLogic().map { channelsLogic ->
            scope.async {
                handleChatEvents(sortedEvents, channelsLogic)
            }
        }.awaitAll()
    }

    private fun updateThreadState(batchEvent: BatchEvent) {
        logger.v { "[updateThreadState] batchEvent.size: ${batchEvent.size}" }
        val sortedEvents: List<ChatEvent> = batchEvent.sortedEvents
        sortedEvents.filterIsInstance<HasMessage>()
            .groupBy { it.message.parentId ?: it.message.id }
            .filterKeys(logicRegistry::isActiveThread)
            .forEach { (messageId, events) ->
                logicRegistry.thread(messageId).handleEvents(events)
            }
    }

    private suspend fun updateOfflineStorage(batchEvent: BatchEvent) {
        logger.v { "[updateOfflineStorage] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size} " }
        val events = batchEvent.sortedEvents
        val batchBuilder = EventBatchUpdate.Builder(batchEvent.id)
        batchBuilder.addToFetchChannels(events.filterIsInstance<CidEvent>().map { it.cid })

        val users: List<User> = events.filterIsInstance<UserEvent>().map { it.user } +
            events.filterIsInstance<HasOwnUser>().map { it.me }

        batchBuilder.addUsers(users)

        // step 1. see which data we need to retrieve from offline storage

        val messageIds = events.extractMessageIds()
        batchBuilder.addToFetchMessages(messageIds)

        // actually fetch the data
        StreamLog.d(TAG) { "batchBuilder.build" }
        val batch = batchBuilder.build(mutableGlobalState, repos, currentUserId)

        // step 2. second pass through the events, make a list of what we need to update
        StreamLog.d(TAG) { "event handling" }
        for (event in events) {
            when (event) {
                is ConnectedEvent -> if (batchEvent.isFromSocketConnection) {
                    event.me.id mustBe currentUserId
                    StreamLog.d(TAG) { "insertCurrentUser" }
                    repos.insertCurrentUser(event.me)
                    StreamLog.d(TAG) { "insertCurrentUser - complete" }
                }
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessageData(event.cid, event.message, isNewMessage = true)
                    repos.selectChannelWithoutMessages(event.cid)?.let { channel ->
                        val updatedChannel = channel.copy(
                            hidden = false,
                            messages = listOf(event.message)
                        )
                        batch.addChannel(updatedChannel)
                    }
                }
                is MessageDeletedEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessageData(event.cid, event.message)
                }
                is MessageUpdatedEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessageData(event.cid, event.message)
                }
                is NotificationMessageNewEvent -> {
                    event.message.enrichWithCid(event.cid)
                    batch.addMessageData(event.cid, event.message, isNewMessage = true)
                    batch.addChannel(event.channel.copy(hidden = false))
                }
                is NotificationAddedToChannelEvent -> {
                    batch.addChannel(
                        event.channel.addMembership(currentUserId, event.member)
                    )
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
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }

                is ReactionNewEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessage(event.message)
                }
                is ReactionDeletedEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessage(event.message)
                }
                is ReactionUpdateEvent -> {
                    event.message.enrichWithCid(event.cid)
                    event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessage(event.message)
                }
                is ChannelUserBannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = true)
                                .updateMembershipBanned(event.user.id, banned = true)
                        )
                    }
                }
                is ChannelUserUnbannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = false)
                                .updateMembershipBanned(event.user.id, banned = false)
                        )
                    }
                }
                is MemberAddedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.addMember(event.member)
                        )
                    }
                }
                is MemberUpdatedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMember(event.member)
                                .updateMembership(event.member)
                        )
                    }
                }
                is MemberRemovedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.removeMember(event.user.id)
                                .removeMembership(currentUserId)
                        )
                    }
                }
                is NotificationRemovedFromChannelEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.removeMembership(currentUserId).apply {
                                memberCount = event.channel.memberCount
                                members = event.channel.members
                            }
                        )
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
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }
                is NotificationChannelTruncatedEvent -> {
                    batch.addChannel(event.channel)
                }

                // get the channel, update reads, write the channel
                is MessageReadEvent ->
                    batch.getCurrentChannel(event.cid)
                        ?.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                        ?.let(batch::addChannel)

                is NotificationMarkReadEvent -> {
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
                is UserUpdatedEvent -> if (event.user.id == currentUserId) {
                    repos.insertCurrentUser(event.user)
                }
                else -> Unit
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

    private fun List<ChatEvent>.extractMessageIds() = mapNotNull { event ->
        when (event) {
            is ReactionNewEvent -> event.reaction.messageId
            is ReactionDeletedEvent -> event.reaction.messageId
            is MessageDeletedEvent -> event.message.id
            is MessageUpdatedEvent -> event.message.id
            is NewMessageEvent -> event.message.id
            is NotificationMessageNewEvent -> event.message.id
            is ReactionUpdateEvent -> event.message.id
            else -> null
        }
    }

    private fun StateFlow<List<Member>>.containsWithUserId(userId: String): Boolean {
        return value.find { it.user.id == userId } != null
    }

    /**
     * Checks if unread counts should be updated for particular channel.
     * The unread counts should not be updated if channel in the DB
     * does not contain [ChannelCapabilities.READ_EVENTS] capability.
     *
     * @param cid CID of the channel.
     *
     * @return True if unread counts should be updated
     */
    private suspend fun RepositoryFacade.hasReadEventsCapability(cid: String): Boolean {
        return selectChannels(listOf(cid)).let { channels ->
            val channel = channels.firstOrNull()
            if (channel?.ownCapabilities?.contains(ChannelCapabilities.READ_EVENTS) == true) {
                true
            } else {
                logger.d {
                    "Skipping unread counts update for channel: $cid. ${ChannelCapabilities.READ_EVENTS} capability is missing."
                }
                false
            }
        }
    }

    private fun Message.enrichWithOwnReactions(batch: EventBatchUpdate, currentUserId: UserId, eventUser: User?) {
        ownReactions = if (eventUser != null && currentUserId != eventUser.id) {
            batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
        } else {
            mergeReactions(
                latestReactions.filter { it.userId == currentUserId },
                batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
            ).toMutableList()
        }
    }

    private infix fun UserId.mustBe(currentUserId: UserId?) {
        if (this != currentUserId) {
            throw InputMismatchException(
                "received connect event for user with id $this while for user configured " +
                    "has id $currentUserId. Looks like there's a problem in the user set"
            )
        }
    }

    companion object {
        val EMPTY_DISPOSABLE = object : Disposable {
            override val isDisposed: Boolean = true
            override fun dispose() = Unit
        }
    }
}
