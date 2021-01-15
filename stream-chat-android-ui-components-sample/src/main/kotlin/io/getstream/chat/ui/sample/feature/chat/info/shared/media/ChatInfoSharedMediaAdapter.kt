package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedMediaItemBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment

class ChatInfoSharedMediaAdapter :
    ListAdapter<SharedAttachment.AttachmentItem, ChatInfoSharedMediaAdapter.MediaViewHolder>(
        object : DiffUtil.ItemCallback<SharedAttachment.AttachmentItem>() {
            override fun areItemsTheSame(
                oldItem: SharedAttachment.AttachmentItem,
                newItem: SharedAttachment.AttachmentItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SharedAttachment.AttachmentItem,
                newItem: SharedAttachment.AttachmentItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    private var mediaClickListener: MediaClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        return ChatInfoSharedMediaItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::MediaViewHolder)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setMediaClickListener(listener: MediaClickListener?) {
        mediaClickListener = listener
    }

    inner class MediaViewHolder(private val binding: ChatInfoSharedMediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.mediaContainer.setOnClickListener {
                mediaClickListener?.onClick()
            }
        }

        fun bind(item: SharedAttachment.AttachmentItem) {
            binding.mediaImageView.load(
                item.attachment.url ?: item.attachment.imageUrl
            ) {
                placeholder(R.drawable.stream_placeholder)
                error(R.drawable.stream_placeholder)
            }
        }
    }

    fun interface MediaClickListener {
        fun onClick()
    }
}
