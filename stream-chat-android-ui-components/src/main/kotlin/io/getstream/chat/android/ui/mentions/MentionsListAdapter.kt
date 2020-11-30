package io.getstream.chat.android.ui.mentions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.databinding.StreamItemMentionListBinding
import io.getstream.chat.android.ui.mentions.MentionsListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.mentions.MentionsListView.MentionSelectedListener
import io.getstream.chat.android.ui.messagepreview.MessagePreviewView

public class MentionsListAdapter : ListAdapter<Message, MessagePreviewViewHolder>(MessageDiffCallback) {

    private var mentionSelectedListener: MentionSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        return StreamItemMentionListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { MessagePreviewViewHolder(it.root) }
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    public fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        this.mentionSelectedListener = mentionSelectedListener
    }

    public inner class MessagePreviewViewHolder(
        private val view: MessagePreviewView
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var message: Message

        init {
            view.setOnClickListener {
                mentionSelectedListener?.onMentionSelected(message)
            }
        }

        internal fun bind(message: Message) {
            this.message = message
            view.setMessage(message)
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
