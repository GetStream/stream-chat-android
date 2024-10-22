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
import io.getstream.chat.android.client.events.HasPoll
import io.getstream.chat.android.client.events.HasUnreadCounts
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
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.addMember
import io.getstream.chat.android.client.extensions.internal.addMembership
import io.getstream.chat.android.client.extensions.internal.enrichIfNeeded
import io.getstream.chat.android.client.extensions.internal.mergeReactions
import io.getstream.chat.android.client.extensions.internal.removeMember
import io.getstream.chat.android.client.extensions.internal.removeMembership
import io.getstream.chat.android.client.extensions.internal.updateMember
import io.getstream.chat.android.client.extensions.internal.updateMemberBanned
import io.getstream.chat.android.client.extensions.internal.updateMembership
import io.getstream.chat.android.client.extensions.internal.updateMembershipBanned
import io.getstream.chat.android.client.extensions.internal.updateReads
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.mergePartially
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.lazy.parameterizedLazy
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.mergeChannelFromEvent
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.event.handler.internal.batch.BatchEvent
import io.getstream.chat.android.state.event.handler.internal.batch.SocketEventCollector
import io.getstream.chat.android.state.event.handler.internal.utils.realType
import io.getstream.chat.android.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
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
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "Chat:EventHandlerSeq"
private const val TAG_SOCKET = "Chat:SocketEvent"

/**
 * Processes events sequentially. That means a new event will not be processed
 * until the previous event processing is not completed.
 */
