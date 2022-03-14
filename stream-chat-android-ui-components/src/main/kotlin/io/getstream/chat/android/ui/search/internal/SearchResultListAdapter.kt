package io.getstream.chat.android.ui.search.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.experimental.extensions.globalState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.ui.common.extensions.internal.asMention
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.message.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.search.internal.SearchResultListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.search.list.SearchResultListView.SearchResultSelectedListener

internal class SearchResultListAdapter(
    private val globalState: GlobalState = ChatClient.instance().globalState,
) : ListAdapter<Message, MessagePreviewViewHolder>(MessageDiffCallback) {

    private var searchResultSelectedListener: SearchResultSelectedListener? = null

    var messagePreviewStyle: MessagePreviewStyle? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        return StreamUiItemMentionListBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { binding ->
                messagePreviewStyle?.let(binding.root::styleView)
                MessagePreviewViewHolder(binding)
            }
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSearchResultSelectedListener(searchResultSelectedListener: SearchResultSelectedListener?) {
        this.searchResultSelectedListener = searchResultSelectedListener
    }

    inner class MessagePreviewViewHolder(
        private val binding: StreamUiItemMentionListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var message: Message

        init {
            binding.root.setOnClickListener {
                searchResultSelectedListener?.onSearchResultSelected(message)
            }
        }

        internal fun bind(message: Message) {
            this.message = message
            binding.root.setMessage(message, globalState.user.value?.asMention(context))
        }
    }

    private object MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Comparing only properties used by the ViewHolder
            return oldItem.id == newItem.id &&
                oldItem.createdAt == newItem.createdAt &&
                oldItem.createdLocallyAt == newItem.createdLocallyAt &&
                oldItem.text == newItem.text &&
                oldItem.user == newItem.user
        }
    }
}
