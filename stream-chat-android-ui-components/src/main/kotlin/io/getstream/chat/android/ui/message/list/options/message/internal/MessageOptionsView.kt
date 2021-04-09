package io.getstream.chat.android.ui.message.list.options.message.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import java.io.Serializable

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

    internal fun configure(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessageTheirs: Boolean,
        syncStatus: SyncStatus,
    ) {
        if (isMessageTheirs) {
            configureTheirsMessage(configuration, style)
        } else {
            configureMineMessage(configuration, style, syncStatus)
        }
    }

    private fun configureTheirsMessage(configuration: Configuration, style: MessageListViewStyle) {
        val iconsTint = style.iconsTint

        configureReply(configuration, style, iconsTint)

        if (configuration.threadsEnabled) {
            binding.threadReplyTV.configureListItem(style.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        configureCopyMessage(iconsTint, configuration, style)

        binding.flagTV.configureListItem(style.flagIcon, iconsTint)
        binding.muteTV.configureListItem(style.muteIcon, iconsTint)
        binding.blockTV.configureListItem(style.blockIcon, iconsTint)
        binding.editTV.isVisible = false
        binding.deleteTV.isVisible = false
    }

    private fun configureMineMessage(
        configuration: Configuration,
        style: MessageListViewStyle,
        syncStatus: SyncStatus,
    ) {
        val iconsTint = style.iconsTint

        configureReply(configuration, style, iconsTint)

        if (configuration.threadsEnabled) {
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

        configureCopyMessage(iconsTint, configuration, style)

        configureEditMessage(configuration, style)
        binding.flagTV.isVisible = false
        binding.muteTV.isVisible = false
        binding.blockTV.isVisible = false
        configureDeleteMessage(configuration, style)
    }

    private fun configureEditMessage(configuration: Configuration, style: MessageListViewStyle) {
        binding.editTV.apply {
            if (configuration.editMessageEnabled) {
                isVisible = true
                configureListItem(style.editIcon, style.iconsTint)
            } else {
                isVisible = false
            }
        }
    }

    private fun configureReply(configuration: Configuration, style: MessageListViewStyle, iconTint: Int) {
        if (configuration.replyEnabled) {
            binding.replyTV.configureListItem(style.replyIcon, iconTint)
        } else {
            binding.replyTV.isVisible = false
        }
    }

    private fun configureCopyMessage(iconsTint: Int, configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.copyTextEnabled) {
            binding.copyTV.isVisible = true
            binding.copyTV.configureListItem(style.copyIcon, iconsTint)
        } else {
            binding.copyTV.isVisible = false
        }
    }

    private fun configureDeleteMessage(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.deleteMessageEnabled) {
            binding.deleteTV.apply {
                isVisible = true
                configureListItem(style.deleteIcon, style.iconsTint)
                setTextColor(ContextCompat.getColor(context, R.color.stream_ui_accent_red))
            }
        } else {
            binding.deleteTV.isVisible = false
        }
    }

    internal data class Configuration(
        val replyEnabled: Boolean,
        val threadsEnabled: Boolean,
        val editMessageEnabled: Boolean,
        val deleteMessageEnabled: Boolean,
        val copyTextEnabled: Boolean,
        val deleteConfirmationEnabled: Boolean,
        val reactionsEnabled: Boolean,
    ) : Serializable {
        internal companion object {
            operator fun invoke(viewStyle: MessageListViewStyle, channelConfig: Config, suppressThreads: Boolean) =
                Configuration(
                    replyEnabled = viewStyle.replyEnabled && channelConfig.isRepliesEnabled,
                    threadsEnabled = if (suppressThreads) false else viewStyle.threadsEnabled && channelConfig.isRepliesEnabled,
                    editMessageEnabled = viewStyle.editMessageEnabled,
                    deleteMessageEnabled = viewStyle.deleteMessageEnabled,
                    copyTextEnabled = viewStyle.copyTextEnabled,
                    deleteConfirmationEnabled = viewStyle.deleteConfirmationEnabled,
                    reactionsEnabled = viewStyle.reactionsEnabled && channelConfig.isReactionsEnabled
                )
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
