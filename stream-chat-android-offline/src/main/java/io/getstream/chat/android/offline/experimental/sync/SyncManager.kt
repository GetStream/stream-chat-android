package io.getstream.chat.android.offline.experimental.sync

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UserEntity
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.message.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.model.SyncState
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateRepository
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
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
    private val activeEntitiesManager: ActiveEntitiesManager,
) : SyncStateRepository by repos {

    private val entitiesRetryMutex = Mutex()
    private var logger = ChatLogger.get("SyncManager")
    internal val syncStateFlow: MutableStateFlow<SyncState?> = MutableStateFlow(null)

    internal suspend fun connectionRecovered(recoverAll: Boolean = false) {
        // 0. ensure load is complete

        val online = globalState.isOnline()

        // 1. Retry any failed requests first (synchronous)
        if (online) {
            retryFailedEntities()
        }

        var queriesToRetry = 0

        // 2. update the results for queries that are actively being shown right now (synchronous)
        val updatedChannelIds = mutableSetOf<String>()
        activeEntitiesManager.activeQueries()
            .filter { it.recoveryNeeded.value || recoverAll }
            .take(QUERIES_TO_RETRY)
            .also { controllerList ->
                queriesToRetry = controllerList.size
            }
            .forEach { queryChannelController ->
                val pagination = QueryChannelsPaginationRequest(
                    queryChannelController.sort,
                    INITIAL_CHANNEL_OFFSET,
                    CHANNEL_LIMIT,
                    MESSAGE_LIMIT,
                    MEMBER_LIMIT
                )

                val response = queryChannelController.runQueryOnline(pagination)
                if (response.isSuccess) {
                    queryChannelController.updateOnlineChannels(response.data(), true)
                    updatedChannelIds.addAll(response.data().map { it.cid })
                }
            }

        // 3. update the data for all channels that are being show right now...
        // exclude ones we just updated
        // (synchronous)
        val cids: List<String> = activeEntitiesManager.activeChannelsMap()
            .entries
            .asSequence()
            .filter { it.value.recoveryNeeded || recoverAll }
            .filterNot { updatedChannelIds.contains(it.key) }
            .take(30)
            .map { it.key }
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
                    for (c in channels) {
                        val channelController = activeEntitiesManager.channel(c)
                        addTypingChannel(channelController)
                        channelController.updateDataFromChannel(c)
                    }
                    missingChannelIds = cids.filterNot { foundChannelIds.contains(it) }
                    storeStateForChannels(channels)
                }

            // create channels that are not present on the API
            missingChannelIds.map(activeEntitiesManager::channel)
                .forEach { channelController ->
                    channelController.watch()
                }
        }
    }

    internal suspend fun retryFailedEntities() {
        entitiesRetryMutex.withLock {
            // retry channels, messages and reactions in that order..
            val channels = retryChannels()
            val messages = retryMessages()
            val reactions = retryReactions()
            logger.logI("Retried ${channels.size} channel entities, ${messages.size} messages and ${reactions.size} reaction entities")
        }
    }

    @VisibleForTesting
    private suspend fun retryChannels(): List<Channel> {
        return repos.selectChannelsSyncNeeded().onEach { channel ->
            val result = chatClient.createChannel(
                channel.type,
                channel.id,
                channel.members.map(UserEntity::getUserId),
                channel.extraData
            ).await()

            when {
                result.isSuccess -> {
                    channel.syncStatus = SyncStatus.COMPLETED
                    repos.insertChannel(channel)
                }
                result.isError && result.error().isPermanent() -> {
                    channel.syncStatus = SyncStatus.FAILED_PERMANENTLY
                    repos.insertChannel(channel)
                }
            }
        }
    }

    @VisibleForTesting
    private suspend fun retryMessages(): List<Message> {
        return retryMessagesWithSyncedAttachments() + retryMessagesWithPendingAttachments()
    }

    @VisibleForTesting
    private suspend fun retryReactions(): List<Reaction> {
        return repos.selectReactionsBySyncStatus(SyncStatus.SYNC_NEEDED).onEach { reaction ->
            val result = if (reaction.deletedAt != null) {
                chatClient.deleteReaction(reaction.messageId, reaction.type)
            } else {
                chatClient.sendReaction(reaction, reaction.enforceUnique)
            }.await()

            if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
            } else if (result.error().isPermanent()) {
                reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
            }

            repos.insertReaction(reaction)
        }
    }

    private suspend fun retryMessagesWithSyncedAttachments(): List<Message> {
        val (messages, nonCorrectStateMessages) = repos.selectMessageBySyncState(SyncStatus.SYNC_NEEDED).partition {
            it.attachments.all { attachment -> attachment.uploadState === Attachment.UploadState.Success }
        }

        if (nonCorrectStateMessages.isNotEmpty()) {
            val message = nonCorrectStateMessages.first()
            val attachmentUploadState =
                message.attachments.firstOrNull { it.uploadState != Attachment.UploadState.Success }
                    ?: Attachment.UploadState.Success
            logger.logE(
                "Logical error. Messages with non-synchronized attachments should have another sync status!" +
                    "\nMessage has ${message.syncStatus} syncStatus, while attachment has $attachmentUploadState upload state"
            )
        }

        messages.forEach { message ->
            val channelClient = chatClient.channel(message.cid)

            when {
                message.deletedAt != null -> {
                    logger.logD("Deleting message: ${message.id}")
                    channelClient.deleteMessage(message.id).await()
                }
                message.updatedLocallyAt != null -> {
                    logger.logD("Updating message: ${message.id}")
                    channelClient.updateMessage(message).await()
                }
                else -> {
                    logger.logD("Sending message: ${message.id}")
                    val result = channelClient.sendMessage(message).await()

                    if (result.isSuccess) {
                        repos.insertMessage(message.copy(syncStatus = SyncStatus.COMPLETED))
                    } else if (result.isError && result.error().isPermanent()) {
                        markMessageAsFailed(message)
                    }
                }
            }
        }

        return messages
    }

    /**
     * Retries messages with [SyncStatus.AWAITING_ATTACHMENTS] status.
     */
    private suspend fun retryMessagesWithPendingAttachments(): List<Message> {
        val retriedMessages = repos.selectMessageBySyncState(SyncStatus.AWAITING_ATTACHMENTS)

        val (failedMessages, needToBeSync) = retriedMessages.partition { message ->
            message.attachments.any { it.uploadState is Attachment.UploadState.Failed }
        }

        failedMessages.forEach { markMessageAsFailed(it) }

        needToBeSync.map { message ->
            message to activeEntitiesManager.channel(message.cid)
        }.forEach { (messageId, channel) ->
            channel.retrySendMessage(messageId)
        }

        return retriedMessages
    }

    private suspend fun markMessageAsFailed(message: Message) =
        repos.insertMessage(message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY, updatedLocallyAt = Date()))

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

    private suspend fun addTypingChannel(channelController: ChannelController) {
        globalState._typingChannels.emitAll(channelController.typing)
    }

    internal suspend fun storeSyncState() {
        syncStateFlow.value?.let { syncState ->
            val newSyncState = syncState.copy(activeChannelIds = activeEntitiesManager.activeChannelsCids())
            repos.insertSyncState(newSyncState)
            syncStateFlow.value = newSyncState
        }
    }
}
