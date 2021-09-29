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
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.ChatDomain
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
 */
public class ChannelListViewModel(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject? = null,
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30,
    private val messageLimit: Int = 1,
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    public val state: LiveData<State> = stateMerger
    public val typingEvents: LiveData<TypingEvent>
        get() = chatDomain.typingUpdates.asLiveData()

    private val paginationStateMerger = MediatorLiveData<PaginationState>()
    public val paginationState: LiveData<PaginationState> = Transformations.distinctUntilChanged(paginationStateMerger)
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    public val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

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

                val channelState = queryChannelsController.channelsState.map { channelState ->
                    handleChannelState(channelState, queryChannelsController.mutedChannelIds.value)
                }.asLiveData()

                stateMerger.addSource(channelState) { state -> stateMerger.value = state }

                stateMerger.addSource(queryChannelsController.mutedChannelIds.asLiveData()) { mutedChannels ->
                    val state = stateMerger.value

                    if (state?.channels?.isNotEmpty() == true) {
                        stateMerger.value = state.copy(channels = parseMutedChannels(state.channels, mutedChannels))
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
            }
        }
    }

    private fun handleChannelState(
        channelState: QueryChannelsController.ChannelsState,
        channelMutesIds: List<String>,
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
                    channels = parseMutedChannels(channelState.channels, channelMutesIds),
                )
        }
    }

    public fun onAction(action: Action) {
        when (action) {
            is Action.ReachedEndOfList -> requestMoreChannels()
        }.exhaustive
    }

    public fun leaveChannel(channel: Channel) {
        chatDomain.leaveChannel(channel.cid).enqueue(
            onError = { _errorEvents.postValue(Event(ErrorEvent.LeaveChannelError(it))) }
        )
    }

    public fun deleteChannel(channel: Channel) {
        chatDomain.deleteChannel(channel.cid).enqueue(
            onError = { _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError(it))) }
        )
    }

    public fun hideChannel(channel: Channel) {
        chatDomain.hideChannel(channel.cid, true).enqueue(
            onError = { _errorEvents.postValue(Event(ErrorEvent.HideChannelError(it))) }
        )
    }

    public fun markAllRead() {
        chatDomain.markAllRead().enqueue()
    }

    private fun requestMoreChannels() {
        filterLiveData.value?.let { chatDomain.queryChannelsLoadMore(it, sort).enqueue() }
    }

    private fun setPaginationState(reducer: PaginationState.() -> PaginationState) {
        paginationStateMerger.value = reducer(paginationStateMerger.value ?: PaginationState())
    }

    public data class State(val isLoading: Boolean, val channels: List<Channel>)

    private fun parseMutedChannels(
        channels: List<Channel>,
        channelMutesIds: List<String>,
    ): List<Channel> {
        return channels.map { channel ->
            when {
                channel.isMuted != channelMutesIds.contains(channel.id) ->
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
