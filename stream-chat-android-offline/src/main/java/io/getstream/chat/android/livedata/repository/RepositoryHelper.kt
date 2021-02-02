package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.extensions.users
import io.getstream.chat.android.livedata.repository.mapper.toModel
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.isRequestingMoreThanLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.Date

internal class RepositoryHelper(
    factory: RepositoryFactory,
    private val scope: CoroutineScope,
) {
    private val userRepository = factory.createUserRepository()
    val configs = factory.createChannelConfigRepository()
    val channels = factory.createChannelRepository()
    val queryChannels = factory.createQueryChannelsRepository()
    val messages = factory.createMessageRepository()
    val reactions = factory.createReactionRepository()
    val syncState = factory.createSyncStateRepository()

    internal suspend fun selectChannels(
        channelIds: List<String>,
        defaultConfig: Config,
        pagination: AnyChannelPaginationRequest? = null,
    ): List<Channel> {
        // fetch the channel entities from room
        val channelEntities = channels.select(channelIds)
        val messagesMap = if (pagination?.isRequestingMoreThanLastMessage() != false) {
            // with postgres this could be optimized into a single query instead of N, not sure about sqlite on android
            // sqlite has window functions: https://sqlite.org/windowfunctions.html
            // but android runs a very dated version: https://developer.android.com/reference/android/database/sqlite/package-summary
            channelIds.map { cid ->
                scope.async {
                    cid to messages.selectMessagesForChannel(cid, pagination, ::selectUser)
                }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        // convert the channels
        return channelEntities.map { entity ->
            entity.toModel(::selectUser) { messages.select(it, ::selectUser) }.apply {
                config = configs.select(type)?.config ?: defaultConfig
                messages = messagesMap[cid] ?: messages
            }
        }
    }

    internal suspend fun updateCurrentUser(currentUser: User) {
        userRepository.insertMe(currentUser)
    }

    internal suspend fun insertUser(user: User) {
        userRepository.insertUser(user)
    }

    internal suspend fun insertManyUsers(users: List<User>) {
        userRepository.insert(users)
    }

    internal suspend fun selectCurrentUser(): User? {
        return userRepository.selectMe()
    }

    internal suspend fun selectMessageSyncNeeded(): List<Message> {
        return messages.selectSyncNeeded(::selectUser)
    }

    internal suspend fun selectMessages(messageIds: List<String>): List<Message> =
        messages.select(messageIds, ::selectUser)

    internal suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> = reactions.selectUserReactionsToMessage(messageId, userId, ::selectUser)

    internal suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) =
        reactions.updateReactionsForMessageByDeletedDate(userId, messageId, deletedAt)

    @VisibleForTesting
    internal suspend fun selectUserReactionsToMessageByType(
        messageId: String,
        userId: String,
        type: String,
    ) = reactions.selectUserReactionsToMessageByType(messageId, userId, type, ::selectUser)

    internal suspend fun selectReactionSyncNeeded(): List<Reaction> = reactions.selectSyncNeeded(::selectUser)

    suspend fun insertChannel(channel: Channel) {
        insertChannels(listOf(channel))
    }

    suspend fun insertChannels(channels: Collection<Channel>) {
        this.channels.insertChannels(channels)
        userRepository.insert(channels.flatMap(Channel::users))
    }

    suspend fun removeChannel(cid: String) {
        channels.delete(cid)
    }

    private suspend fun selectUser(userId: String): User =
        userRepository.select(userId) ?: error("User with the userId: `$userId` has not been found")
}
