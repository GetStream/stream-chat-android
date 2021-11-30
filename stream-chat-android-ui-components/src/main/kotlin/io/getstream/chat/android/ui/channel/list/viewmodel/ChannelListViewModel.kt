package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.getstream.sdk.chat.utils.extensions.defaultChannelListFilter
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.TaggedLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.querychannels.ChatEventHandler
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.ui.common.extensions.internal.EXTRA_DATA_MUTED
import io.getstream.chat.android.ui.common.extensions.internal.isMuted
import kotlinx.coroutines.flow.map

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.ChannelListView].
 * Responsible for keeping the channels list up to date.
 * Can be bound to the view using [ChannelListViewModel.bindView] function.
 *
 * @param chatDomain Entry point for all livedata & offline operations.
 * @param filter Filter for querying channels, should never be empty.
 * @param sort Defines the ordering of the channels.
 * @param limit The maximum number of channels to fetch.
 * @param messageLimit The number of messages to fetch for each channel.
 * @param chatEventHandler The instance of [ChatEventHandler] that will be used to handle channel updates event for this combination of [sort] and [filter].
 */
public class ChannelListViewModel(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject? = null,
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30,
    private val messageLimit: Int = 1,
    private val chatEventHandler: ChatEventHandler? = null,
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    public val state: LiveData<State> = stateMerger
    public val typingEvents: LiveData<TypingEvent>
        get() = chatDomain.typingUpdates.asLiveData()

    private val paginationStateMerger = MediatorLiveData<PaginationState>()
    public val paginationState: LiveData<PaginationState> = Transformations.distinctUntilChanged(paginationStateMerger)
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    public val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    private val logger: TaggedLogger = ChatLogger.get("ChannelListViewModel")

    private val filterLiveData: LiveData<FilterObject?> =
        filter?.let(::MutableLiveData) ?: chatDomain.user.map(Filters::defaultChannelListFilter).asLiveData()

    init {
        stateMerger.addSource(filterLiveData) { filter ->
            if (filter != null) {
                initData(filter)
            }
        }
    }

    private fun initData(filterObject: FilterObject) {
        stateMerger.value = INITIAL_STATE

        chatDomain.queryChannels(filterObject, sort, limit, messageLimit).enqueue { queryChannelsControllerResult ->
            if (queryChannelsControllerResult.isSuccess) {
                val queryChannelsController = queryChannelsControllerResult.data()

                chatEventHandler?.let { eventsHandler ->
                    queryChannelsController.chatEventHandler = eventsHandler
                }

                val channelState = queryChannelsController.channelsState.map { channelState ->
                    handleChannelState(channelState, chatDomain.channelMutes.value)
                }.asLiveData()

                stateMerger.addSource(channelState) { state -> stateMerger.value = state }

                stateMerger.addSource(chatDomain.channelMutes.asLiveData()) { channelMutes ->
                    val state = stateMerger.value

                    if (state?.channels?.isNotEmpty() == true) {
                        stateMerger.value = state.copy(channels = parseMutedChannels(state.channels, channelMutes))
                    } else {
                        stateMerger.value = state?.copy()
                    }
                }

                paginationStateMerger.addSource(queryChannelsController.loadingMore.asLiveData()) { loadingMore ->
                    setPaginationState { copy(loadingMore = loadingMore) }
                }
                paginationStateMerger.addSource(queryChannelsController.endOfChannels.asLiveData()) { endOfChannels ->
                    setPaginationState { copy(endOfChannels = endOfChannels) }
                }
            } else {
                logger.logE("Could not query channels. Error: ${queryChannelsControllerResult.error()}")
            }
        }
    }

    private fun handleChannelState(
        channelState: QueryChannelsController.ChannelsState,
        channelMutes: List<ChannelMute>,
    ): State {
        return when (channelState) {
            is QueryChannelsController.ChannelsState.NoQueryActive,
            is QueryChannelsController.ChannelsState.Loading,
            -> State(isLoading = true, emptyList())
            is QueryChannelsController.ChannelsState.OfflineNoResults -> State(
                isLoading = false,
                channels = emptyList(),
            )
            is QueryChannelsController.ChannelsState.Result ->
                State(
                    isLoading = false,
                    channels = parseMutedChannels(channelState.channels, channelMutes),
                )
        }
    }

    public fun onAction(action: Action) {
        when (action) {
            is Action.ReachedEndOfList -> requestMoreChannels()
        }
    }

    public fun leaveChannel(channel: Channel) {
        chatDomain.leaveChannel(channel.cid).enqueue(
            onError = { chatError ->
                logger.logE("Could not leave channel with id: ${channel.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                _errorEvents.postValue(Event(ErrorEvent.LeaveChannelError(chatError)))
            }
        )
    }

    public fun deleteChannel(channel: Channel) {
        chatDomain.deleteChannel(channel.cid).enqueue(
            onError = { chatError ->
                logger.logE("Could not delete channel with id: ${channel.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError(chatError)))
            }
        )
    }

    public fun hideChannel(channel: Channel) {
        chatDomain.hideChannel(channel.cid, true).enqueue(
            onError = { chatError ->
                logger.logE("Could not hide channel with id: ${channel.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                _errorEvents.postValue(Event(ErrorEvent.HideChannelError(chatError)))
            }
        )
    }

    public fun markAllRead() {
        chatDomain.markAllRead().enqueue(
            onError = { chatError ->
                logger.logE("Could not mark all messages as read. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
            }
        )
    }

    private fun requestMoreChannels() {
        filterLiveData.value?.let { filter ->
            chatDomain.queryChannelsLoadMore(filter, sort, limit, messageLimit).enqueue(
                onError = { chatError ->
                    logger.logE("Could not load more channels. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                }
            )
        }
    }

    private fun setPaginationState(reducer: PaginationState.() -> PaginationState) {
        paginationStateMerger.value = reducer(paginationStateMerger.value ?: PaginationState())
    }

    public data class State(val isLoading: Boolean, val channels: List<Channel>)

    private fun parseMutedChannels(
        channels: List<Channel>,
        channelMutes: List<ChannelMute>,
    ): List<Channel> {
        val mutedChannelsIds = channelMutes.map { channelMute -> channelMute.channel.id }.toSet()
        return channels.map { channel ->
            when {
                channel.isMuted != channel.id in mutedChannelsIds ->
                    channel.copy(extraData = channel.extraData.clone(EXTRA_DATA_MUTED, !channel.isMuted))

                else -> channel
            }
        }
    }

    private fun <K, V> Map<K, V>.clone(changeKey: K, changeValue: V): MutableMap<K, V> {
        val originalMap = this

        return mutableMapOf<K, V>().apply {
            putAll(originalMap)
            put(changeKey, changeValue)
        }
    }

    public data class PaginationState(
        val loadingMore: Boolean = false,
        val endOfChannels: Boolean = false,
    )

    public sealed class Action {
        public object ReachedEndOfList : Action()
    }

    public sealed class ErrorEvent(public open val chatError: ChatError) {
        public data class LeaveChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
        public data class DeleteChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
        public data class HideChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
    }

    public companion object {
        @JvmField
        public val DEFAULT_SORT: QuerySort<Channel> = QuerySort.desc("last_updated")

        private val INITIAL_STATE: State = State(isLoading = true, channels = emptyList())
    }
}
