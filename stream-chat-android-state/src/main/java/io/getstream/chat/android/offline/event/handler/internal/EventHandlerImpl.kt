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

package io.getstream.chat.android.offline.event.handler.internal

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
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.EventHandlingResult
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HasMessage
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
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UserId
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUser
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserFull
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserPart
import io.getstream.chat.android.offline.event.handler.internal.utils.updateCurrentUser
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.InputMismatchException
import kotlin.math.absoluteValue
import kotlin.random.Random

private const val TAG = "Chat:EventHandlerOld"

@Suppress("LongParameterList")
internal class EventHandlerImpl(
    private val currentUserId: UserId,
    private val scope: CoroutineScope,
    private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable,
    private val logic: LogicRegistry,
    private val state: StateRegistry,
    private val mutableGlobalState: MutableGlobalState,
    private val repos: RepositoryFacade,
    private val syncedEvents: Flow<List<ChatEvent>>,
) : EventHandler {

    private val logger = StreamLog.getLogger(TAG)

    private var eventSubscription: Disposable = EMPTY_DISPOSABLE

    init {
        logger.d { "<init> no args" }
    }

    /**
     * Start listening to chat events.
     */
    override fun startListening() {
        val isDisposed = eventSubscription.isDisposed
        logger.i { "[startListening] isDisposed: $isDisposed, user: $currentUserId" }
        if (isDisposed) {
            val initJob = scope.launch {
                repos.cacheChannelConfigs()
            }
            scope.launch {
                syncedEvents.collect {
                    logger.i { "[onSyncEventsReceived] events.size: ${it.size}" }
                    handleEventsInternal(it, isFromSync = true)
                    logger.i { "[onSyncEventsReceived] processed" }
                }
            }
            eventSubscription = subscribeForEvents {
                scope.launch {
                    initJob.join()
                    handleEvents(listOf(it))
                }
            }
        }
    }

    /**
     * Stop listening for events.
     */
    override fun stopListening() {
        logger.i { "[stopListening] no args" }
        eventSubscription.dispose()
    }

    /**
     * For testing purpose only. Simulates socket event handling.
     */
    @VisibleForTesting
    override suspend fun handleEvents(vararg events: ChatEvent) {
        logger.i { "[handleEvent] events: $events" }
        val eventList = events.toList()
        handleConnectEvents(eventList)
        handleEventsInternal(eventList, isFromSync = false)
    }

    private suspend fun handleEvents(events: List<ChatEvent>) {
        logger.i { "[handleEvents] events.size: ${events.size}" }
        handleConnectEvents(events)
        handleEventsInternal(events, isFromSync = false)
    }

    private suspend fun handleConnectEvents(sortedEvents: List<ChatEvent>) {
        // send out the connect events
        sortedEvents.forEach { event ->
            logger.i { "[handleConnectEvents] event: $event" }
            // connection events are never send on the recovery endpoint, so handle them 1 by 1
            when (event) {
                is DisconnectedEvent -> {
                    logger.i { "[handleConnectEvents] received DisconnectedEvent" }
                }
                is ConnectedEvent -> {
                    logger.i { "[handleConnectEvents] received ConnectedEvent" }
                    updateCurrentUser(SelfUserFull(event.me))
                }
                is HealthEvent -> {
                    logger.v { "[handleConnectEvents] received HealthEvent" }
                }

                is ConnectingEvent -> {
                    logger.i { "[handleConnectEvents] received ConnectingEvent" }
                }

                else -> Unit // Ignore other events
            }
        }
    }

    private suspend fun updateOfflineStorageFromEvents(events: List<ChatEvent>, isFromSync: Boolean) {
        val batchId = Random.nextInt().absoluteValue
        val batchBuilder = EventBatchUpdate.Builder(batchId)
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
                else -> Unit
            }
        }
        // actually fetch the data
        val batch = batchBuilder.build(mutableGlobalState, repos, currentUserId)

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
                    repos.selectChannelWithoutMessages(event.cid)?.copy(
                        hidden = false,
                        messages = listOf(event.message)
                    )?.let(batch::addChannel)
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
                    updateCurrentUser(SelfUserFull(event.me))
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
                is ChannelUserBannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = true, event.shadow)
                                .updateMembershipBanned(event.user.id, banned = true)
                        )
                    }
                }
                is ChannelUserUnbannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = false, false)
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
                    updateCurrentUser(SelfUserFull(event.me))
                }
                is NotificationChannelTruncatedEvent -> {
                    batch.addChannel(event.channel)
                }

                // we use syncState to store the last markAllRead date for a given
                // user since it makes more sense to write to the database once instead of N times.
                is MarkAllReadEvent -> {
                    mutableGlobalState.setTotalUnreadCount(event.totalUnreadCount)
                    mutableGlobalState.setChannelUnreadCount(event.unreadChannels)
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
                        .takeIf { it.id == currentUserId }
                        ?.let {
                            updateCurrentUser(SelfUserPart(it))
                        }
                }
                is TypingStartEvent,
                is TypingStopEvent,
                is HealthEvent,
                is ConnectingEvent,
                is DisconnectedEvent,
                is ErrorEvent,
                is UnknownEvent,
                is UserDeletedEvent,
                is UserStartWatchingEvent,
                is UserStopWatchingEvent,
                is UserPresenceChangedEvent,
                is ConnectedEvent,
                -> Unit
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

    private suspend fun handleEventsInternal(events: List<ChatEvent>, isFromSync: Boolean) {
        events.forEach { chatEvent ->
            logger.v { "[handleEventsInternal] chatEvent: $chatEvent" }
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

        // handle events for active threads
        sortedEvents.filterIsInstance<HasMessage>()
            .groupBy { it.message.parentId ?: it.message.id }
            .filterKeys(logic::isActiveThread)
            .forEach { (messageId, events) ->
                logic.thread(messageId).handleEvents(events)
            }

        // only afterwards forward to the queryRepo since it borrows some data from the channel
        // queryRepo mainly monitors for the notification added to channel event
        logic.getActiveQueryChannelsLogic().forEach { queryChannelsLogic ->
            handleChatEvents(events, queryChannelsLogic)
        }
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
            mutableGlobalState.setTotalUnreadCount(totalUnreadCount)
            mutableGlobalState.setChannelUnreadCount(channelUnreadCount)
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
                logger.d {
                    "Skipping unread counts update for channel: $cid. ${ChannelCapabilities.READ_EVENTS} capability is missing."
                }
                false
            }
        }
    }

    private fun Message.enrichWithOwnReactions(batch: EventBatchUpdate, user: User?) {
        ownReactions = if (user != null && currentUserId != user.id) {
            batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
        } else {
            mergeReactions(
                latestReactions.filter { it.userId == currentUserId },
                batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
            ).toMutableList()
        }
    }

    private suspend fun updateCurrentUser(self: SelfUser) {
        val me = self.me
        if (me.id != currentUserId) {
            throw InputMismatchException(
                "received connect event for user with id ${me.id} while for user configured " +
                    "has id $currentUserId. Looks like there's a problem in the user set"
            )
        }
        mutableGlobalState.updateCurrentUser(self)
        repos.insertCurrentUser(me)
    }

    companion object {
        val EMPTY_DISPOSABLE = object : Disposable {
            override val isDisposed: Boolean = true
            override fun dispose() {}
        }
    }
}
