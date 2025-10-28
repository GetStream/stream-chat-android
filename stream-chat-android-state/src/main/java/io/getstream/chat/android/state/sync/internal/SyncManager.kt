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

package io.getstream.chat.android.state.sync.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
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
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.client.sync.stringify
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.coroutines.Tube
import io.getstream.chat.android.core.utils.date.diff
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.TimeDuration
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
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

/**
 * This class is responsible to sync messages, reactions and channel data. It tries to sync then, if necessary,
 * when connection is reestablished or when a health check event happens.
 */
@Suppress("LongParameterList", "TooManyFunctions", "TooGenericExceptionCaught")
internal class SyncManager(
    private val currentUserId: String,
    private val chatClient: ChatClient,
    private val clientState: ClientState,
    private val mutableGlobalState: MutableGlobalState,
    private val repos: RepositoryFacade,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val userPresence: Boolean,
    private val syncMaxThreshold: TimeDuration,
    private val now: () -> Long,
    scope: CoroutineScope,
    private val events: Tube<List<ChatEvent>> = Tube(),
    private val syncState: MutableStateFlow<SyncState?> = MutableStateFlow(null),
) : SyncHistoryManager {

    private val logger by taggedLogger("Chat:SyncManager")

    private val syncScope = scope + SupervisorJob(scope.coroutineContext.job) +
        CoroutineExceptionHandler { context, throwable ->
            logger.e(throwable) { "[uncaughtCoroutineException] throwable: $throwable, context: $context" }
        }

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
    }

    override fun stop() {
        logger.d { "[stop] no args" }
        eventsDisposable?.dispose()
        syncScope.coroutineContext.job.cancelChildren()
    }

    override suspend fun sync() {
        logger.d { "[sync] no args" }
        state.value = State.Syncing
        performSync()
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
        performSync()
        restoreActiveChannels()
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

    private suspend fun performSync() {
        logger.i { "[performSync] no args" }
        val cids = logicRegistry.getActiveChannelsLogic().map { it.cid }.ifEmpty {
            logger.w { "[performSync] no active cids found" }
            repos.selectSyncState(currentUserId)?.activeChannelIds ?: emptyList()
        }
        mutex.withLock {
            performSync(cids)
        }
    }

    @VisibleForTesting
    internal suspend fun performSync(cids: List<String>) {
        logger.d { "[performSync] cids.size: ${cids.size} " }
        if (cids.isEmpty()) {
            logger.w { "[performSync] rejected (cids is empty)" }
            return
        }
        val syncState = syncState.value ?: repos.selectSyncState(currentUserId)
        val lastSyncAt = syncState?.lastSyncedAt ?: Date(now())
        val rawLastSyncAt = syncState?.rawLastSyncedAt
        logger.v { "[performSync] lastSyncAt: $lastSyncAt, rawLastSyncAt: $rawLastSyncAt" }
        val result = if (rawLastSyncAt != null) {
            chatClient.getSyncHistory(cids, rawLastSyncAt).await()
        } else {
            chatClient.getSyncHistory(cids, lastSyncAt).await()
        }
        if (result.isTooManyEventsToSyncError()) {
            logger.e { "[performSync] failed (too many events to sync): $result" }
            updateLastSyncedDate(latestEventDate = Date(now()), rawLatestEventDate = null)
            return
        }
        if (result !is Result.Success) {
            logger.e { "[performSync] failed($result)" }
            return
        }
        val sortedEvents = result.value.sortedBy { it.createdAt }
        logger.v { "[performSync] succeed; events.size: ${sortedEvents.size}" }

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
            return
        }
        if (rawLastSyncAt == rawLatestEventDate) {
            logger.w { "[performSync] rejected (rawLatestEventDate equals to rawLastSyncAt)" }
            return
        }
        events.emit(sortedEvents)
        logger.v { "[performSync] events emission completed" }
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
     * Updates all the read state for the SDK. If the currentDate of this update is older then the most recent one, the update
     * is ignored.
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
            retryChannels()
            retryMessages()
            retryReactions()
            logger.v { "[retryFailedEntities] completed" }
        }
    } catch (e: Throwable) {
        logger.e(e) { "[retryFailedEntities] failed: $e" }
    }

    @SuppressWarnings("LongMethod")
    /**
     * This method needs to be refactored. It's too long.
     */
    private suspend fun restoreActiveChannels() {
        val recoverAll = !isFirstConnect.compareAndSet(true, false)
        logger.d { "[restoreActiveChannels] recoverAll: $recoverAll" }
        when (val result = updateActiveQueryChannels(recoverAll)) {
            is Result.Success -> {
                val updatedCids = result.value
                logger.v { "[restoreActiveChannels] updatedCids.size: ${updatedCids.size}" }
                updateActiveChannels(
                    recoverAll,
                    updatedCids,
                )
            }

            is Result.Failure -> {
                logger.e { "[restoreActiveChannels] failed: ${result.value}" }
                return
            }
        }
    }

    private suspend fun updateActiveQueryChannels(recoverAll: Boolean): Result<Set<String>> {
        // 2. update the results for queries that are actively being shown right now (synchronous)
        logger.d { "[updateActiveQueryChannels] recoverAll: $recoverAll" }
        val queryLogicsToRestore = logicRegistry.getActiveQueryChannelsLogic()
            .asSequence()
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
            logger.v { "[updateActiveQueryChannels] queryLogic.filter: ${queryLogic.filter()}" }
            queryLogic.queryFirstPage()
                .onError {
                    logger.e { "[updateActiveQueryChannels] request failed: $it" }
                    failed.set(it)
                }
                .onSuccessSuspend { foundChannels ->
                    logger.v {
                        "[updateActiveQueryChannels] request completed; foundChannels.size: ${foundChannels.size}"
                    }
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
        val missingCids: List<String> = stateRegistry.getActiveChannelStates()
            .asSequence()
            .filter { (it.recoveryNeeded || recoverAll) && !cidsToExclude.contains(it.cid) }
            .take(30)
            .map { it.cid }
            .toList()

        logger.v { "[updateActiveChannels] missingCids.size: ${missingCids.size}" }
        if (missingCids.isEmpty() || !online) {
            return
        }
        val filter = Filters.`in`("cid", missingCids)
        val request = QueryChannelsRequest(filter, 0, 30)
        logger.v { "[updateActiveChannels] request: $request" }
        chatClient.queryChannelsInternal(request)
            .await()
            .onError {
                logger.e { "[updateActiveChannels] request failed: $it" }
            }
            .onSuccessSuspend { foundChannels ->
                logger.v { "[updateActiveChannels] request completed; foundChannels.size: ${foundChannels.size}" }

                foundChannels.forEach { channel ->
                    val channelLogic = logicRegistry.channel(channel.type, channel.id)
                    channelLogic.updateDataForChannel(channel, channel.messages.size)
                }
                repos.storeStateForChannels(foundChannels)
                val foundCids = foundChannels.map { it.cid }
                val stillMissingCids = missingCids - foundCids.toSet()
                logger.v { "[updateActiveChannels] stillMissingCids.size: ${stillMissingCids.size}" }

                // create channels that are not present on the API
                stillMissingCids.forEach { cid ->
                    val (type, id) = cid.cidToTypeAndId()
                    val channelLogic = logicRegistry.channel(type, id)
                    channelLogic.watch(userPresence = userPresence)
                }
            }
    }

    private suspend fun retryChannels() {
        val cids = repos.selectChannelCidsBySyncNeeded()
        logger.d { "[retryChannels] cids.size: ${cids.size}" }
        cids.forEach { cid ->
            logger.v { "[retryChannels] channel.cid: $cid" }
            val channel = repos.selectChannel(cid) ?: return@forEach
            val result = if (channel.createdAt.exceedsSyncThreshold()) {
                logger.w { "[retryChannels] outdated channel($cid)" }
                repos.deleteChannel(cid)
                Result.Success(Unit)
            } else {
                logger.v { "[retryChannels] sending channel($cid)" }
                val params = CreateChannelParams(
                    members = channel.members.map { member ->
                        MemberData(member.getUserId(), extraData = member.extraData)
                    },
                    extraData = channel.extraData,
                )
                chatClient.createChannel(
                    channel.type,
                    channel.id,
                    params,
                ).await()
            }
            logger.v { "[retryChannels] result($cid).isSuccess: ${result is Result.Success}" }
        }
    }

    @VisibleForTesting
    internal suspend fun retryMessages() {
        logger.d { "[retryMessages] no args" }
        retryMessagesWithSyncedAttachments()
        retryMessagesWithPendingAttachments()
        logger.v { "[retryMessages] completed" }
    }

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

    private suspend fun retryMessagesWithSyncedAttachments() {
        val ids = repos.selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)
        logger.d { "[retryMgsWithSyncedAttachments] ids.size: ${ids.size}" }
        ids.forEach { id ->
            logger.v { "[retryMgsWithSyncedAttachments] message.id: $id" }
            val message = repos.selectMessage(id) ?: return@forEach
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
    private suspend fun retryMessagesWithPendingAttachments() {
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
                if (message.createdLocallyAt.exceedsSyncThreshold()) {
                    logger.w { "[retryMessagesWithPendingAttachments] outdated sending($id)" }
                    removeMessage(message).await()
                } else {
                    val (channelType, channelId) = message.cid.cidToTypeAndId()
                    chatClient.sendMessage(channelType, channelId, message, true).await()
                }
            }
        }
    }

    private suspend fun RepositoryFacade.markMessageAsFailed(message: Message) = insertMessage(message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY, updatedLocallyAt = Date(now())))

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
        return if (message.createdLocallyAt.exceedsSyncThreshold()) {
            logger.w { "[retrySendingOfMessageWithSyncedAttachments] outdated sending($id)" }
            removeMessage(message).await()
        } else {
            channelClient.sendMessage(message).await().also { result ->
                when (result) {
                    is Result.Success -> repos.insertMessage(
                        message.copy(syncStatus = SyncStatus.COMPLETED),
                    )

                    is Result.Failure -> if (result.value.isPermanent()) {
                        repos.markMessageAsFailed(message)
                    }
                }
            }
        }
    }

    private suspend fun removeReaction(reaction: Reaction): Call<Unit> = CoroutineCall(syncScope) {
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

    private suspend fun removeMessage(message: Message): Call<Unit> = CoroutineCall(syncScope) {
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

    private fun Date?.exceedsSyncThreshold(): Boolean = this == null || diff(now()) > syncMaxThreshold

    private enum class State {
        Idle,
        Syncing,
    }
}
