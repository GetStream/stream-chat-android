package io.getstream.chat.android.ui.gallery.overview

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMediaAttachmentBinding

internal class MediaAttachmentAdapter(
    private val showUserAvatars: Boolean,
    private val mediaAttachmentClickListener: MediaAttachmentClickListener,
) : ListAdapter<UserMediaAttachment, MediaAttachmentAdapter.MediaAttachmentViewHolder>(UserMediaAttachmentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaAttachmentViewHolder {
        return StreamUiItemMediaAttachmentBinding
            .inflate(parent.context.inflater, parent, false)
            .let { MediaAttachmentViewHolder(it, showUserAvatars, mediaAttachmentClickListener) }
    }

    override fun onBindViewHolder(holder: MediaAttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaAttachmentViewHolder(
        private val binding: StreamUiItemMediaAttachmentBinding,
        private val showUserAvatars: Boolean,
        private val mediaAttachmentClickListener: MediaAttachmentClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.mediaContainer.setOnClickListener {
                mediaAttachmentClickListener?.onMediaAttachmentClick(bindingAdapterPosition)
            }
        }

        fun bind(userMediaAttachment: UserMediaAttachment) {
            binding.mediaImageView.load(
                data = userMediaAttachment.imageUrl,
                placeholderResId = R.drawable.stream_placeholder
            )

            val user = userMediaAttachment.user
            if (user != null && showUserAvatars) {
                binding.avatarView.isVisible = true
                binding.avatarView.setUserData(user)
            } else {
                binding.avatarView.isVisible = false
            }
        }
    }

    internal fun interface MediaAttachmentClickListener {
        fun onMediaAttachmentClick(position: Int)
    }

    private object UserMediaAttachmentDiffCallback : DiffUtil.ItemCallback<UserMediaAttachment>() {
        override fun areItemsTheSame(oldItem: UserMediaAttachment, newItem: UserMediaAttachment): Boolean {
            return oldItem.imageUrl == newItem.imageUrl &&
                oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(oldItem: UserMediaAttachment, newItem: UserMediaAttachment): Boolean {
            return oldItem == newItem
        }
    }
}
