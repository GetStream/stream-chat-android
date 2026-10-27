/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.sync.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.errors.isStatusBadRequest
import io.getstream.chat.android.client.errors.isValidationError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.removeMyReaction
import io.getstream.chat.android.client.extensions.isAnonymousChannelId
import io.getstream.chat.android.client.internal.state.plugin.listener.internal.ReconnectGroupQueries
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.client.sync.stringify
import io.getstream.chat.android.client.utils.channel.generateChannelIdIfNeeded
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.client.utils.internal.ServerClockOffset
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.coroutines.Tube
import io.getstream.chat.android.core.utils.date.diff
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.TimeDuration
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private const val QUERIES_TO_RETRY = 3
private const val SYNC_MAX_CIDS = 100

/**
 * Maximum number of events replayed from a single `/sync` response. Above this count, event replay
 * is skipped and the actively watched channels are refreshed with `queryChannels` instead.
 *
 * Deliberately conservative and well below the backend cap: a payload can be accepted by the
 * backend and still be too expensive to replay on the device.
 */
private const val SYNC_EVENT_REPLAY_MAX_COUNT = 250

/**
 * Page size of the `queryChannels` fallback. The API caps `queryChannels` at 30 channels per
 * request, so watched cids are refreshed in chunks of this size rather than in [SYNC_MAX_CIDS]
 * batches like `/sync` is.
 */
private const val QUERY_CHANNELS_MAX_LIMIT = 30

/**
 * Age at which the last-synced date is abandoned instead of synced from.
 *
 * `/sync` cannot serve a range this old anyway, and without a ceiling a checkpoint that stops
 * advancing - because every fallback refresh keeps failing - would be retried on every reconnect
 * forever. Past this age the checkpoint is moved to now and the range is given up on.
 */
private const val SYNC_MAX_AGE_DAYS = 30

/**
 * This class is responsible to sync messages, reactions and channel data. It tries to sync then, if necessary,
 * when connection is reestablished or when a health check event happens.
 */
