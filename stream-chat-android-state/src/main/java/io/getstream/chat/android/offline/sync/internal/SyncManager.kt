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

package io.getstream.chat.android.offline.sync.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UserEntity
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.client.utils.onError
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.client.utils.stringify
import io.getstream.chat.android.core.internal.coroutines.Tube
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    private val repos: RepositoryFacade,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val userPresence: Boolean,
    scope: CoroutineScope,
    private val events: Tube<List<ChatEvent>> = Tube(),
) : SyncHistoryManager {

    private val logger = StreamLog.getLogger("Chat:SyncManager")

    private val syncScope = scope + SupervisorJob(scope.coroutineContext.job) +
        CoroutineExceptionHandler { context, throwable ->
            logger.e(throwable) { "[uncaughtCoroutineException] throwable: $throwable, context: $context" }
        }

    private val entitiesRetryMutex = Mutex()
    private val syncState = MutableStateFlow<SyncState?>(null)
    private val isFirstConnect = AtomicBoolean(true)

    private var eventsDisposable: Disposable? = null

    private val state = MutableStateFlow(State.Idle)

    override val syncedEvents: Flow<List<ChatEvent>> = events

    override fun start() {
        logger.d { "[start] no args" }
        val isDisposed = eventsDisposable?.isDisposed ?: true
        if (!isDisposed) return
        eventsDisposable = chatClient.subscribe { event ->
            onEvent(event)
        }
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

    @VisibleForTesting
    internal fun onEvent(event: ChatEvent) {
        when (event) {
            is ConnectingEvent -> syncScope.launch {
                logger.i { "[onEvent] ConnectingEvent received" }
            }
            is ConnectedEvent -> syncScope.launch {
                logger.i { "[onEvent] ConnectedEvent received" }
                onConnectionEstablished(currentUserId)
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

    /**
     * Handles connection recover in the SDK.
     * This method will sync the data, retry failed entities, update channels data, etc.
     */
    private suspend fun onConnectionEstablished(userId: String) = try {
        logger.i { "[onConnectionEstablished] >>> isFirstConnect: $isFirstConnect" }
        state.value = State.Syncing
        val online = clientState.isOnline
        logger.v { "[onConnectionEstablished] online: $online" }
        retryFailedEntities()

        if (syncState.value == null && syncState.value?.userId != userId) {
            updateAllReadStateForDate(userId, currentDate = Date())
        }
        performSync()
        restoreActiveChannels()
        state.value = State.Idle

        logger.i { "[onConnectionEstablished] <<< completed" }
    } catch (e: Throwable) {
        logger.e { "[onConnectionEstablished] failed: $e" }
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
            this.syncState.value = newSyncState
        }
    } catch (e: Throwable) {
        logger.i { "[connectionLost] failed: $e" }
    }

    private suspend fun performSync() {
        val cids = logicRegistry.getActiveChannelsLogic().map { it.cid }.ifEmpty {
            logger.w { "[performSync] no active cids found" }
            repos.selectSyncState(currentUserId)?.activeChannelIds ?: emptyList()
        }
        if (cids.isEmpty()) {
            logger.w { "[performSync] rejected (cids is empty)" }
            return
        }
        val syncState = syncState.value ?: repos.selectSyncState(currentUserId)
        val lastSyncAt = syncState?.lastSyncedAt ?: Date()
        val rawLastSyncAt = syncState?.rawLastSyncedAt
        logger.i { "[performSync] cids.size: ${cids.size}, lastSyncAt: $lastSyncAt, rawLastSyncAt: $rawLastSyncAt" }
        val result = if (rawLastSyncAt != null) {
            chatClient.getSyncHistory(cids, rawLastSyncAt).await()
        } else {
            chatClient.getSyncHistory(cids, lastSyncAt).await()
        }

        if (result.isSuccess) {
            val sortedEvents = result.data().sortedBy { it.createdAt }
            logger.d { "[performSync] succeed. events: ${sortedEvents.size}" }
            val latestEvent = sortedEvents.lastOrNull()
            val latestEventDate = latestEvent?.createdAt ?: Date()
            val rawLatestEventDate = latestEvent?.rawCreatedAt
            updateLastSyncedDate(latestEventDate, rawLatestEventDate)
            sortedEvents.forEach {
                if (it is MarkAllReadEvent) {
                    updateAllReadStateForDate(it.user.id, it.createdAt)
                }
            }
            if (sortedEvents.isNotEmpty() && rawLastSyncAt != null && rawLastSyncAt != rawLatestEventDate) {
                events.emit(sortedEvents)
                logger.v { "[performSync] events emission completed" }
            } else {
                logger.v { "[performSync] no events to emit" }
            }
        } else {
            logger.e { "[performSync] failed(${result.error().stringify()})" }
        }
    }

    /**
     * Store the date of the latest events sync.
     * The date should be updated whenever the sync endpoint returns a successful response.
     *
     * @param latestEventDate The date of the last event returned by the sync endpoint.
     */
    private suspend fun updateLastSyncedDate(latestEventDate: Date, rawLatestEventDate: String?) {
        logger.d {
            "[updateLastSyncedDate] latestEventDate: $latestEventDate, rawLatestEventDate: $rawLatestEventDate "
        }
        syncState.value?.let { syncState ->
            val newSyncState = syncState.copy(lastSyncedAt = latestEventDate, rawLastSyncedAt = rawLatestEventDate)
            repos.insertSyncState(newSyncState)
            this.syncState.value = newSyncState
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
            return
        }
        logger.d { "[updateAllReadStateForDate] userId: $userId, currentDate: $currentDate" }
        syncState.value = repos.selectSyncState(userId)?.let { selectedState ->
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
        logger.e { "[retryFailedEntities] failed: $e" }
    }

    @SuppressWarnings("LongMethod")
    /**
     * This method needs to be refactored. It's too long.
     */
    private suspend fun restoreActiveChannels() {
        val recoverAll = !isFirstConnect.compareAndSet(true, false)
        logger.d { "[restoreActiveChannels] recoverAll: $recoverAll" }
        val result = updateActiveQueryChannels(recoverAll)
        if (result.isError) {
            logger.e { "[restoreActiveChannels] failed: ${result.error()}" }
            return
        }
        val updatedCids = result.data()
        logger.v { "[restoreActiveChannels] updatedCids.size: ${updatedCids.size}" }
        updateActiveChannels(
            recoverAll,
            updatedCids
        )
    }

    private suspend fun updateActiveQueryChannels(recoverAll: Boolean): Result<Set<String>> {
        // 2. update the results for queries that are actively being shown right now (synchronous)
        logger.d { "[updateActiveQueryChannels] recoverAll: $recoverAll" }
        val queryLogicsToRestore = logicRegistry.getActiveQueryChannelsLogic()
            .asSequence()
            .filter { queryChannelsLogic -> queryChannelsLogic.recoveryNeeded()?.value == true || recoverAll }
            .take(QUERIES_TO_RETRY)
            .toList()
        if (queryLogicsToRestore.isEmpty()) {
            logger.v { "[updateActiveQueryChannels] queryLogicsToRestore.size: ${queryLogicsToRestore.size}" }
            return Result.success(emptySet())
        }
        logger.v { "[updateActiveQueryChannels] queryLogicsToRestore.size: ${queryLogicsToRestore.size}" }

        val failed = AtomicReference<ChatError>()
        val updatedCids = mutableSetOf<String>()
        queryLogicsToRestore.forEach { queryLogic ->
            logger.v { "[updateActiveQueryChannels] queryLogic.filter: ${queryLogic.filter()}" }
            queryLogic.queryFirstPage()
                .onError {
                    logger.e { "[updateActiveQueryChannels] request failed: ${it.stringify()}" }
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
        return when (val chatError = failed.get()) {
            null -> Result.success(updatedCids)
            else -> Result.error(chatError)
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
                logger.e { "[updateActiveChannels] request failed: ${it.stringify()}" }
            }
            .onSuccessSuspend { foundChannels ->
                logger.v { "[updateActiveChannels] request completed; foundChannels.size: ${foundChannels.size}" }

                foundChannels.forEach { channel ->
                    val channelLogic = logicRegistry.channel(channel.type, channel.id)
                    channelLogic.updateDataFromChannel(channel)
                }
                storeStateForChannels(foundChannels)
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
        logger.v { "[retryChannels] cids.size: ${cids.size}" }
        cids.forEach { cid ->
            logger.d { "[retryReactions] process channel($cid)" }
            val channel = repos.selectChannelByCid(cid) ?: return@forEach
            logger.v { "[retryChannels] sending channel($cid)" }
            val result = chatClient.createChannel(
                channel.type,
                channel.id,
                channel.members.map(UserEntity::getUserId),
                channel.extraData
            ).await()
            logger.v { "[retryChannels] result($cid).isSuccess: ${result.isSuccess}" }
        }
    }

    @VisibleForTesting
    private suspend fun retryMessages() {
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
                logger.v { "[retryReactions] deleting reaction($id) for messageId: ${reaction.messageId}" }
                chatClient.deleteReaction(reaction.messageId, reaction.type)
            } else {
                logger.v { "[retryReactions] sending reaction($id) for messageId: ${reaction.messageId}" }
                chatClient.sendReaction(reaction, reaction.enforceUnique)
            }.await()
            logger.v { "[retryReactions] result($id).isSuccess: ${result.isSuccess}" }
        }
    }

    private suspend fun retryMessagesWithSyncedAttachments() {
        val ids = repos.selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)
        logger.d { "[retryMgsWithSyncedAttachments] ids.size: ${ids.size}" }
        ids.forEach { id ->
            logger.d { "[retryMgsWithSyncedAttachments] process message($id)" }
            val message = repos.selectMessage(id) ?: return@forEach
            val channelClient = chatClient.channel(message.cid)
            val result = when {
                message.deletedAt != null -> {
                    logger.v { "[retryMgsWithSyncedAttachments] deleting message($id)" }
                    channelClient.deleteMessage(message.id).await()
                }
                message.updatedLocallyAt != null && message.createdAt != null -> {
                    logger.v { "[retryMgsWithSyncedAttachments] updating message($id)" }
                    channelClient.updateMessage(message).await()
                }
                else -> {
                    logger.v { "[retryMgsWithSyncedAttachments] sending message($id)" }
                    channelClient.sendMessage(message).await().also { result ->
                        if (result.isSuccess) {
                            repos.insertMessage(message.copy(syncStatus = SyncStatus.COMPLETED))
                        } else if (result.isError && result.error().isPermanent()) {
                            repos.markMessageAsFailed(message)
                        }
                    }
                }
            }
            logger.v { "[retryMgsWithSyncedAttachments] result($id).isSuccess: ${result.isSuccess}" }
        }
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
                logger.v { "[retryMessagesWithSyncedAttachments] marking message(${message.id}) as failed" }
                repos.markMessageAsFailed(message)
            } else {
                logger.v { "[retryMessagesWithSyncedAttachments] sending message(${message.id})" }
                val (channelType, channelId) = message.cid.cidToTypeAndId()
                chatClient.sendMessage(channelType, channelId, message, true).await()
            }
        }
    }

    private suspend fun RepositoryFacade.markMessageAsFailed(message: Message) =
        insertMessage(message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY, updatedLocallyAt = Date()))

    private suspend fun storeStateForChannels(channelsResponse: Collection<Channel>) {
        val users = mutableMapOf<String, User>()
        val configs: MutableCollection<ChannelConfig> = mutableSetOf()
        // start by gathering all the users
        val messages = mutableListOf<Message>()

        for (channel in channelsResponse) {
            users.putAll(channel.users().associateBy { it.id })
            configs += ChannelConfig(channel.type, channel.config)

            channel.messages.forEach { message ->
                message.enrichWithCid(channel.cid)
                users.putAll(message.users().associateBy { it.id })
            }

            messages.addAll(channel.messages)
        }

        repos.storeStateForChannels(
            configs = configs,
            users = users.values.toList(),
            channels = channelsResponse,
            messages = messages
        )

        logger.i {
            "storeStateForChannels stored ${channelsResponse.size} channels, " +
                "${configs.size} configs, " +
                "${users.size} users " +
                "and ${messages.size} messages"
        }
    }

    private enum class State {
        Idle, Syncing
    }
}