@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
internal class EventHandlerSequential(
    private val currentUserId: UserId,
    private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val clientState: ClientState,
    private val mutableGlobalState: MutableGlobalState,
    private val repos: RepositoryFacade,
    private val sideEffect: suspend () -> Unit,
    private val syncedEvents: Flow<List<ChatEvent>>,
    scope: CoroutineScope,
) : EventHandler {

    private val logger by taggedLogger(TAG)
    private val scope = scope + SupervisorJob() + CoroutineExceptionHandler { context, throwable ->
        logger.e(throwable) { "[uncaughtCoroutineException] throwable: $throwable, context: $context" }
    }

    private val mutex = Mutex()
    private val socketEvents = MutableSharedFlow<ChatEvent>(extraBufferCapacity = Int.MAX_VALUE)
    private val socketEventCollector = SocketEventCollector(scope) { batchEvent ->
        handleBatchEvent(batchEvent)
    }

    private var eventsDisposable: Disposable = EMPTY_DISPOSABLE

    init {
        logger.d { "<init> no args" }
    }

    private val emittedCount = AtomicInteger()
    private val collectedCount = AtomicInteger()

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
                        BatchEvent(sortedEvents = it, isFromHistorySync = true),
                    )
                }
            }
            scope.launch {
                socketEvents.collect { event ->
                    collectedCount.incrementAndGet()
                    initJob.join()
                    sideEffect()
                    socketEventCollector.collect(event)
                }
            }
            eventsDisposable = subscribeForEvents { event ->
                if (socketEvents.tryEmit(event)) {
                    val cCount = collectedCount.get()
                    val eCount = emittedCount.incrementAndGet()
                    val ratio = eCount.toDouble() / cCount.toDouble()
                    StreamLog.v(TAG_SOCKET) {
                        "[onSocketEventReceived] event.type: ${event.realType}; $eCount => $cCount ($ratio)"
                    }
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

    private suspend fun handleChatEvents(batchEvent: BatchEvent, queryChannelsLogic: QueryChannelsLogic) {
        logger.v { "[handleChatEvents] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" }
        queryChannelsLogic.parseChatEventResults(batchEvent.sortedEvents).forEach { result ->
            when (result) {
                is EventHandlingResult.Add -> queryChannelsLogic.addChannel(result.channel)
                is EventHandlingResult.WatchAndAdd -> queryChannelsLogic.watchAndAddChannel(result.cid)
                is EventHandlingResult.Remove -> queryChannelsLogic.removeChannel(result.cid)
                is EventHandlingResult.Skip -> Unit
            }
        }

        val hasMarkAllReadEvent = batchEvent.sortedEvents.lastOrNull { it is MarkAllReadEvent } != null
        if (hasMarkAllReadEvent) {
            queryChannelsLogic.refreshAllChannelsState()
        }

        val cids = batchEvent.sortedEvents
            .filterIsInstance<CidEvent>()
            // skip events that are typically not impacting the query channels overview
            .filterNot { it is UserStartWatchingEvent || it is UserStopWatchingEvent }
            .map { it.cid }
            .distinct()
        if (cids.isNotEmpty()) {
            queryChannelsLogic.refreshChannelsState(cids)
        }

        val event = batchEvent.sortedEvents.filterIsInstance<UserPresenceChangedEvent>().lastOrNull()
        if (event is UserPresenceChangedEvent) {
            queryChannelsLogic.refreshMembersStateForUser(event.user)
        }

        logger.v { "[handleChatEvents] completed batchId: ${batchEvent.id}" }
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
                    ", size: ${event.size}, event.types: '${event.sortedEvents.joinToString { it.realType }}'"
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

    @SuppressWarnings("LongMethod", "NestedBlockDepth")
    private suspend fun updateGlobalState(batchEvent: BatchEvent) {
        logger.v { "[updateGlobalState] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" }

        var me = clientState.user.value
        var totalUnreadCount = mutableGlobalState.totalUnreadCount.value
        var channelUnreadCount = mutableGlobalState.channelUnreadCount.value
        var unreadThreadsCount = mutableGlobalState.unreadThreadsCount.value

        val modifyValuesFromEvent = { event: HasUnreadCounts ->
            totalUnreadCount = event.totalUnreadCount
            channelUnreadCount = event.unreadChannels
        }

        val modifyValuesFromUser = { user: User ->
            me = user
            totalUnreadCount = user.totalUnreadCount
            channelUnreadCount = user.unreadChannels
            unreadThreadsCount = user.unreadThreads
        }

        val hasReadEventsCapability = parameterizedLazy<String, Boolean> { cid ->
            // can we somehow get rid of repos usage here?
            repos.hasReadEventsCapability(cid)
        }

        batchEvent.sortedEvents.forEach { event: ChatEvent ->
            // connection events are never send on the recovery endpoint, so handle them 1 by 1
            when (event) {
                is ConnectedEvent -> if (batchEvent.isFromSocketConnection && event.me.id == currentUserId) {
                    modifyValuesFromUser(event.me)
                }
                is NotificationMutesUpdatedEvent -> if (event.me.id == currentUserId) {
                    modifyValuesFromUser(event.me)
                }
                is NotificationChannelMutesUpdatedEvent -> if (event.me.id == currentUserId) {
                    modifyValuesFromUser(event.me)
                }
                is UserUpdatedEvent -> if (event.user.id == currentUserId) {
                    modifyValuesFromUser(me?.mergePartially(event.user) ?: event.user)
                }
                is MarkAllReadEvent -> {
                    modifyValuesFromEvent(event)
                }
                is NotificationMessageNewEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                is NotificationMarkReadEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                is NotificationMarkUnreadEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                is NewMessageEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                else -> Unit
            }
        }

        me?.let {
            mutableGlobalState.setBanned(it.isBanned)
            mutableGlobalState.setMutedUsers(it.mutes)
            mutableGlobalState.setChannelMutes(it.channelMutes)
        }
        mutableGlobalState.setTotalUnreadCount(totalUnreadCount)
        mutableGlobalState.setChannelUnreadCount(channelUnreadCount)
        mutableGlobalState.setUnreadThreadsCount(unreadThreadsCount)
        logger.v { "[updateGlobalState] completed batchId: ${batchEvent.id}" }
    }

    private suspend fun updateChannelsState(batchEvent: BatchEvent) {
        val first = batchEvent.sortedEvents.firstOrNull()
        val last = batchEvent.sortedEvents.lastOrNull()
        val firstDate = first?.createdAt
        val lastDate = last?.createdAt
        val firstLessLast = firstDate?.let { lastDate?.let { firstDate < lastDate } }
        logger.v {
            "[updateChannelsState] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" +
                ", first(${first?.seq}) < last(${last?.seq}): $firstLessLast"
        }
        val sortedEvents: List<ChatEvent> = batchEvent.sortedEvents

        stateRegistry.handleBatchEvent(batchEvent)

        // step 3 - forward the events to the active channels
        sortedEvents.filterIsInstance<CidEvent>()
            .groupBy { it.cid }
            .forEach { (cid, events) ->
                val (channelType, channelId) = cid.cidToTypeAndId()
                if (events.any { it is ChannelDeletedEvent || it is NotificationChannelDeletedEvent }) {
                    logicRegistry.removeChannel(channelType, channelId)
                }
                if (logicRegistry.isActiveChannel(channelType = channelType, channelId = channelId)) {
                    val channelLogic: ChannelLogic = logicRegistry.channel(
                        channelType = channelType,
                        channelId = channelId,
                    )
                    channelLogic.handleEvents(events)
                }
            }

        // mark all read applies to all channels
        sortedEvents.filterIsInstance<MarkAllReadEvent>().lastOrNull()?.let { markAllRead ->
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
                        channelId = channelState.channelId,
                    )
                    channelLogic.handleEvent(userPresenceChanged)
                }
        }

        // only afterwards forward to the queryRepo since it borrows some data from the channel
        // queryRepo mainly monitors for the notification added to channel event
        logicRegistry.getActiveQueryChannelsLogic().map { channelsLogic ->
            scope.async {
                handleChatEvents(batchEvent, channelsLogic)
            }
        }.awaitAll()
        logger.v { "[updateChannelsState] completed batchId: ${batchEvent.id}" }
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
        logger.v { "[updateThreadState] completed batchId: ${batchEvent.id}" }
    }

    private suspend fun updateOfflineStorage(batchEvent: BatchEvent) {
        logger.v { "[updateOfflineStorage] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size} " }
        val events = batchEvent.sortedEvents.map { it.enrichIfNeeded() }
        val batchBuilder = EventBatchUpdate.Builder(batchEvent.id)
        val cidEvents = events.filterIsInstance<CidEvent>()
        val pollEvents = events.filterIsInstance<HasPoll>()
        batchBuilder.addToFetchChannels(
            cidEvents
                .filterNot { it is ChannelDeletedEvent || it is NotificationChannelDeletedEvent }
                .map { it.cid },
        )

        batchBuilder.addToRemoveChannels(
            cidEvents
                .filter { it is ChannelDeletedEvent || it is NotificationChannelDeletedEvent }
                .map { it.cid },
        )
        pollEvents.forEach { batchBuilder.addPollToFetch(it.poll.id) }

        val users: List<User> = events.filterIsInstance<UserEvent>().map { it.user } +
            events.filterIsInstance<HasOwnUser>().map { it.me }

        batchBuilder.addUsers(users)

        // step 1. see which data we need to retrieve from offline storage

        val messageIds = events.extractMessageIds()
        batchBuilder.addToFetchMessages(messageIds)

        // actually fetch the data
        val batch = batchBuilder.build(mutableGlobalState, repos, currentUserId)

        // step 2. second pass through the events, make a list of what we need to update
        for (event in events) {
            when (event) {
                is ConnectedEvent -> if (batchEvent.isFromSocketConnection) {
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent -> {
                    val enrichedMessage = event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessageData(event.createdAt, event.cid, enrichedMessage)
                    // first check channel in batch, if not found, check in db
                    //  if we ignore batch, we may override existing channel in batch with the one from db
                    val channel = batch.getCurrentChannel(event.cid) ?: repos.selectChannel(event.cid)
                    if (channel == null) {
                        logger.w { "[updateOfflineStorage] #new_message; (now channel found for ${event.cid})" }
                        continue
                    }
                    val updatedChannel = channel.copy(
                        hidden = channel.hidden.takeIf { enrichedMessage.shadowed } ?: false,
                        messages = channel.messages + listOf(enrichedMessage),
                    )
                    batch.addChannel(updatedChannel)
                }
                is MessageDeletedEvent -> {
                    batch.addMessageData(
                        event.createdAt,
                        event.cid,
                        event.message.enrichWithOwnReactions(batch, currentUserId, event.user),
                    )
                }
                is MessageUpdatedEvent -> {
                    batch.addMessageData(
                        event.createdAt,
                        event.cid,
                        event.message.enrichWithOwnReactions(batch, currentUserId, event.user),
                    )
                }
                is NotificationMessageNewEvent -> {
                    batch.addMessageData(event.createdAt, event.cid, event.message)
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel.copy(hidden = false))
                }
                is NotificationAddedToChannelEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(
                        channel.addMembership(currentUserId, event.member),
                    )
                }
                is NotificationInvitedEvent -> {
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                }
                is NotificationInviteAcceptedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                    batch.addChannel(channel)
                }
                is NotificationInviteRejectedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                    batch.addChannel(channel)
                }
                is ChannelHiddenEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        val updatedChannel = it.copy(
                            hidden = true,
                            hiddenMessagesBefore = event.createdAt.takeIf { event.clearHistory },
                        )
                        batch.addChannel(updatedChannel)
                    }
                }
                is ChannelVisibleEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.copy(hidden = false))
                    }
                }
                is NotificationMutesUpdatedEvent -> {
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }

                is ReactionNewEvent -> {
                    batch.addMessage(event.message.enrichWithOwnReactions(batch, currentUserId, event.user))
                }
                is ReactionDeletedEvent -> {
                    batch.addMessage(event.message.enrichWithOwnReactions(batch, currentUserId, event.user))
                }
                is ReactionUpdateEvent -> {
                    batch.addMessage(event.message.enrichWithOwnReactions(batch, currentUserId, event.user))
                }
                is ChannelUserBannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = true, event.shadow)
                                .updateMembershipBanned(event.user.id, banned = true),
                        )
                    }
                }
                is ChannelUserUnbannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = false, false)
                                .updateMembershipBanned(event.user.id, banned = false),
                        )
                    }
                }
                is MemberAddedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.addMember(event.member),
                        )
                    }
                }
                is MemberUpdatedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMember(event.member)
                                .updateMembership(event.member),
                        )
                    }
                }
                is MemberRemovedEvent -> {
                    if (event.user.id == currentUserId) {
                        logger.i { "[updateOfflineStorage] skip MemberRemovedEvent for currentUser" }
                        continue
                    }
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.removeMember(event.user.id)
                                .removeMembership(currentUserId),
                        )
                    }
                }
                is NotificationRemovedFromChannelEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.removeMembership(currentUserId).copy(
                                memberCount = event.channel.memberCount,
                                members = event.channel.members,
                                watcherCount = event.channel.watcherCount,
                                watchers = event.channel.watchers,
                            ),
                        )
                    }
                }
                is ChannelUpdatedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is ChannelUpdatedByUserEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is ChannelDeletedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is ChannelTruncatedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is NotificationChannelDeletedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is NotificationChannelMutesUpdatedEvent -> {
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }
                is NotificationChannelTruncatedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }

                // get the channel, update reads, write the channel
                is MessageReadEvent ->
                    batch.getCurrentChannel(event.cid)
                        ?.updateReads(event.toChannelUserRead())
                        ?.let(batch::addChannel)

                is NotificationMarkReadEvent -> {
                    batch.getCurrentChannel(event.cid)
                        ?.updateReads(event.toChannelUserRead())
                        ?.let(batch::addChannel)
                }
                is NotificationMarkUnreadEvent -> {
                    batch.getCurrentChannel(event.cid)
                        ?.updateReads(event.toChannelUserRead())
                        ?.let(batch::addChannel)
                }
                is GlobalUserBannedEvent -> {
                    batch.addUser(event.user.copy(banned = true))
                }
                is GlobalUserUnbannedEvent -> {
                    batch.addUser(event.user.copy(banned = false))
                }
                is UserUpdatedEvent -> if (event.user.id == currentUserId) {
                    repos.insertCurrentUser(event.user)
                }
                is PollClosedEvent -> batch.addPoll(event.poll)
                is PollDeletedEvent -> batch.addPoll(event.poll)
                is PollUpdatedEvent -> batch.addPoll(event.poll)
                is VoteCastedEvent -> {
                    val ownVotes =
                        (
                            batch.getPoll(event.poll.id)?.ownVotes?.associateBy { it.id }
                                ?: emptyMap()
                            ) +
                            listOfNotNull(event.newVote.takeIf { it.user?.id == currentUserId }).associateBy { it.id }
                    batch.addPoll(
                        event.poll.copy(
                            ownVotes = ownVotes.values.toList(),
                        ),
                    )
                }
                is VoteChangedEvent -> {
                    val ownVotes = event.newVote.takeIf { it.user?.id == currentUserId }?.let { listOf(it) }
                        ?: batch.getPoll(event.poll.id)?.ownVotes
                    batch.addPoll(
                        event.poll.copy(
                            ownVotes = ownVotes ?: emptyList(),
                        ),
                    )
                }
                is VoteRemovedEvent -> {
                    val ownVotes =
                        (
                            batch.getPoll(event.poll.id)?.ownVotes?.associateBy { it.id }
                                ?: emptyMap()
                            ) - event.removedVote.id
                    batch.addPoll(
                        event.poll.copy(
                            ownVotes = ownVotes.values.toList(),
                        ),
                    )
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
                is ChannelHiddenEvent -> {
                    repos.evictChannel(event.cid)
                    if (event.clearHistory) {
                        repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                    }
                }
                is MessageDeletedEvent -> {
                    if (event.hardDelete) {
                        repos.deleteChannelMessage(event.message)
                    } else {
                        repos.markMessageAsDeleted(event.message)
                    }
                }
                is MessageUpdatedEvent -> {
                    repos.updateChannelMessage(event.message)
                }
                is MemberRemovedEvent -> {
                    repos.evictChannel(event.cid)
                }
                is NotificationRemovedFromChannelEvent -> {
                    repos.evictChannel(event.cid)
                }
                else -> Unit // Ignore other events
            }
        }

        logger.v { "[updateOfflineStorage] completed batchId: ${batchEvent.id}" }
    }

    private fun List<ChatEvent>.extractMessageIds() = mapNotNull { event ->
        (event as? HasMessage)?.message?.id
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

    private fun Message.enrichWithOwnReactions(
        batch: EventBatchUpdate,
        currentUserId: UserId,
        eventUser: User?,
    ): Message = copy(
        ownReactions = if (eventUser != null && currentUserId != eventUser.id) {
            batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
        } else {
            mergeReactions(
                latestReactions.filter { it.userId == currentUserId },
                batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf(),
            ).toMutableList()
        },
    )

    private infix fun UserId.mustBe(currentUserId: UserId?) {
        if (this != currentUserId) {
            throw InputMismatchException(
                "received connect event for user with id $this while for user configured " +
                    "has id $currentUserId. Looks like there's a problem in the user set",
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
