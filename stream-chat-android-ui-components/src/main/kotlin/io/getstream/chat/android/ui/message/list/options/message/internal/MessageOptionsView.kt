package io.getstream.chat.android.ui.message.list.options.message.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import io.getstream.chat.android.ui.message.list.MessageListViewStyle

internal class MessageOptionsView : FrameLayout {

    private val binding: StreamUiMessageOptionsViewBinding =
        StreamUiMessageOptionsViewBinding.inflate(context.inflater, this, true)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    internal fun configure(style: MessageListViewStyle, isMessageTheirs: Boolean, syncStatus: SyncStatus) {
        if (isMessageTheirs) {
            configureTheirsMessage(style)
        } else {
            configureMineMessage(style, syncStatus)
        }
    }

    private fun configureTheirsMessage(style: MessageListViewStyle) {
        val iconsTint = style.iconsTint

        configureReply(style, iconsTint)

        if (style.threadsEnabled) {
            binding.threadReplyTV.configureListItem(style.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        configureCopyMessage(iconsTint, style)

        binding.flagTV.configureListItem(style.flagIcon, iconsTint)
        binding.muteTV.configureListItem(style.muteIcon, iconsTint)
        binding.blockTV.configureListItem(style.blockIcon, iconsTint)
        binding.editTV.isVisible = false
        binding.deleteTV.isVisible = false
    }

    private fun configureMineMessage(style: MessageListViewStyle, syncStatus: SyncStatus) {
        val iconsTint = style.iconsTint

        configureReply(style, iconsTint)

        if (style.threadsEnabled) {
            binding.threadReplyTV.configureListItem(style.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        when (syncStatus) {
            SyncStatus.FAILED_PERMANENTLY -> {
                binding.retryTV.configureListItem(
                    style.retryIcon,
                    ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
                )

                binding.retryTV.isVisible = true
                binding.threadReplyTV.isVisible = false
            }
            SyncStatus.COMPLETED -> {
                // Empty
            }
            SyncStatus.SYNC_NEEDED, SyncStatus.IN_PROGRESS -> {
                binding.threadReplyTV.isVisible = false
            }
        }

        configureCopyMessage(iconsTint, style)

        configureEditMessage(style)
        binding.flagTV.isVisible = false
        binding.muteTV.isVisible = false
        binding.blockTV.isVisible = false
        configureDeleteMessage(style)
    }

    private fun configureEditMessage(style: MessageListViewStyle) {
        binding.editTV.apply {
            if (style.editMessageEnabled) {
                isVisible = true
                configureListItem(style.editIcon, style.iconsTint)
            } else {
                isVisible = false
            }
        }
    }

    private fun configureReply(style: MessageListViewStyle, iconTint: Int) {
        if (style.replyEnabled) {
            binding.replyTV.configureListItem(style.replyIcon, iconTint)
        } else {
            binding.replyTV.isVisible = false
        }
    }

    private fun configureCopyMessage(iconsTint: Int, style: MessageListViewStyle) {
        if (style.copyTextEnabled) {
            binding.copyTV.isVisible = true
            binding.copyTV.configureListItem(style.copyIcon, iconsTint)
        } else {
            binding.copyTV.isVisible = false
        }
    }

    private fun configureDeleteMessage(style: MessageListViewStyle) {
        if (style.deleteMessageEnabled) {
            binding.deleteTV.apply {
                isVisible = true
                configureListItem(style.deleteIcon, style.iconsTint)
                setTextColor(ContextCompat.getColor(context, R.color.stream_ui_accent_red))
            }
        } else {
            binding.deleteTV.isVisible = false
        }
    }

    fun setReplyListener(onReplyListener: () -> Unit) {
        binding.replyTV.setOnClickListener {
            onReplyListener()
        }
    }

    fun setThreadListener(onThreadReply: () -> Unit) {
        binding.threadReplyTV.setOnClickListener {
            onThreadReply()
        }
    }

    fun setRetryListener(onRetry: () -> Unit) {
        binding.retryTV.setOnClickListener {
            onRetry()
        }
    }

    fun setCopyListener(onCopy: () -> Unit) {
        binding.copyTV.setOnClickListener {
            onCopy()
        }
    }

    fun setEditMessageListener(onEdit: () -> Unit) {
        binding.editTV.setOnClickListener {
            onEdit()
        }
    }

    fun setFlagMessageListener(onFlag: () -> Unit) {
        binding.flagTV.setOnClickListener {
            onFlag()
        }
    }

    fun setDeleteMessageListener(onDelete: () -> Unit) {
        binding.deleteTV.setOnClickListener {
            onDelete()
        }
    }

    fun setMuteUserListener(onMute: () -> Unit) {
        binding.muteTV.setOnClickListener {
            onMute()
        }
    }

    fun setBlockUserListener(onBlock: () -> Unit) {
        binding.blockTV.setOnClickListener {
            onBlock()
        }
    }

    private fun TextView.configureListItem(icon: Int, iconTint: Int) {
        this.setLeftDrawable(icon, iconTint)
    }
}
