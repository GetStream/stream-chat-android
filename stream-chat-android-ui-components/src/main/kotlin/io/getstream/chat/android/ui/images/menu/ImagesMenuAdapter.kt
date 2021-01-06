package io.getstream.chat.android.ui.images.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.ImageLoader.load
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentImageBinding

internal class ImagesMenuAdapter(
    private val clickListener: (Int) -> Unit
) : ListAdapter<String, ImagesMenuAdapter.ImageViewHolder>(diffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return StreamUiItemAttachmentImageBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ImageViewHolder)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            clickListener(position)
        }
    }

    internal class ImageViewHolder(
        private val binding: StreamUiItemAttachmentImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            binding.image.load(imageUrl, R.drawable.stream_placeholder)
        }
    }
}

private fun diffUtil(): DiffUtil.ItemCallback<String> {
    return object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