@Suppress("LongParameterList", "TooManyFunctions", "TooGenericExceptionCaught", "LargeClass")
internal class SyncManager(
    private val currentUserId: String,
    private val chatClient: ChatClient,
    private val clientState: ClientState,
    private val mutableGlobalState: MutableGlobalState,
    private val repos: RepositoryFacade,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val userPresence: Boolean,
    private val isAutomaticSyncOnReconnectEnabled: Boolean,
    private val syncMaxThreshold: TimeDuration,
    /** Overridable for tests only; production always uses [SYNC_EVENT_REPLAY_MAX_COUNT]. */
    private val eventReplayMaxCount: Int = SYNC_EVENT_REPLAY_MAX_COUNT,
    private val now: () -> Long,
    private val serverClockOffset: ServerClockOffset,
    scope: CoroutineScope,
    private val events: Tube<List<ChatEvent>> = Tube(),
    private val syncState: MutableStateFlow<SyncState?> = MutableStateFlow(null),
) : SyncHistoryManager {

    private val logger by taggedLogger("Chat:SyncManager")

    private val syncScope = scope + SupervisorJob(scope.coroutineContext.job) +
        CoroutineExceptionHandler { context, throwable ->
            logger.e(throwable) { "[uncaughtCoroutineException] throwable: $throwable, context: $context" }
        }

    // Outside syncScope, whose children a DisconnectedEvent cancels: the socket emits one while it connects.
    private val rescueScope = scope

    private val entitiesRetryMutex = Mutex()
    private val isFirstConnect = AtomicBoolean(true)

    private var eventsDisposable: Disposable? = null

    private val state = MutableStateFlow(State.Idle)

    override val syncedEvents: Flow<List<ChatEvent>> = events

    private val mutex = Mutex()

    private val liveLocationJobs = mutableListOf<Job>()

    override fun start() {
        logger.d { "[start] no args" }
        val isDisposed = eventsDisposable?.isDisposed ?: true
        if (!isDisposed) return
        eventsDisposable = chatClient.subscribe(::onEvent)
        rescueScope.launch { rescueEntitiesStuckInProgress() }
    }

    override fun stop() {
        logger.d { "[stop] no args" }
        eventsDisposable?.dispose()
        syncScope.coroutineContext.job.cancelChildren()
    }

    override suspend fun sync() {
        logger.d { "[sync] no args" }
        state.value = State.Syncing
        performSync(watch = false, groupedListsFirst = false)
        state.value = State.Idle
    }

    override suspend fun awaitSyncing() {
        if (state.value == State.Idle) {
            return
        }
        logger.i { "[awaitSyncing] no args" }
        state.first { it == State.Idle }
        logger.v { "[awaitSyncing] completed" }
    }

    private fun scheduleLiveLocationMessageUpdates() {
        logger.d { "[scheduleLiveLocationMessageUpdates] no args" }

        // Listen for changes in active live locations
        mutableGlobalState.activeLiveLocations.onEach { locations ->
            logger.d { "[scheduleLiveLocationMessageUpdates] locations: ${locations.size}" }
            liveLocationJobs.forEach(Job::cancel)
            locations.forEach { location ->
                val delayMillis = (location.endAt?.time ?: now()) - now()
                logger.d {
                    "[scheduleLiveLocationMessageUpdates] " +
                        "cid: ${location.cid}, " +
                        "messageId: ${location.messageId}, " +
                        "delayMillis: $delayMillis"
                }
                liveLocationJobs += syncScope.launch {
                    delay(delayMillis)
                    logger.v {
                        "[scheduleLiveLocationMessageUpdates] live location sharing ended at ${location.endAt}, " +
                            "messageId: ${location.messageId}"
                    }
                    // Ensure the message has any attribute updated, so the UI can reflect the live location update
                    chatClient.partialUpdateMessage(
                        messageId = location.messageId,
                        set = mapOf("live_location_sharing_ended" to true),
                    ).await()
                        .onError { error ->
                            logger.e {
                                "[scheduleLiveLocationMessageUpdates] failed to update message: " +
                                    "${location.messageId}, error: $error"
                            }
                        }
                }
            }
        }.launchIn(syncScope)
    }

    @VisibleForTesting
    internal fun onEvent(event: ChatEvent) {
        when (event) {
            is ConnectingEvent -> syncScope.launch {
                logger.i { "[onEvent] ConnectingEvent received" }
            }

            is ConnectedEvent -> syncScope.launch {
                logger.i { "[onEvent] ConnectedEvent received" }
                onConnectionEstablished(currentUserId)
                syncScope.launch { syncOfflineDraftMessages() }
                scheduleLiveLocationMessageUpdates()
            }

            is DisconnectedEvent -> syncScope.launch {
                logger.i { "[onEvent] DisconnectedEvent received" }
                onConnectionLost()
                syncScope.coroutineContext.job.cancelChildren()
            }

            is HealthEvent -> syncScope.launch {
                logger.v { "[onEvent] HealthEvent received" }
                retryFailedEntities()
            }

            is MarkAllReadEvent -> syncScope.launch {
                logger.i { "[onEvent] MarkAllReadEvent received" }
                updateAllReadStateForDate(event.user.id, event.createdAt)
            }

            else -> Unit
        }
    }

    private suspend fun syncOfflineDraftMessages() {
        repos.selectDraftMessages().forEach { draftMessage ->
            mutableGlobalState.updateDraftMessage(draftMessage)
        }
    }

    private fun setSyncState(state: SyncState?) {
        logger.v { "[setSyncState] state: ${state?.stringify()}" }
        this.syncState.value = state
    }

    /**
     * Handles connection recover in the SDK.
     * This method will sync the data, retry failed entities, update channels data, etc.
     */
    private suspend fun onConnectionEstablished(userId: String) = try {
        logger.i { "[onConnectionEstablished] >>> userId: $userId, isFirstConnect: $isFirstConnect" }
        state.value = State.Syncing
        val online = clientState.isOnline
        logger.v { "[onConnectionEstablished] online: $online" }
        retryFailedEntities()

        if (syncState.value == null && syncState.value?.userId != userId) {
            logger.v { "[onConnectionEstablished] syncState: ${syncState.value?.stringify()}" }
            updateAllReadStateForDate(userId, currentDate = Date(now()))
        }
        if (isAutomaticSyncOnReconnectEnabled) {
            logger.i { "[onConnectionEstablished] performing sync and restoring active channels" }
            // The first connect does not refresh the grouped lists, so it has no kept pages to watch again.
            val outcome = performSync(
                watch = !isFirstConnect.get() && groupedListsWatch(),
                groupedListsFirst = true,
            )
            restoreActiveChannels(syncedCids = outcome.syncedCids, alreadyRefreshedCids = outcome.refreshedCids)
        } else {
            logger.i { "[onConnectionEstablished] skipping sync, isAutomaticSyncOnReconnectEnabled=false" }
        }
        state.value = State.Idle

        logger.i { "[onConnectionEstablished] <<< completed" }
    } catch (e: Throwable) {
        logger.e { "[onConnectionEstablished] <<< failed: $e" }
    }

    /**
     * Stores the state to be request in a later moment.
     * Should be used when SDK is disconnecting.
     */
    private suspend fun onConnectionLost() = try {
        logger.i { "[connectionLost] firstConnect: $isFirstConnect" }
        state.value = State.Idle
        syncState.value?.let { syncState ->
            val activeCids = logicRegistry.getActiveChannelsLogic().map { it.cid }
            logger.d { "[connectionLost] activeCids.size: ${activeCids.size}" }
            val newSyncState = syncState.copy(activeChannelIds = activeCids)
            repos.insertSyncState(newSyncState)
            setSyncState(newSyncState)
            logger.v { "[connectionLost] syncState saved" }
        }
    } catch (e: Throwable) {
        logger.i { "[connectionLost] failed: $e" }
    }

    /**
     * @param groupedListsFirst Whether the channels of grouped lists go first, so the cap on the request covers them. A
     * grouped list keeps its loaded pages after a reconnect only when the request covered them.
     */
    private suspend fun performSync(watch: Boolean, groupedListsFirst: Boolean): SyncOutcome {
        logger.i { "[performSync] watch: $watch, groupedListsFirst: $groupedListsFirst" }
        val groupedCids = if (groupedListsFirst) {
            logicRegistry.getActiveQueryChannelsLogic().filter { it.groupKey() != null }.flatMap { it.cids() }
        } else {
            emptyList()
        }
        val cids = (groupedCids + logicRegistry.getActiveChannelsLogic().map { it.cid }).distinct().ifEmpty {
            logger.w { "[performSync] no active cids found" }
            repos.selectSyncState(currentUserId)?.activeChannelIds ?: emptyList()
        }
        return mutex.withLock {
            performSync(cids, watch)
        }
    }

    /**
     * Whether a grouped list watches its channels. The channels on its later pages are only watched again by `/sync`,
     * since the reconnect refresh asks for the first page.
     */
    private fun groupedListsWatch(): Boolean = logicRegistry.getActiveQueryChannelsLogic()
        .any { it.groupKey() != null && it.groupedQueryConfig()?.watch ?: true }

    /**
     * Requests the events of [cids] since the last sync and emits them. When the events cannot be replayed, the watched
     * channels are refreshed with `queryChannels` instead.
     */
    @VisibleForTesting
    internal suspend fun performSync(cids: List<String>, watch: Boolean = false): SyncOutcome {
        logger.d { "[performSync] cids.size: ${cids.size}, watch: $watch" }
        if (cids.isEmpty()) {
            logger.w { "[performSync] rejected (cids is empty)" }
            return SyncOutcome()
        }
        val syncState = syncState.value ?: repos.selectSyncState(currentUserId)
        val lastSyncAt = syncState?.lastSyncedAt ?: Date(now())
        val rawLastSyncAt = syncState?.rawLastSyncedAt
        logger.v { "[performSync] lastSyncAt: $lastSyncAt, rawLastSyncAt: $rawLastSyncAt" }
        if (lastSyncAt.exceedsMaxSyncAge()) {
            logger.i { "[performSync] skipping sync; lastSyncAt ($lastSyncAt) is older than $SYNC_MAX_AGE_DAYS days" }
            updateLastSyncedDate(latestEventDate = Date(now()), rawLatestEventDate = null)
            return SyncOutcome()
        }
        val cappedCids = cids.take(SYNC_MAX_CIDS)
        val syncedCids = if (syncState?.lastSyncedAt == null) emptySet() else cappedCids.toSet()
        val result = if (rawLastSyncAt != null) {
            chatClient.getSyncHistoryInternal(cappedCids, rawLastSyncAt, watch).await()
        } else {
            chatClient.getSyncHistoryInternal(cappedCids, lastSyncAt, watch).await()
        }
        if (result.isTooManyEventsToSyncError()) {
            // Event replay is impossible here, so refresh the watched surfaces instead. The payload
            // is unavailable, hence the timestamp moves to now - but only once the refresh
            // succeeded, otherwise the skipped events would become unrecoverable.
            logger.e { "[performSync] failed (too many events to sync): $result" }
            return SyncOutcome(refreshedCids = skipRefusedEventRange(cappedCids))
        }
        if (result !is Result.Success) {
            logger.e { "[performSync] failed($result)" }
            return SyncOutcome()
        }
        val sortedEvents = result.value.sortedBy { it.createdAt }
        logger.v { "[performSync] succeed; events.size: ${sortedEvents.size}" }

        if (sortedEvents.size > eventReplayMaxCount) {
            return SyncOutcome(refreshedCids = skipEventReplay(sortedEvents, cappedCids))
        }

        val latestEvent = sortedEvents.lastOrNull()
        val latestEventDate = latestEvent?.createdAt ?: Date(now())
        val rawLatestEventDate = latestEvent?.rawCreatedAt
        updateLastSyncedDate(latestEventDate, rawLatestEventDate)
        sortedEvents.forEach {
            if (it is MarkAllReadEvent) {
                logger.v { "[performSync] found MarkAllReadEvent (in ${sortedEvents.size} events)" }
                updateAllReadStateForDate(it.user.id, it.createdAt)
            }
        }
        if (sortedEvents.isEmpty()) {
            logger.w { "[performSync] rejected (no events to emit)" }
            return SyncOutcome(syncedCids = syncedCids)
        }
        if (rawLastSyncAt == rawLatestEventDate) {
            logger.w { "[performSync] rejected (rawLatestEventDate equals to rawLastSyncAt)" }
            return SyncOutcome(syncedCids = syncedCids)
        }
        events.emit(sortedEvents)
        logger.v { "[performSync] events emission completed" }
        return SyncOutcome(syncedCids = syncedCids)
    }

    /**
     * Handles a `/sync` range the backend refused for holding too many events.
     *
     * Event replay is impossible here - the payload never arrived - so the watched channels are
     * refreshed instead and the checkpoint is abandoned to now. As in [skipEventReplay], the
     * checkpoint only moves once that refresh succeeded; [SYNC_MAX_AGE_DAYS] is what stops a
     * permanently failing refresh from retrying this range forever.
     *
     * @return The channel ids that were refreshed, empty when the refresh failed.
     */
    private suspend fun skipRefusedEventRange(syncedCids: List<String>): Set<String> {
        val refresh = refreshActiveWatchedChannels(syncedCids)
        if (!refresh.isSuccess) {
            logger.e { "[skipRefusedEventRange] fallback refresh failed, keeping lastSyncedAt: ${refresh.error}" }
            return refresh.refreshedCids
        }
        updateLastSyncedDate(latestEventDate = Date(now()), rawLatestEventDate = null)
        return refresh.refreshedCids
    }

    /**
     * Skips the replay of an oversized `/sync` payload.
     *
     * Replaying this many events would hold the state and persistence pipeline long enough to slow
     * down the requests that also need it, so the actively watched channels are refreshed with
     * `queryChannels` instead.
     *
     * The last-synced timestamp only moves to the newest skipped event once that refresh succeeded:
     * it is what makes discarding the payload safe. When the refresh fails the checkpoint is kept,
     * so the next reconnect asks for the same range again and can retry - at the cost of re-fetching
     * a payload we discard, which is a request and a parse rather than a stalled pipeline.
     *
     * @return The channel ids that were refreshed, empty when the refresh failed.
     */
    private suspend fun skipEventReplay(sortedEvents: List<ChatEvent>, syncedCids: List<String>): Set<String> {
        logger.i {
            "[performSync] skipping event replay; events.size: ${sortedEvents.size} exceeds $eventReplayMaxCount"
        }
        val refresh = refreshActiveWatchedChannels(syncedCids)
        if (!refresh.isSuccess) {
            logger.e { "[skipEventReplay] fallback refresh failed, keeping lastSyncedAt: ${refresh.error}" }
            return refresh.refreshedCids
        }
        // Read state is not carried by the refresh, so keep honouring these the way the replay path
        // does rather than losing them with the rest of the payload.
        sortedEvents.forEach {
            if (it is MarkAllReadEvent) {
                logger.v { "[skipEventReplay] found MarkAllReadEvent (in ${sortedEvents.size} events)" }
                updateAllReadStateForDate(it.user.id, it.createdAt)
            }
        }
        val skippedLatestEvent = sortedEvents.last()
        updateLastSyncedDate(skippedLatestEvent.createdAt, skippedLatestEvent.rawCreatedAt)
        return refresh.refreshedCids
    }

    /**
     * Store the date of the latest events sync.
     * The date should be updated whenever the sync endpoint returns a successful response.
     *
     * @param latestEventDate The date of the last event returned by the sync endpoint.
     */
    private suspend fun updateLastSyncedDate(latestEventDate: Date, rawLatestEventDate: String?) {
        logger.d { "[updateLastSyncedDate] latestEventDate: $latestEventDate, rawLatestEventDate: $rawLatestEventDate" }
        syncState.value?.let { syncState ->
            val newSyncState = syncState.copy(lastSyncedAt = latestEventDate, rawLastSyncedAt = rawLatestEventDate)
            repos.insertSyncState(newSyncState)
            setSyncState(newSyncState)
            logger.v { "[updateLastSyncedDate] saved" }
        }
    }

    /**
     * Updates all the read state for the SDK. If the currentDate of this update is older then the most recent one,
     * the update is ignored.
     *
     * @param userId The id of the current user
     * @param currentDate the moment of the update.
     */
    private suspend fun updateAllReadStateForDate(userId: String, currentDate: Date) {
        if (currentUserId != userId) {
            logger.w { "[updateAllReadStateForDate] rejected (userId mismatch): $userId != $currentDate" }
            return
        }
        logger.d { "[updateAllReadStateForDate] userId: $userId, currentDate: $currentDate" }
        val syncState = repos.selectSyncState(userId)?.let { selectedState ->
            logger.v {
                "[updateAllReadStateForDate] selectedState.activeCids.zie: ${selectedState.activeChannelIds.size}"
            }
            when (selectedState.markedAllReadAt?.before(currentDate)) {
                true -> selectedState.copy(markedAllReadAt = currentDate).also { newState ->
                    repos.insertSyncState(newState)
                }

                else -> selectedState
            }
        } ?: SyncState(userId)
        setSyncState(syncState)
    }

    /**
     * Retry all entities that have failed. Channels, messages, reactions, etc.
     */
    private suspend fun retryFailedEntities() = try {
        entitiesRetryMutex.withLock {
            logger.d { "[retryFailedEntities] no args" }
            // retry channels, messages and reactions in that order..
            val uncreatedCids = retryChannels()
            retryMessages(uncreatedCids)
            retryReactions()
            logger.v { "[retryFailedEntities] completed" }
        }
    } catch (e: Throwable) {
        logger.e(e) { "[retryFailedEntities] failed: $e" }
    }

    /**
     * Hands channels, messages and reactions left [SyncStatus.IN_PROGRESS] by an earlier session back
     * to the retry flow. Their request died with that session, so no result will ever settle them.
     */
    private suspend fun rescueEntitiesStuckInProgress() = entitiesRetryMutex.withLock {
        // Each type on its own, so a failure in one does not leave the others stuck.
        rescueStuckInProgress("channels") {
            val cids = repos.selectChannelCidsBySyncStatus(SyncStatus.IN_PROGRESS)
            logger.d { "[rescueEntitiesStuckInProgress] cids.size: ${cids.size}" }
            cids.forEach { cid ->
                val channel = repos.selectChannel(cid) ?: return@forEach
                repos.insertChannel(channel.copy(syncStatus = SyncStatus.SYNC_NEEDED))
            }
        }
        rescueStuckInProgress("messages") {
            val messageIds = repos.selectMessageIdsBySyncState(SyncStatus.IN_PROGRESS)
            logger.d { "[rescueEntitiesStuckInProgress] messageIds.size: ${messageIds.size}" }
            messageIds.forEach { id ->
                val message = repos.selectMessage(id) ?: return@forEach
                repos.insertMessage(message.copy(syncStatus = SyncStatus.SYNC_NEEDED))
            }
        }
        rescueStuckInProgress("reactions") {
            val reactionIds = repos.selectReactionIdsBySyncStatus(SyncStatus.IN_PROGRESS)
            logger.d { "[rescueEntitiesStuckInProgress] reactionIds.size: ${reactionIds.size}" }
            reactionIds.forEach { id ->
                val reaction = repos.selectReactionById(id) ?: return@forEach
                repos.insertReaction(reaction.copy(syncStatus = SyncStatus.SYNC_NEEDED))
            }
        }
    }

    private suspend fun rescueStuckInProgress(entities: String, rescue: suspend () -> Unit) = try {
        rescue()
    } catch (e: Throwable) {
        logger.e(e) { "[rescueEntitiesStuckInProgress] failed for $entities: $e" }
    }

    /**
     * @param syncedCids The cids whose events the reconnect sync received. A grouped list keeps its loaded pages only
     * when they are all here, since no other request updates the channels beyond its first page.
     * @param alreadyRefreshedCids Channel ids already brought up to date earlier in the recovery
     * flow, e.g. by the `/sync` event replay fallback. They are not queried a second time.
     */
    private suspend fun restoreActiveChannels(syncedCids: Set<String>, alreadyRefreshedCids: Set<String>) {
        val recoverAll = !isFirstConnect.compareAndSet(true, false)
        logger.d {
            "[restoreActiveChannels] recoverAll: $recoverAll, " +
                "alreadyRefreshedCids.size: ${alreadyRefreshedCids.size}"
        }

        val allLogics = logicRegistry.getActiveQueryChannelsLogic()
        val hasGroupedQueries = allLogics.any { it.groupKey() != null }
        val hasStandardQueries = allLogics.any { it.groupKey() == null }

        // --- GroupedQueryChannels path ---
        val groupedHandledCids: Set<String> = if (hasGroupedQueries) {
            // Refresh first page of the queries populated via GroupedQueryChannels
            val refreshed = updateGroupedQueryChannels(recoverAll, syncedCids)
            // Re-watch tracked channels (specific for this path, where we don't re-watch the groups, just the manually
            // opened/tracked channels)
            val rewatched = rewatchTrackedWatchedChannels(cidsToExclude = alreadyRefreshedCids)
            refreshed + rewatched + alreadyRefreshedCids
        } else {
            emptySet()
        }

        // --- QueryChannels path ---
        if (hasStandardQueries) {
            when (val result = updateActiveQueryChannels(recoverAll)) {
                is Result.Success -> {
                    val updatedCids = result.value
                    logger.v { "[restoreActiveChannels] standardCids.size: ${result.value.size}" }
                    updateActiveChannels(recoverAll, updatedCids + groupedHandledCids + alreadyRefreshedCids)
                }
                is Result.Failure -> {
                    logger.e { "[restoreActiveChannels] standard query failed: ${result.value}" }
                    return
                }
            }
        }

        // --- Active Channels created outside of QueryChannels requests
        if (!hasStandardQueries && !hasGroupedQueries) {
            // Check for active channels created outside of a QueryChannels requests
            updateActiveChannels(recoverAll, cidsToExclude = alreadyRefreshedCids)
        }
    }

    /**
     * Drives the recovery flow for grouped channel queries: when at least one active grouped
     * logic needs recovery, calls [ChatClient.queryGroupedChannelsInternal] once per requested sort,
     * so groups requested with different sorts keep their own order. The
     * [io.getstream.chat.android.client.internal.state.plugin.listener.internal.QueryGroupedChannelsListenerState]
     * routes the response into the corresponding per-group state and persists it.
     *
     * Assumes the grouped queries that share a sort also share the same request-level `limit`,
     * `watch`, and `presence` flags — the first captured config wins.
     */
    private suspend fun updateGroupedQueryChannels(recoverAll: Boolean, syncedCids: Set<String>): Set<String> {
        val activeGroupedLogics = logicRegistry.getActiveQueryChannelsLogic()
            .filter { it.groupKey() != null && (it.recoveryNeeded().value || recoverAll) }

        if (activeGroupedLogics.isEmpty()) {
            logger.v { "[updateGroupedQueryChannels] no grouped queries to restore" }
            return emptySet()
        }

        return activeGroupedLogics
            .groupBy { it.groupedQueryConfig()?.sort }
            .values
            .flatMapTo(mutableSetOf()) { logics -> recoverGroupedQueryChannels(logics, syncedCids) }
    }

    private suspend fun recoverGroupedQueryChannels(
        activeGroupedLogics: List<QueryChannelsLogic>,
        syncedCids: Set<String>,
    ): Set<String> {
        // Shared fields (limit, watch, presence, sort) are identical across groups from the same
        // original session — take the first captured config. Per-group page sizes and filters are merged in
        // the `groups` map below so the server returns the same page sizes the caller chose.
        //
        // Always include every active group's key, even when the captured per-group `pageSize` is
        // null. An empty `{}` per-group entry is meaningful: it tells the server "refresh this
        // group too". Filtering it out would cause re-sync to skip that group entirely.
        // A configuration restored from the cache only has defaults for the shared fields,
        // so prefer one a request set.
        val shared = activeGroupedLogics
            .firstNotNullOfOrNull { logic ->
                logic.groupedQueryConfig()?.takeUnless { logic.isGroupedQueryConfigRestored() }
            }
            ?: activeGroupedLogics.firstNotNullOfOrNull { it.groupedQueryConfig() }
        val groupsParam = activeGroupedLogics
            .mapNotNull { logic ->
                val key = logic.groupKey() ?: return@mapNotNull null
                val cfg = logic.groupedQueryConfig()
                key to GroupedChannelsGroupQuery(limit = cfg?.pageSize, filter = cfg?.filter)
            }
            .toMap()
            .takeIf { it.isNotEmpty() }
            ?.let { groups ->
                // `/sync` does not subscribe to presence, so only pages fetched again would get presence events.
                val keysKeepingPages = if (shared?.presence == true) {
                    emptySet()
                } else {
                    activeGroupedLogics
                        .filter { syncedCids.containsAll(it.cids()) }
                        .mapNotNull { it.groupKey() }
                        .toSet()
                }
                ReconnectGroupQueries(groups, keysKeepingPages)
            }

        val result = chatClient.queryGroupedChannelsInternal(
            limit = shared?.limit,
            groups = groupsParam,
            watch = shared?.watch ?: true,
            presence = shared?.presence ?: false,
            sort = shared?.sort,
        ).await()

        return when (result) {
            is Result.Success -> {
                val cids = result.value.groups.values
                    .flatMap { group -> group.channels }
                    .mapTo(mutableSetOf()) { it.cid }
                logger.v { "[updateGroupedQueryChannels] succeeded; cids.size: ${cids.size}" }
                cids
            }
            is Result.Failure -> {
                logger.e { "[updateGroupedQueryChannels] queryGroupedChannelsInternal failed: ${result.value}" }
                emptySet()
            }
        }
    }

    /**
     * Re-watches channels explicitly opened by the user (tracked via
     * [io.getstream.chat.android.client.internal.state.plugin.state.internal.WatchedChannelStateFlow]
     * weak references in [StateRegistry]).
     */
    private suspend fun rewatchTrackedWatchedChannels(cidsToExclude: Set<String> = emptySet()): Set<String> {
        val online = clientState.isOnline
        val watchedCids = stateRegistry.getTrackedWatchedChannels() - cidsToExclude
        logger.d {
            "[rewatchTrackedWatchedChannels] watchedCids.size: ${watchedCids.size}, online: $online, " +
                "cidsToExclude.size: ${cidsToExclude.size}"
        }
        if (watchedCids.isEmpty() || !online) return emptySet()

        watchedCids.forEach { cid ->
            val (type, id) = cid.cidToTypeAndId()
            logicRegistry.channel(type, id).watch(userPresence = userPresence)
        }
        return watchedCids
    }

    private suspend fun updateActiveQueryChannels(recoverAll: Boolean): Result<Set<String>> {
        // 2. update the results for queries that are actively being shown right now (synchronous)
        logger.d { "[updateActiveQueryChannels] recoverAll: $recoverAll" }
        val queryLogicsToRestore = logicRegistry.getActiveQueryChannelsLogic()
            .asSequence()
            .filter { it.groupKey() == null }
            .filter { queryChannelsLogic -> queryChannelsLogic.recoveryNeeded().value || recoverAll }
            .take(QUERIES_TO_RETRY)
            .toList()
        if (queryLogicsToRestore.isEmpty()) {
            logger.v { "[updateActiveQueryChannels] queryLogicsToRestore.size: ${queryLogicsToRestore.size}" }
            return Result.Success(emptySet())
        }
        logger.v { "[updateActiveQueryChannels] queryLogicsToRestore.size: ${queryLogicsToRestore.size}" }

        val failed = AtomicReference<Error>()
        val updatedCids = mutableSetOf<String>()
        queryLogicsToRestore.forEach { queryLogic ->
            logger.v { "[updateActiveQueryChannels] queryLogic.identifier: ${queryLogic.identifier}" }
            queryLogic.queryFirstPage()
                .onError {
                    logger.e { "[updateActiveQueryChannels] request failed: $it" }
                    failed.set(it)
                }
                .onSuccessSuspend { foundChannels ->
                    logger.v {
                        "[updateActiveQueryChannels] request completed; foundChannels.size: ${foundChannels.size}"
                    }
                    mutableGlobalState.updateChannelDrafts(foundChannels)
                    updatedCids.addAll(foundChannels.map { it.cid })
                    logger.v { "[updateActiveQueryChannels] updatedCids.size: ${updatedCids.size}" }
                }
        }
        return when (val error = failed.get()) {
            null -> Result.Success(updatedCids)
            else -> Result.Failure(error)
        }
    }

    private suspend fun updateActiveChannels(
        recoverAll: Boolean,
        cidsToExclude: Set<String>,
    ) {
        val online = clientState.isOnline
        logger.d {
            "[updateActiveChannels] recoverAll: $recoverAll, online: $online, cidsToExclude.size: ${cidsToExclude.size}"
        }
        val missingChannelIds: List<ChannelId> = stateRegistry.getActiveChannelStates()
            .filter { (id, state) -> (state.recoveryNeeded || recoverAll) && id.cid !in cidsToExclude }
            .keys
            .take(n = 30)

        logger.v { "[updateActiveChannels] missingCids.size: ${missingChannelIds.size}" }
        if (missingChannelIds.isEmpty() || !online) {
            return
        }
        val missingCids = missingChannelIds.map(ChannelId::cid)
        val filter = Filters.`in`("cid", missingCids)
        val request = QueryChannelsRequest(filter, offset = 0, limit = 30)
        logger.v { "[updateActiveChannels] request: $request" }
        chatClient.queryChannelsInternal(request)
            .await()
            .onError {
                logger.e { "[updateActiveChannels] request failed: $it" }
            }
            .onSuccessSuspend { queryResult ->
                val foundChannels = queryResult.channels
                logger.v { "[updateActiveChannels] request completed; foundChannels.size: ${foundChannels.size}" }

                foundChannels.forEach { channel ->
                    ChannelId.fromTypeAndId(channel.type, channel.id)
                        ?.let(logicRegistry::channel)
                        ?.updateDataForChannel(channel, channel.messages.size)
                }
                mutableGlobalState.updateChannelDrafts(foundChannels)
                repos.storeStateForChannels(foundChannels)
                val foundCids = foundChannels.mapTo(mutableSetOf(), Channel::cid)
                val stillMissingChannelIds = missingChannelIds.filterNot { it.cid in foundCids }
                logger.v { "[updateActiveChannels] stillMissingCids.size: ${stillMissingChannelIds.size}" }

                // create channels that are not present on the API
                stillMissingChannelIds.forEach { id ->
                    logicRegistry.channel(id).watch(userPresence = userPresence)
                }
            }
    }

    /**
     * Refreshes the channels the user is actively watching through `queryChannels`, instead of
     * replaying an oversized `/sync` payload event by event.
     *
     * A `queryChannels` response carries the latest channel state, messages, members, watchers and
     * read state, and re-registers the watch for the requested cids, which is what the surfaces the
     * user is looking at need. Channels that are not actively watched are left to a future explicit
     * query.
     *
     * The preferred source is [StateRegistry.getTrackedWatchedChannels] - channels an explicitly
     * opened screen subscribed to. It is deliberately *not* [LogicRegistry.getActiveChannelsLogic],
     * which holds an entry for every channel any query has paged through and would turn this into
     * dozens of `watch` requests for channels nobody is viewing.
     *
     * Nothing has subscribed yet on a cold start, which is also when a payload is most likely to be
     * oversized. Falling back to [syncedCids] keeps the refresh symmetric with the request: the
     * channels the range was requested for are the ones brought up to date by discarding it. They
     * are already capped at [SYNC_MAX_CIDS], so this stays bounded.
     *
     * Having nothing to refresh is a success. A failed request is not, and the caller must not
     * advance the last-synced date on it, or the events it skipped become unrecoverable.
     *
     * @param syncedCids The cids `/sync` was asked about, used when no channel is being watched yet.
     *
     * @return The cids that were refreshed, and the error that stopped the refresh, if any. The
     * refreshed cids are reported even on failure so the caller can still skip re-querying them.
     */
    private suspend fun refreshActiveWatchedChannels(syncedCids: List<String>): WatchedChannelsRefresh {
        val online = clientState.isOnline
        val watchedCids = stateRegistry.getTrackedWatchedChannels().ifEmpty { syncedCids.toSet() }
        logger.d { "[refreshActiveWatchedChannels] watchedCids.size: ${watchedCids.size}, online: $online" }
        if (watchedCids.isEmpty()) {
            return WatchedChannelsRefresh(refreshedCids = emptySet())
        }
        if (!online) {
            logger.w { "[refreshActiveWatchedChannels] rejected (offline)" }
            return WatchedChannelsRefresh(
                refreshedCids = emptySet(),
                error = Error.GenericError("Cannot refresh watched channels while offline"),
            )
        }

        val failed = AtomicReference<Error>()
        val refreshedCids = mutableSetOf<String>()
        for (batch in watchedCids.chunked(QUERY_CHANNELS_MAX_LIMIT)) {
            val request = QueryChannelsRequest(
                filter = Filters.`in`("cid", batch),
                offset = 0,
                limit = batch.size,
            ).apply { presence = userPresence }
            logger.v { "[refreshActiveWatchedChannels] request: $request" }
            chatClient.queryChannelsInternal(request)
                .await()
                .onError {
                    logger.e { "[refreshActiveWatchedChannels] request failed: $it" }
                    failed.set(it)
                }
                .onSuccessSuspend { queryResult ->
                    val foundChannels = queryResult.channels
                    foundChannels.forEach { channel ->
                        ChannelId.fromTypeAndId(channel.type, channel.id)
                            ?.let(logicRegistry::channel)
                            ?.updateDataForChannel(channel, channel.messages.size)
                    }
                    mutableGlobalState.updateChannelDrafts(foundChannels)
                    repos.storeStateForChannels(foundChannels)
                    foundChannels.mapTo(refreshedCids, Channel::cid)
                }
            // Stop at the first failure: the remaining requests would be spent on a refresh the
            // caller is going to reject anyway.
            if (failed.get() != null) {
                break
            }
        }
        // A cid the server did not return is not a failure: it can be deleted or no longer visible
        // to this user, and holding the checkpoint for it would re-fetch the payload on every
        // reconnect forever. Only a failed request blocks the checkpoint.
        val missedCids = watchedCids - refreshedCids
        if (missedCids.isNotEmpty()) {
            logger.w { "[refreshActiveWatchedChannels] not returned by the server; cids.size: ${missedCids.size}" }
        }
        logger.v { "[refreshActiveWatchedChannels] refreshedCids.size: ${refreshedCids.size}" }
        return WatchedChannelsRefresh(refreshedCids = refreshedCids, error = failed.get())
    }

    /**
     * @return The cids of the channels the server has not created yet, whose messages can only be sent once it has.
     */
    private suspend fun retryChannels(): Set<String> {
        val cids = repos.selectChannelCidsBySyncNeeded()
        logger.d { "[retryChannels] cids.size: ${cids.size}" }
        val uncreatedCids = mutableSetOf<String>()
        cids.forEach { cid ->
            logger.v { "[retryChannels] channel.cid: $cid" }
            val channel = repos.selectChannel(cid) ?: return@forEach
            val result = if (channel.createdAt.exceedsSyncThreshold()) {
                logger.w { "[retryChannels] outdated channel($cid)" }
                repos.deleteChannel(cid)
                uncreatedCids += cid
                Result.Success(Unit)
            } else {
                logger.v { "[retryChannels] sending channel($cid)" }
                val params = CreateChannelParams(
                    members = channel.members.map { member ->
                        MemberData(member.getUserId(), extraData = member.extraData)
                    },
                    extraData = channel.extraData,
                )
                // A member-based id may be a local placeholder the server rejects; let the server resolve it
                val channelId = if (channel.id.isAnonymousChannelId()) "" else channel.id
                chatClient.createChannel(channel.type, channelId, params).await().onSuccessSuspend { created ->
                    // The create replaces the placeholder it regenerates from the members, which may not be this one
                    val regeneratedCid = "${channel.type}:${generateChannelIdIfNeeded("", params.memberIds)}"
                    if (created.cid != cid && cid != regeneratedCid) {
                        repos.replacePlaceholderChannel(cid, created.cid)
                    }
                }.onError { error ->
                    // A permanent rejection leaves the messages to fail where the user can retry them
                    if (!error.isPermanent()) uncreatedCids += cid
                }
            }
            logger.v { "[retryChannels] result($cid).isSuccess: ${result is Result.Success}" }
        }
        return uncreatedCids
    }

    /**
     * @param uncreatedCids The cids of the channels the server has not created yet. Their messages wait for a later
     * retry instead of failing against a channel that does not exist, until they are outdated.
     */
    @VisibleForTesting
    internal suspend fun retryMessages(uncreatedCids: Set<String> = emptySet()) {
        logger.d { "[retryMessages] uncreatedCids.size: ${uncreatedCids.size}" }
        retryMessagesWithSyncedAttachments(uncreatedCids)
        retryMessagesWithPendingAttachments(uncreatedCids)
        logger.v { "[retryMessages] completed" }
    }

    private fun Message.waitsForChannel(uncreatedCids: Set<String>): Boolean =
        cid in uncreatedCids && !createdLocallyAt.exceedsSyncThresholdServerTime()

    private suspend fun retryReactions() {
        val ids = repos.selectReactionIdsBySyncStatus(SyncStatus.SYNC_NEEDED)
        logger.d { "[retryReactions] ids.size: ${ids.size}" }
        ids.forEach { id ->
            logger.d { "[retryReactions] process reaction($id)" }
            val reaction = repos.selectReactionById(id) ?: return@forEach
            val result = if (reaction.deletedAt != null) {
                retryReactionDeletion(id, reaction)
            } else {
                retryReactionSending(reaction, id)
            }.await()
            logger.v { "[retryReactions] result(${reaction.id}).isSuccess: ${result is Result.Success}" }
        }
    }

    private suspend fun retryMessagesWithSyncedAttachments(uncreatedCids: Set<String>) {
        val ids = repos.selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)
        logger.d { "[retryMgsWithSyncedAttachments] ids.size: ${ids.size}" }
        ids.forEach { id ->
            logger.v { "[retryMgsWithSyncedAttachments] message.id: $id" }
            val message = repos.selectMessage(id) ?: return@forEach
            if (message.waitsForChannel(uncreatedCids)) return@forEach
            val channelClient = chatClient.channel(message.cid)
            val result = when {
                message.isDeleted() && !message.deletedForMe -> {
                    retryDeletionOfMessageWithSyncedAttachments(id, message, channelClient)
                }

                message.deletedForMe -> retryDeleteMessageForMe(id)
                message.updatedLocallyAt != null && message.createdAt != null -> {
                    retryUpdateOfMessageWithSyncedAttachments(id, message, channelClient)
                }

                else -> retrySendingOfMessageWithSyncedAttachments(message, id, channelClient)
            }
            logger.v { "[retryMgsWithSyncedAttachments] result(${message.id}).isSuccess: ${result is Result.Success}" }
        }
    }

    private suspend fun retryDeleteMessageForMe(messageId: String): Result<Message> {
        logger.d { "[retryDeleteMessageForMe] messageId: $messageId" }
        return chatClient.deleteMessageForMe(messageId).await()
    }

    /**
     * Retries messages with [SyncStatus.AWAITING_ATTACHMENTS] status.
     */
    private suspend fun retryMessagesWithPendingAttachments(uncreatedCids: Set<String>) {
        val ids = repos.selectMessageIdsBySyncState(SyncStatus.AWAITING_ATTACHMENTS)
        logger.d { "[retryMessagesWithPendingAttachments] ids.size: ${ids.size}" }
        ids.forEach { id ->
            logger.d { "[retryMessagesWithPendingAttachments] process message($id)" }
            val message = repos.selectMessage(id) ?: return@forEach
            val isFailed = message.attachments.any { it.uploadState is Attachment.UploadState.Failed }
            if (isFailed) {
                logger.v { "[retryMessagesWithPendingAttachments] marking message($id) as failed" }
                repos.markMessageAsFailed(message)
            } else {
                logger.v { "[retryMessagesWithPendingAttachments] sending message($id)" }
                if (message.createdLocallyAt.exceedsSyncThresholdServerTime()) {
                    logger.w { "[retryMessagesWithPendingAttachments] outdated sending($id)" }
                    removeMessage(message).await()
                } else if (message.waitsForChannel(uncreatedCids)) {
                    logger.v { "[retryMessagesWithPendingAttachments] waiting for channel(${message.cid})" }
                } else {
                    val (channelType, channelId) = message.cid.cidToTypeAndId()
                    chatClient.sendMessage(channelType, channelId, message, true).await()
                }
            }
        }
    }

    private suspend fun RepositoryFacade.markMessageAsFailed(message: Message) =
        insertMessage(message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY, updatedLocallyAt = Date(now())))

    private suspend fun retryReactionDeletion(
        id: Int,
        reaction: Reaction,
    ): Call<out Any> {
        logger.v { "[retryReactionDeletion] reaction($id) for messageId: ${reaction.messageId}" }
        return if (reaction.deletedAt.exceedsSyncThreshold()) {
            logger.w { "[retryReactionDeletion] outdated deletion($id)" }
            removeReaction(reaction)
        } else {
            chatClient.deleteReaction(reaction.messageId, reaction.type)
        }
    }

    private suspend fun retryReactionSending(
        reaction: Reaction,
        id: Int,
    ): Call<out Any> {
        logger.v { "[retryReactionSending] reaction(${reaction.id}) for messageId: ${reaction.messageId}" }
        return if (reaction.createdLocallyAt.exceedsSyncThreshold()) {
            logger.w { "[retryReactionSending] outdated sending($id)" }
            removeReaction(reaction)
        } else {
            chatClient.sendReaction(reaction, reaction.enforceUnique, null, reaction.skipPush)
        }
    }

    private suspend fun retryDeletionOfMessageWithSyncedAttachments(
        id: String,
        message: Message,
        channelClient: ChannelClient,
    ): Result<Any> {
        logger.v { "[retryDeletionOfMessageWithSyncedAttachments] deleting message($id)" }
        return if (message.deletedAt.exceedsSyncThreshold()) {
            logger.w { "[retryDeletionOfMessageWithSyncedAttachments] outdated deleting($id)" }
            removeMessage(message).await()
        } else {
            channelClient.deleteMessage(message.id).await()
        }
    }

    private suspend fun retryUpdateOfMessageWithSyncedAttachments(
        id: String,
        message: Message,
        channelClient: ChannelClient,
    ): Result<Any> {
        logger.v { "[retryUpdateOfMessageWithSyncedAttachments] updating message($id)" }
        return if (message.updatedLocallyAt.exceedsSyncThreshold()) {
            logger.w { "[retryUpdateOfMessageWithSyncedAttachments] outdated updating($id)" }
            removeMessage(message).await()
        } else {
            channelClient.updateMessage(message).await()
        }
    }

    private suspend fun retrySendingOfMessageWithSyncedAttachments(
        message: Message,
        id: String,
        channelClient: ChannelClient,
    ): Result<Any> {
        logger.v { "[retrySendingOfMessageWithSyncedAttachments] sending message(${message.id})" }
        return if (message.createdLocallyAt.exceedsSyncThresholdServerTime()) {
            logger.w { "[retrySendingOfMessageWithSyncedAttachments] outdated sending($id)" }
            removeMessage(message).await()
        } else {
            // Do not persist the local message here. The SendMessageListener plugins already store the
            // outcome: the server's reply, which for a rejected send is a type "error" echo rather than
            // the message that was sent, or the failed status.
            channelClient.sendMessage(message).await()
        }
    }

    private suspend fun removeReaction(reaction: Reaction): Call<Unit> {
        return CoroutineCall(syncScope) {
            try {
                logger.d { "[removeReaction] reaction.id: ${reaction.id}" }
                repos.deleteReaction(reaction)
                logicRegistry.channelFromMessageId(reaction.messageId)
                    ?.getMessage(reaction.messageId)
                    ?.removeMyReaction(reaction)
                logicRegistry.threadFromMessageId(reaction.messageId)
                    ?.getMessage(reaction.messageId)
                    ?.removeMyReaction(reaction)
                logger.v { "[removeReaction] completed: ${reaction.id}" }
                Result.Success(Unit)
            } catch (e: Throwable) {
                logger.e { "[removeReaction] failed(${reaction.id}): $e" }
                Result.Failure(Error.ThrowableError(e.message.orEmpty(), e))
            }
        }
    }

    private suspend fun removeMessage(message: Message): Call<Unit> {
        return CoroutineCall(syncScope) {
            try {
                logger.d { "[removeMessage] message.id: ${message.id}" }
                repos.deleteChannelMessage(message)
                logicRegistry.channelFromMessage(message)?.deleteMessage(message)
                logicRegistry.getActiveQueryThreadsLogic().forEach { it.deleteMessage(message) }
                logicRegistry.threadFromMessage(message)?.deleteMessage(message)
                logger.v { "[removeMessage] completed: ${message.id}" }
                Result.Success(Unit)
            } catch (e: Throwable) {
                logger.e { "[removeMessage] failed(${message.id}): $e" }
                Result.Failure(Error.ThrowableError(e.message.orEmpty(), e))
            }
        }
    }

    /**
     * Checks if the result is a too many events to sync error.
     * This error happens when the user is offline for a long time and has more than 1000 events to sync.
     *
     * @return True if the error is related to too many events to sync, false otherwise.
     */
    private fun Result<List<ChatEvent>>.isTooManyEventsToSyncError(): Boolean {
        val error = errorOrNull()
        return error is Error.NetworkError &&
            error.isStatusBadRequest() &&
            error.isValidationError()
    }

    private fun Date?.exceedsSyncThreshold(): Boolean {
        return this == null || diff(now()) > syncMaxThreshold
    }

    /**
     * Whether the last-synced date is too old for `/sync` to serve. Distinct from
     * [exceedsSyncThreshold], which gates retrying local entities, not the sync range.
     */
    private fun Date.exceedsMaxSyncAge(): Boolean {
        return diff(now()) > TimeDuration.days(SYNC_MAX_AGE_DAYS)
    }

    /**
     * Important: Use only for local dates created with [ServerClockOffset.estimatedServerTime].
     * Use for comparing:
     *  - [Message.createdLocallyAt]
     */
    private fun Date?.exceedsSyncThresholdServerTime(): Boolean {
        return this == null || diff(serverClockOffset.estimatedServerTime()) > syncMaxThreshold
    }

    private enum class State {
        Idle, Syncing
    }

    /**
     * Outcome of [performSync].
     *
     * @param syncedCids The cids whose events since a known last sync date were received and replayed. A grouped list
     * keeps its loaded pages after a reconnect only when they are all here.
     * @param refreshedCids The cids refreshed with `queryChannels` because the events could not be replayed.
     */
    internal data class SyncOutcome(
        val syncedCids: Set<String> = emptySet(),
        val refreshedCids: Set<String> = emptySet(),
    )

    /**
     * Outcome of [refreshActiveWatchedChannels].
     *
     * Carries both halves because they are needed independently: [error] decides whether the
     * last-synced date may advance, while [refreshedCids] is still worth reporting on failure so
     * the recovery flow does not query those channels a second time.
     */
    private data class WatchedChannelsRefresh(
        val refreshedCids: Set<String>,
        val error: Error? = null,
    ) {
        val isSuccess: Boolean get() = error == null
    }
}
