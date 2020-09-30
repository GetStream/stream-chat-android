package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import androidx.core.view.isVisible
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory.Position.TOP
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding

internal class SpaceConfigurator(
    private val binding: StreamItemMessageBinding
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        if (TOP in messageItem.positions) {
            // TOP
            binding.spaceHeader.isVisible = true
            binding.spaceSameUser.isVisible = false
        } else {
            binding.spaceHeader.isVisible = false
            binding.spaceSameUser.isVisible = true
        }
        // Attach Gap
        binding.spaceAttachment.isVisible = binding.attachmentview.isVisible

        if (binding.attachmentview.isVisible && messageItem.message.text.isEmpty()) {
            binding.spaceAttachment.isVisible = false
        }

        // Reaction Gap
        binding.spaceReaction.isVisible = binding.reactionsRecyclerView.isVisible

        // ONLY_FOR_DEBUG
        if (false) {
            binding.spaceHeader.setBackgroundResource(R.color.stream_gap_header)
            binding.spaceSameUser.setBackgroundResource(R.color.stream_gap_message)
            binding.spaceAttachment.setBackgroundResource(R.color.stream_gap_attach)
            binding.spaceReaction.setBackgroundResource(R.color.stream_gap_reaction)
        }
    }
}
