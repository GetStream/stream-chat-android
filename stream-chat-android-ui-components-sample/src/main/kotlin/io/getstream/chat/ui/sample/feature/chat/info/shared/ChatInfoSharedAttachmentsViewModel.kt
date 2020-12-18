package io.getstream.chat.ui.sample.feature.chat.info.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.AttachmentWithDate
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ChatInfoSharedAttachmentsViewModel(
    private val cid: String,
    private val attachmentsType: AttachmentsType,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val channelClient = chatClient.channel(cid)
    private val _state: MutableLiveData<State> = MutableLiveData(INITIAL_STATE)
    private var isLoadingMore: Boolean = false
    val state: LiveData<State> = _state

    init {
        viewModelScope.launch {
            fetchAttachments()
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.LoadMoreRequested -> loadMore()
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            val currentState = _state.value!!

            if (!currentState.canLoadMore || isLoadingMore) {
                return@launch
            }

            isLoadingMore = true
            fetchAttachments()
        }
    }

    private suspend fun fetchAttachments() {
        val currentState = state.value!!
        val result = if (attachmentsType == AttachmentsType.FILES) {
            channelClient.getFileAttachments(offset = currentState.results.size, limit = QUERY_LIMIT).await()
        } else {
            channelClient.getImageAttachments(offset = currentState.results.size, limit = QUERY_LIMIT).await()
        }

        if (result.isSuccess) {
            _state.value = currentState.copy(
                results = currentState.results + mapAttachments(result.data()),
                isLoading = false,
                canLoadMore = result.data().size == QUERY_LIMIT
            )
        } else {
            _state.value = currentState.copy(
                isLoading = false,
                canLoadMore = true,
            )
        }
    }

    private fun mapAttachments(attachments: List<AttachmentWithDate>): List<SharedAttachment> {
        return if (attachmentsType == AttachmentsType.FILES) {
            attachments
                .groupBy { mapDate(it.createdAt) }
                .flatMap { (date, attachments) ->
                    mutableListOf(SharedAttachment.DateDivider(date)) + attachments.map {
                        SharedAttachment.AttachmentItem(it.attachment)
                    }
                }
        } else {
            attachments.map { SharedAttachment.AttachmentItem(it.attachment) }
        }
    }

    private fun mapDate(date: Date): Date {
        // We only care about year and month
        return Calendar.getInstance().apply {
            time = date
            val year = get(Calendar.YEAR)
            val month = get(Calendar.MONTH)
            clear()
            set(year, month, getActualMinimum(Calendar.DAY_OF_MONTH))
        }.time
    }

    data class State(
        val canLoadMore: Boolean,
        val results: List<SharedAttachment>,
        val isLoading: Boolean,
    )

    sealed class Action {
        object LoadMoreRequested : Action()
    }

    enum class AttachmentsType {
        MEDIA, FILES
    }

    companion object {
        private const val QUERY_LIMIT = 30
        private val INITIAL_STATE = State(
            canLoadMore = true,
            results = emptyList(),
            isLoading = true
        )
    }
}

class ChatInfoSharedAttachmentsViewModelFactory(
    private val cid: String,
    private val type: ChatInfoSharedAttachmentsViewModel.AttachmentsType
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChatInfoSharedAttachmentsViewModel::class.java) {
            "ChatInfoSharedAttachmentsViewModelFactory can only create instances of ChatInfoSharedAttachmentsViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChatInfoSharedAttachmentsViewModel(cid, type) as T
    }
}
