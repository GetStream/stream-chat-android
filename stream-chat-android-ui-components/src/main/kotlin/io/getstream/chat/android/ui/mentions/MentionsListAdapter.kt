package io.getstream.chat.android.ui.mentions

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.mentions.MentionsListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.mentions.MentionsListView.MentionSelectedListener
import io.getstream.chat.android.ui.messagepreview.MessagePreviewView
import io.getstream.chat.android.ui.utils.extensions.asMention
import io.getstream.chat.android.ui.utils.extensions.context

internal class MentionsListAdapter(
    context: Context,
    private val chatDomain: ChatDomain,
) : ListAdapter<Message, MessagePreviewViewHolder>(MessageDiffCallback) {

    private var mentionSelectedListener: MentionSelectedListener? = null
    private var dateFormatter = DateFormatter.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        return StreamUiItemMentionListBinding
            .inflate(parent.inflater, parent, false)
            .let { binding ->
                binding.root.dateFormatter = dateFormatter
                MessagePreviewViewHolder(binding.root)
            }
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        this.mentionSelectedListener = mentionSelectedListener
    }

    inner class MessagePreviewViewHolder(
        private val view: MessagePreviewView,
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var message: Message

        init {
            view.setOnClickListener {
                mentionSelectedListener?.onMentionSelected(message)
            }
        }

        internal fun bind(message: Message) {
            this.message = message
            view.setMessage(message, chatDomain.currentUser.asMention(context))
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
