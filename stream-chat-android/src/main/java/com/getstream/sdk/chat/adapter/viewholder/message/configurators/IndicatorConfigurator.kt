package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.Position.BOTTOM
import com.getstream.sdk.chat.adapter.updateConstraints
import com.getstream.sdk.chat.adapter.viewholder.message.getActiveContentViewResId
import com.getstream.sdk.chat.adapter.viewholder.message.isDeleted
import com.getstream.sdk.chat.adapter.viewholder.message.isEphemeral
import com.getstream.sdk.chat.adapter.viewholder.message.isFailed
import com.getstream.sdk.chat.adapter.viewholder.message.isInThread
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.utils.SyncStatus

internal class IndicatorConfigurator(
    private val binding: StreamItemMessageBinding,
    private val style: MessageListViewStyle,
    private val channel: Channel,
    private val readStateClickListener: MessageListView.ReadStateClickListener
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configDeliveredIndicator(messageItem)
        configReadIndicator(messageItem)
        configParamsReadIndicator(messageItem)
    }

    private fun configDeliveredIndicator(messageItem: MessageItem) {
        binding.ivDeliver.isVisible = false
        binding.pbDeliver.isVisible = false

        val message = messageItem.message
        val lastMessage = LlcMigrationUtils.computeLastMessage(channel)
        val messageDate = message.createdAt ?: message.createdLocallyAt
        val lastMessageDate = lastMessage?.createdAt ?: lastMessage?.createdLocallyAt

        if (message.isDeleted() ||
            message.isFailed() ||
            lastMessage == null ||
            message.id.isEmpty() ||
            BOTTOM !in messageItem.positions ||
            messageItem.messageReadBy.isNotEmpty() ||
            !messageItem.isMine ||
            messageDate?.time ?: 0 < lastMessageDate?.time ?: 0 ||
            message.type == ModelType.message_ephemeral ||
            message.isInThread() ||
            message.isEphemeral()
        ) {
            return
        }

        when (message.syncStatus) {
            SyncStatus.IN_PROGRESS -> {
                binding.pbDeliver.isVisible = true
                binding.ivDeliver.isVisible = false
            }
            SyncStatus.COMPLETED -> {
                binding.pbDeliver.isVisible = false
                binding.ivDeliver.isVisible = true
            }
            SyncStatus.SYNC_NEEDED, SyncStatus.FAILED_PERMANENTLY -> {
                binding.pbDeliver.isVisible = false
                binding.ivDeliver.isVisible = false
            }
        }
    }

    private fun configReadIndicator(messageItem: MessageItem) {
        val readBy: List<ChannelUserRead> = messageItem.messageReadBy
        val message = messageItem.message

        if (message.isDeleted() ||
            message.isFailed() ||
            readBy.isEmpty() ||
            message.isInThread() ||
            message.isEphemeral()
        ) {
            binding.readState.isVisible = false
            return
        }

        binding.readState.apply {
            isVisible = true
            setReads(readBy, messageItem.isTheirs, style)
            setOnClickListener {
                readStateClickListener.onReadStateClick(readBy)
            }
        }
    }

    internal fun configParamsReadIndicator(messageItem: MessageItem) {
        if (binding.readState.isGone) {
            return
        }

        binding.root.updateConstraints {
            clear(R.id.read_state, ConstraintSet.START)
            clear(R.id.read_state, ConstraintSet.END)
            clear(R.id.read_state, ConstraintSet.BOTTOM)
        }

        binding.readState.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val activeContentViewResId = getActiveContentViewResId(messageItem.message, binding)

            if (messageItem.isMine) {
                endToStart = activeContentViewResId
            } else {
                startToEnd = activeContentViewResId
            }

            bottomToBottom = activeContentViewResId
            leftMargin = Utils.dpToPx(8)
            rightMargin = Utils.dpToPx(8)
        }
    }
}
