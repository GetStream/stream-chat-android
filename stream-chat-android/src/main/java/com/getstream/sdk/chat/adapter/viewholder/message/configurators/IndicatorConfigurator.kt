package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.viewholder.message.getActiveContentViewResId
import com.getstream.sdk.chat.adapter.viewholder.message.isDeleted
import com.getstream.sdk.chat.adapter.viewholder.message.isEphemeral
import com.getstream.sdk.chat.adapter.viewholder.message.isFailed
import com.getstream.sdk.chat.adapter.viewholder.message.isInThread
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.extensions.isNotBottomPosition
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.ReadStateStyle
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.exhaustive

internal class IndicatorConfigurator(
    private val binding: StreamItemMessageBinding,
    private val readStateStyle: ReadStateStyle,
    private val readStateClickListener: MessageListView.ReadStateClickListener,
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

        if (message.isDeleted() ||
            message.isFailed() ||
            message.id.isEmpty() ||
            messageItem.isNotBottomPosition() ||
            messageItem.messageReadBy.isNotEmpty() ||
            !messageItem.isMine ||
            message.isInThread() ||
            message.isEphemeral() ||
            !readStateStyle.isDeliveredIndicatorEnabled
        ) {
            return
        }

        when (message.syncStatus) {
            SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED, SyncStatus.WAIT_ATTACHMENTS -> {
                binding.pbDeliver.isVisible = true
                binding.ivDeliver.isVisible = false
            }
            SyncStatus.COMPLETED -> {
                binding.pbDeliver.isVisible = false
                binding.ivDeliver.isVisible = true
            }
            SyncStatus.FAILED_PERMANENTLY -> {
                binding.pbDeliver.isVisible = false
                binding.ivDeliver.isVisible = false
            }
        }.exhaustive

        if (message.text.isEmpty()) {
            binding.itemMessageContainer.updateConstraints {
                clear(binding.ivDeliver.id, ConstraintSet.START)
                clear(binding.ivDeliver.id, ConstraintSet.END)
                connect(
                    binding.ivDeliver.id,
                    ConstraintSet.END,
                    binding.attachmentview.id,
                    ConstraintSet.START,
                )
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
            message.isEphemeral() ||
            !readStateStyle.isReadStateEnabled
        ) {
            binding.readState.isVisible = false
            return
        }

        binding.readState.apply {
            isVisible = true
            setReads(readBy, messageItem.isTheirs, readStateStyle)
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
