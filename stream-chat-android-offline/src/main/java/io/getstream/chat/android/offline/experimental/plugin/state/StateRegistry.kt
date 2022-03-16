package io.getstream.chat.android.offline.experimental.plugin.state

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import io.getstream.chat.android.offline.repository.domain.message.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

@InternalStreamChatApi
/**
 * Registry of all state objects exposed in the offline plugin. This class should have only one instance for the SDK.
 *
 * @param userStateFlow The state flow that provides the user once it is set.
 * @param messageRepository [MessageRepository] Repository for all messages
 * @param latestUsers Latest users of the SDK.
 * @param job A background job cancelled after calling [clear].
 * @param scope A scope for new coroutines.
 */
public class StateRegistry private constructor(
    private val userStateFlow: StateFlow<User?>,
    private val messageRepository: MessageRepository,
    private var latestUsers: StateFlow<Map<String, User>>,
    internal val job: Job,
    public val scope: CoroutineScope,
) {
    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsMutableState> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelMutableState> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadMutableState> = ConcurrentHashMap()

    public fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsState {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsMutableState(filter, sort, scope, latestUsers)
        }
    }

    public fun channel(channelType: String, channelId: String): ChannelState {
        return channels.getOrPut(channelType to channelId) {
            ChannelMutableState(channelType, channelId, scope, userStateFlow, latestUsers)
        }
    }

    /** Returns [ThreadState] of thread replies with parent message that has id equal to [messageId]. */
    public fun thread(messageId: String): ThreadState {
        return threads.getOrPut(messageId) {
            val (channelType, channelId) = runBlocking {
                messageRepository.selectMessage(messageId)?.cid?.cidToTypeAndId()
                    ?: error("There is not such message with messageId = $messageId")
            }
            val channelsState = channel(channelType, channelId)
            ThreadMutableState(messageId, channelsState.toMutableState(), scope)
        }
    }

    internal fun getActiveChannelStates(): List<ChannelState> = channels.values.toList()

    /** Clear state of all state objects. */
    public fun clear() {
        job.cancelChildren()
        queryChannels.clear()
        channels.clear()
        threads.clear()
    }

    internal companion object {
        private var instance: StateRegistry? = null

        /**
         * Gets the singleton of StateRegistry or creates it in the first call.
         *
         * @param job A background job cancelled after calling [clear].
         * @param scope A scope for new coroutines.
         * @param userStateFlow The state flow that provides the user once it is set.
         * @param messageRepository [MessageRepository] Repository for all messages
         * @param latestUsers Latest users of the SDK.
         */
        internal fun getOrCreate(
            job: Job,
            scope: CoroutineScope,
            userStateFlow: StateFlow<User?>,
            messageRepository: MessageRepository,
            latestUsers: StateFlow<Map<String, User>>,
        ): StateRegistry {
            return instance ?: StateRegistry(
                job = job,
                scope = scope,
                userStateFlow = userStateFlow,
                messageRepository = messageRepository,
                latestUsers = latestUsers,
            ).also { stateRegistry ->
                instance = stateRegistry
            }
        }

        /**
         * Gets the current Singleton of StateRegistry. If the initialization is not set yet, it returns null.
         */
        @Throws(IllegalArgumentException::class)
        internal fun get(): StateRegistry = requireNotNull(instance) {
            "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
                "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
        }
    }
}
