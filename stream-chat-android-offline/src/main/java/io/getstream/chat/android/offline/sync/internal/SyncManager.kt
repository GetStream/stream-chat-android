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
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UserEntity
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.extensions.internal.users
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date

private const val MESSAGE_LIMIT = 30
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30
private const val QUERIES_TO_RETRY = 3

/**
 * This class is responsible to sync messages, reactions and channel data. It tries to sync then, if necessary, when connection
 * is reestabilished or when a health check even happens.
 */
internal class SyncManager(
    private val chatClient: ChatClient,
    private val globalState: GlobalMutableState,
    private val repos: RepositoryFacade,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val userPresence: Boolean,
) {

    private val entitiesRetryMutex = Mutex()
    private var logger = ChatLogger.get("SyncManager")
    internal val syncStateFlow: MutableStateFlow<SyncState?> = MutableStateFlow(null)
    private var firstConnect = true

    /**
     * Handles connection recover in the SDK. This method will sync the data, retry failed entities, update channels data, etc.
     */
    internal suspend fun connectionRecovered() {
        logger.logD("[connectionRecovered] firstConnect $firstConnect")
        if (firstConnect) {
            firstConnect = false
            connectionRecovered(false)
        } else {
            // the second time (ie coming from background, or reconnecting we should recover all)
            connectionRecovered(true)
        }
    }

    /**
     * Clears the data of SDK. Total unread count, connection state, etc
     */
    internal fun clearState() {
        globalState.run {
            _totalUnreadCount.value = 0
            _channelUnreadCount.value = 0
            _initialized.value = false
            _connectionState.value = ConnectionState.OFFLINE
            _banned.value = false
            _mutedUsers.value = emptyList()
        }
    }

    /**
     * Store the state to be request in a later moment. Should be used when SDK is disconnecting.
     */
    internal suspend fun storeSyncState() {
        syncStateFlow.value?.let { syncState ->
            val newSyncState = syncState.copy(activeChannelIds = logicRegistry.getActiveChannelsLogic().map { it.cid })
            repos.insertSyncState(newSyncState)
            syncStateFlow.value = newSyncState
        }
    }

    /**
     * Store the date of the latest events sync.
     * The date should be updated whenever the sync endpoint returns a successful response.
     *
     * @param latestEventDate The date of the last event returned by the sync endpoint.
     */
    internal suspend fun updateLastSyncedDate(latestEventDate: Date) {
        syncStateFlow.value?.let { syncState ->
            val newSyncState = syncState.copy(lastSyncedAt = latestEventDate)
            repos.insertSyncState(newSyncState)
            syncStateFlow.value = newSyncState
        }
    }

    /**
     * Updates all the read state for the SDK. If the currentDate of this update is older then the most recent one, the update
     * is ignored.
     *
     * @param userId The id of the current user
     * @param currentDate the moment of the update.
     */
    internal suspend fun updateAllReadStateForDate(userId: String, currentDate: Date) {
        val selectedState = repos.selectSyncState(userId)

        selectedState?.let { state ->
            if (state.markedAllReadAt?.before(currentDate) == true) {
                repos.insertSyncState(state.copy(markedAllReadAt = currentDate))
            }
        }

        syncStateFlow.value = selectedState ?: SyncState(userId)
    }

    /**
     * Loads te sync state for the user from the database.
     */
    internal suspend fun loadSyncStateForUser(userId: String) {
        syncStateFlow.value = repos.selectSyncState(userId) ?: SyncState(userId)
    }

    /**
     * Retry all entities that have failed. Channels, messages, reactions, etc.
     */
    internal suspend fun retryFailedEntities() {
        entitiesRetryMutex.withLock {
            val thread = Thread.currentThread().run { "$name:$id" }
            logger.logD("($thread) [retryFailedEntities] no args")
            // retry channels, messages and reactions in that order..
            retryChannels()
            retryMessages()
            retryReactions()
            logger.logV("[retryFailedEntities] completed")
        }
    }

    @SuppressWarnings("LongMethod")
    /**
     * This method needs to be refactored. It's too long.
     */
    private suspend fun connectionRecovered(recoverAll: Boolean = false) {
        logger.logD("[connectionRecovered] recoverAll $recoverAll")
        // 0. ensure load is complete
        val online = globalState.isOnline()

        // 1. Retry any failed requests first (synchronous)
        if (online) {
            retryFailedEntities()
        }

        var queriesToRetry: Int

        // 2. update the results for queries that are actively being shown right now (synchronous)
        val updatedChannelIds = mutableSetOf<String>()
        logicRegistry.getActiveQueryChannelsLogic()
            .filter { queryChannelsLogic -> queryChannelsLogic.state().recoveryNeeded.value || recoverAll }
            .take(QUERIES_TO_RETRY)
            .also { queryChannelStateList ->
                queriesToRetry = queryChannelStateList.size
            }
            .forEach { queryLogic ->
                // val
                val request = QueryChannelsRequest(
                    filter = queryLogic.state().filter,
                    offset = INITIAL_CHANNEL_OFFSET,
                    limit = CHANNEL_LIMIT,
                    querySort = queryLogic.state().sort,
                    messageLimit = MESSAGE_LIMIT,
                    memberLimit = MEMBER_LIMIT,
                )

                queryLogic.runQueryOnline(request)
                    .onSuccessSuspend { channels ->
                        queryLogic.updateOnlineChannels(channels, true)
                        updatedChannelIds.addAll(channels.map { it.cid })
                    }
            }

        // 3. update the data for all channels that are being show right now...
        // exclude ones we just updated
        // (synchronous)

        val cids: List<String> = stateRegistry.getActiveChannelStates()
            .asSequence()
            .filter { (it.recoveryNeeded || recoverAll) && !updatedChannelIds.contains(it.cid) }
            .take(30)
            .map { it.cid }
            .toList()

        logger.logI("recovery called: recoverAll: $recoverAll, online: $online retrying $queriesToRetry queries and ${cids.size} channels")

        var missingChannelIds = listOf<String>()

        if (cids.isNotEmpty() && online) {
            val filter = Filters.`in`("cid", cids)
            val request = QueryChannelsRequest(filter, 0, 30)
            chatClient.queryChannelsInternal(request)
                .await()
                .onSuccessSuspend { channels ->
                    val foundChannelIds = channels.map { it.id }

                    channels.forEach { channel ->
                        val channelLogic = logicRegistry.channel(channel.type, channel.id)
                        addTypingChannel(channelLogic)
                        channelLogic.updateDataFromChannel(channel)
                    }

                    missingChannelIds = cids.filterNot { foundChannelIds.contains(it) }
                    storeStateForChannels(channels)
                }

            // create channels that are not present on the API
            missingChannelIds.map { cid ->
                val (type, id) = cid.cidToTypeAndId()
                logicRegistry.channel(type, id)
            }.forEach { channelLogic ->
                channelLogic.watch(userPresence = userPresence)
            }
        }
    }

    private suspend fun retryChannels() {
        val cids = repos.selectChannelCidsBySyncNeeded()
        logger.logD("[retryChannels] cids.size: ${cids.size}")
        cids.forEach { cid ->
            logger.logD("[retryReactions] process channel($cid)")
            val channel = repos.selectChannelByCid(cid) ?: return@forEach
            logger.logV("[retryChannels] sending channel($cid)")
            val result = chatClient.createChannel(
                channel.type,
                channel.id,
                channel.members.map(UserEntity::getUserId),
                channel.extraData
            ).await()
            logger.logV("[retryChannels] result($cid).isSuccess: ${result.isSuccess}")
        }
    }

    @VisibleForTesting
    private suspend fun retryMessages() {
        logger.logD("[retryMessages] no args")
        retryMessagesWithSyncedAttachments()
        retryMessagesWithPendingAttachments()
        logger.logV("[retryMessages] completed")
    }

    private suspend fun retryReactions() {
        val ids = repos.selectReactionIdsBySyncStatus(SyncStatus.SYNC_NEEDED)
        logger.logD("[retryReactions] ids.size: ${ids.size}")
        ids.forEach { id ->
            logger.logD("[retryReactions] process reaction($id)")
            val reaction = repos.selectReactionById(id) ?: return@forEach
            val result = if (reaction.deletedAt != null) {
                logger.logV("[retryReactions] deleting reaction($id) for messageId: ${reaction.messageId}")
                chatClient.deleteReaction(reaction.messageId, reaction.type)
            } else {
                logger.logV("[retryReactions] sending reaction($id) for messageId: ${reaction.messageId}")
                chatClient.sendReaction(reaction, reaction.enforceUnique)
            }.await()
            logger.logV("[retryReactions] result($id).isSuccess: ${result.isSuccess}")
        }
    }

    private suspend fun retryMessagesWithSyncedAttachments() {
        val ids = repos.selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)
        logger.logD("[retryMgsWithSyncedAttachments] ids.size: ${ids.size}")
        ids.forEach { id ->
            logger.logD("[retryMgsWithSyncedAttachments] process message($id)")
            val message = repos.selectMessage(id) ?: return@forEach
            val channelClient = chatClient.channel(message.cid)
            val result = when {
                message.deletedAt != null -> {
                    logger.logV("[retryMgsWithSyncedAttachments] deleting message($id)")
                    channelClient.deleteMessage(message.id).await()
                }
                message.updatedLocallyAt != null && message.createdAt != null -> {
                    logger.logV("[retryMgsWithSyncedAttachments] updating message($id)")
                    channelClient.updateMessage(message).await()
                }
                else -> {
                    logger.logV("[retryMgsWithSyncedAttachments] sending message($id)")
                    channelClient.sendMessage(message).await().also { result ->
                        if (result.isSuccess) {
                            repos.insertMessage(message.copy(syncStatus = SyncStatus.COMPLETED))
                        } else if (result.isError && result.error().isPermanent()) {
                            repos.markMessageAsFailed(message)
                        }
                    }
                }
            }
            logger.logV("[retryMgsWithSyncedAttachments] result($id).isSuccess: ${result.isSuccess}")
        }
    }

    /**
     * Retries messages with [SyncStatus.AWAITING_ATTACHMENTS] status.
     */
    private suspend fun retryMessagesWithPendingAttachments() {
        val ids = repos.selectMessageIdsBySyncState(SyncStatus.AWAITING_ATTACHMENTS)
        logger.logD("[retryMessagesWithPendingAttachments] ids.size: ${ids.size}")
        ids.forEach { id ->
            logger.logD("[retryMessagesWithPendingAttachments] process message($id)")
            val message = repos.selectMessage(id) ?: return@forEach
            val isFailed = message.attachments.any { it.uploadState is Attachment.UploadState.Failed }
            if (isFailed) {
                logger.logV("[retryMessagesWithSyncedAttachments] marking message(${message.id}) as failed")
                repos.markMessageAsFailed(message)
            } else {
                logger.logV("[retryMessagesWithSyncedAttachments] sending message(${message.id})")
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

        logger.logI("storeStateForChannels stored ${channelsResponse.size} channels, ${configs.size} configs, ${users.size} users and ${messages.size} messages")
    }

    private suspend fun addTypingChannel(channelLogic: ChannelLogic) {
        globalState._typingChannels.emitAll(channelLogic.state().typing)
    }
}
