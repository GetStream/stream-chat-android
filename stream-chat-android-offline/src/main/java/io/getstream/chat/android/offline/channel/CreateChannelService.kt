package io.getstream.chat.android.offline.channel

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.extensions.retry
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import java.util.Date

/**
 * Service class that has responsibility of creating a new channel.
 *
 * @param client Instance of [ChatClient] to perform request.
 * @param repositoryFacade [RepositoryFacade] to cache intermediate data and final result.
 * @param getChannelController Function that returns an instance of [ChannelController] by cid.
 * @param activeQueries Collection of active [QueryChannelsController].
 */
internal class CreateChannelService(
    private val scope: CoroutineScope,
    private val client: ChatClient,
    private val repositoryFacade: RepositoryFacade,
    private val getChannelController: (cid: String) -> ChannelController,
    private val activeQueries: Collection<QueryChannelsController>,
) {

    /**
     * Creates a channel. It makes API request if there is connection or schedules the create channel request later when
     * connection is established.
     *
     * @param channel The [Channel] instance that going to be created.
     * @param isOnline Boolean flag indicating if there is a connection to Backend.
     * @param currentUser Current user connected to WebSocket. Null in case of not connected state of SDK.
     *
     * @return Result of the create channel request.
     */
    suspend fun createChannel(channel: Channel, isOnline: Boolean, currentUser: User?): Result<Channel> {
        return try {
            channel.createdAt = channel.createdAt ?: Date()
            channel.syncStatus = if (isOnline) {
                SyncStatus.IN_PROGRESS
            } else {
                SyncStatus.SYNC_NEEDED
            }

            if (currentUser != null && channel.createdBy != currentUser) {
                channel.createdBy = currentUser
            }

            val channelController = getChannelController(channel.cid)
            channelController.updateDataFromChannel(channel)

            // Update Room State
            repositoryFacade.insertChannel(channel)

            // Add to query controllers
            activeQueries.forEach { query ->
                query.updateQueryChannelCollectionByNewChannel(channel)
            }

            // make the API call and follow retry policy
            if (isOnline) {
                val members = channel.members.map(Member::getUserId)
                // TODO: Remove after migrating ChatDomain
                val result = client.createChannel(channel.type, channel.id, members, channel.extraData).retry(scope, client.retryPolicy).await()

                if (result.isSuccess) {
                    channel.syncStatus = SyncStatus.COMPLETED
                    repositoryFacade.insertChannel(channel)
                    Result(result.data())
                } else {
                    if (result.error().isPermanent()) {
                        channel.syncStatus = SyncStatus.FAILED_PERMANENTLY
                    } else {
                        channel.syncStatus = SyncStatus.SYNC_NEEDED
                    }
                    repositoryFacade.insertChannel(channel)
                    Result(result.error())
                }
            } else {
                Result(channel)
            }
        } catch (e: IllegalStateException) {
            Result(ChatError(cause = e))
        }
    }
}
