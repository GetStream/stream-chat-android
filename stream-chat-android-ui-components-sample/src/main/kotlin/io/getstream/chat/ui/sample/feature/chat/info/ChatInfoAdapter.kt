package io.getstream.chat.ui.sample.feature.chat.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.ui.sample.databinding.ChatInfoMemberItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoOptionItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoStatefulOptionItemBinding

class ChatInfoAdapter : ListAdapter<ChatInfoItem, BaseViewHolder<*>>(
    object : DiffUtil.ItemCallback<ChatInfoItem>() {
        override fun areItemsTheSame(oldItem: ChatInfoItem, newItem: ChatInfoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatInfoItem, newItem: ChatInfoItem): Boolean {
            return oldItem == newItem
        }
    }
) {

    private var chatInfoOptionClickListener: ChatInfoOptionClickListener? = null
    private var chatInfoStatefulOptionChangedListener: ChatInfoStatefulOptionChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_MEMBER_ITEM ->
                ChatInfoMemberItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let(::ChatInfoMemberViewHolder)
            TYPE_OPTION ->
                ChatInfoOptionItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let { ChatInfoOptionViewHolder(it, chatInfoOptionClickListener) }
            TYPE_STATEFUL_OPTION ->
                ChatInfoStatefulOptionItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let { ChatInfoStatefulOptionViewHolder(it, chatInfoStatefulOptionChangedListener) }
            TYPE_SEPARATOR ->
                ChatInfoSeparatorItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let(::ChatInfoSeparatorViewHolder)
            else -> throw IllegalArgumentException("Unhandled chat info view type ($viewType)")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        holder.bindListItem(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatInfoItem.MemberItem -> TYPE_MEMBER_ITEM
            ChatInfoItem.Separator -> TYPE_SEPARATOR
            ChatInfoItem.Option.SharedMedia,
            ChatInfoItem.Option.SharedFiles,
            ChatInfoItem.Option.SharedGroups,
            ChatInfoItem.Option.LeaveChannel -> TYPE_OPTION
            is ChatInfoItem.Option.Stateful -> TYPE_STATEFUL_OPTION
        }
    }

    fun setChatInfoOptionClickListener(listener: ChatInfoOptionClickListener?) {
        chatInfoOptionClickListener = listener
    }

    fun setChatInfoStatefulOptionChangedListener(listener: ChatInfoStatefulOptionChangedListener?) {
        chatInfoStatefulOptionChangedListener = listener
    }

    companion object {
        private const val TYPE_MEMBER_ITEM = 0
        private const val TYPE_OPTION = 1
        private const val TYPE_STATEFUL_OPTION = 2
        private const val TYPE_SEPARATOR = 3
    }

    fun interface ChatInfoOptionClickListener {
        fun onClick(option: ChatInfoItem.Option)
    }

    fun interface ChatInfoStatefulOptionChangedListener {
        fun onClick(option: ChatInfoItem.Option.Stateful, isChecked: Boolean)
    }
}
