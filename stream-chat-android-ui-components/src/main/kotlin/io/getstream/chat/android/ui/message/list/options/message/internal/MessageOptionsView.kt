package io.getstream.chat.android.ui.message.list.options.message.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import java.io.Serializable

internal class MessageOptionsView : FrameLayout {

    private val binding = StreamUiMessageOptionsViewBinding.inflate(streamThemeInflater, this, true)

    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    )

    internal fun configure(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessageTheirs: Boolean,
        syncStatus: SyncStatus,
        isMessageAuthorMuted: Boolean,
        isMessagePinned: Boolean,
    ) {
        if (isMessageTheirs) {
            configureTheirsMessage(
                configuration = configuration,
                style = style,
                isMessageAuthorMuted = isMessageAuthorMuted,
                isMessagePinned = isMessagePinned,
            )
        } else {
            configureMineMessage(
                configuration = configuration,
                style = style,
                syncStatus = syncStatus,
                isMessagePinned = isMessagePinned,
            )
        }
        binding.blockTV.isVisible = false
        binding.messageOptionsContainer.setCardBackgroundColor(style.messageOptionsBackgroundColor)
    }

    private fun configureTheirsMessage(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessageAuthorMuted: Boolean,
        isMessagePinned: Boolean,
    ) {
        val textStyle = style.messageOptionsText

        configureReply(configuration, style)

        if (configuration.threadsEnabled) {
            binding.threadReplyTV.configureListItem(textStyle, style.threadReplyIcon)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        configureCopyMessage(configuration, style)

        binding.flagTV.configureListItem(textStyle, style.flagIcon)
        binding.muteTV.configureListItem(textStyle, style.muteIcon)
        binding.blockTV.configureListItem(textStyle, style.blockIcon)
        binding.editTV.isVisible = false
        binding.deleteTV.isVisible = false
        configureBlock(configuration = configuration, style = style)
        configureMute(
            configuration = configuration,
            style = style,
            isMessageAuthorMuted = isMessageAuthorMuted,
        )
        configureFlag(configuration = configuration, style = style)
        configurePin(
            configuration = configuration,
            style = style,
            isMessagePinned = isMessagePinned
        )
    }

    private fun configureMineMessage(
        configuration: Configuration,
        style: MessageListViewStyle,
        syncStatus: SyncStatus,
        isMessagePinned: Boolean,
    ) {

        configureReply(configuration, style)

        if (configuration.threadsEnabled) {
            binding.threadReplyTV.configureListItem(style.messageOptionsText, style.threadReplyIcon)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        when (syncStatus) {
            SyncStatus.FAILED_PERMANENTLY -> {
                binding.retryTV.configureListItem(
                    style.messageOptionsText,
                    style.retryIcon,
                )

                binding.retryTV.isVisible = true
                binding.threadReplyTV.isVisible = false
            }
            SyncStatus.COMPLETED -> {
                // Empty
            }
            SyncStatus.SYNC_NEEDED, SyncStatus.IN_PROGRESS, SyncStatus.AWAITING_ATTACHMENTS -> {
                binding.threadReplyTV.isVisible = false
            }
        }.exhaustive

        configureCopyMessage(configuration, style)

        configureEditMessage(configuration, style)
        binding.flagTV.isVisible = false
        binding.muteTV.isVisible = false
        binding.blockTV.isVisible = false
        configureDeleteMessage(configuration, style)
        configurePin(
            configuration = configuration,
            style = style,
            isMessagePinned = isMessagePinned
        )
    }

    private fun configureEditMessage(configuration: Configuration, style: MessageListViewStyle) {
        binding.editTV.apply {
            if (configuration.editMessageEnabled) {
                isVisible = true
                configureListItem(style.messageOptionsText, style.editIcon)
            } else {
                isVisible = false
            }
        }
    }

    private fun configureReply(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.replyEnabled) {
            binding.replyTV.configureListItem(style.messageOptionsText, style.replyIcon)
        } else {
            binding.replyTV.isVisible = false
        }
    }

    private fun configureFlag(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.flagEnabled) {
            binding.flagTV.configureListItem(style.messageOptionsText, style.flagIcon)
        } else {
            binding.flagTV.isVisible = false
        }
    }

    private fun configurePin(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessagePinned: Boolean,
    ) {
        if (configuration.pinMessageEnabled) {
            if (isMessagePinned) {
                binding.pinTV.text = context.getString(R.string.stream_ui_message_list_unpin_message)
                binding.pinTV.configureListItem(style.messageOptionsText, style.unpinIcon)
            } else {
                binding.pinTV.text = context.getString(R.string.stream_ui_message_list_pin_message)
                binding.pinTV.configureListItem(style.messageOptionsText, style.pinIcon)
            }
        } else {
            binding.pinTV.isVisible = false
        }
    }

    private fun configureMute(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessageAuthorMuted: Boolean,
    ) {
        if (configuration.muteEnabled) {
            val icon = if (isMessageAuthorMuted) style.unmuteIcon else style.muteIcon
            binding.muteTV.configureListItem(style.messageOptionsText, icon)
            binding.muteTV.setText(if (isMessageAuthorMuted) R.string.stream_ui_message_list_unmute_user else R.string.stream_ui_message_list_mute_user)
        } else {
            binding.muteTV.isVisible = false
        }
    }

    private fun configureBlock(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.blockEnabled) {
            binding.blockTV.configureListItem(style.messageOptionsText, style.replyIcon)
        } else {
            binding.blockTV.isVisible = false
        }
    }

    private fun configureCopyMessage(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.copyTextEnabled) {
            binding.copyTV.isVisible = true
            binding.copyTV.configureListItem(style.messageOptionsText, style.copyIcon)
        } else {
            binding.copyTV.isVisible = false
        }
    }

    @Suppress("DEPRECATION_ERROR")
    private fun configureDeleteMessage(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.deleteMessageEnabled) {
            binding.deleteTV.apply {
                isVisible = true
                configureListItem(style.warningMessageOptionsText, style.deleteIcon)
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
        val flagEnabled: Boolean,
        val pinMessageEnabled: Boolean,
        val muteEnabled: Boolean,
        val blockEnabled: Boolean,
    ) : Serializable {
        internal companion object {
            operator fun invoke(
                viewStyle: MessageListViewStyle,
                channelConfig: Config,
                hasTextToCopy: Boolean,
                suppressThreads: Boolean,
            ) =
                Configuration(
                    replyEnabled = viewStyle.replyEnabled && channelConfig.isRepliesEnabled,
                    threadsEnabled = if (suppressThreads) false else viewStyle.threadsEnabled && channelConfig.isRepliesEnabled,
                    editMessageEnabled = viewStyle.editMessageEnabled,
                    deleteMessageEnabled = viewStyle.deleteMessageEnabled,
                    copyTextEnabled = viewStyle.copyTextEnabled && hasTextToCopy,
                    deleteConfirmationEnabled = viewStyle.deleteConfirmationEnabled,
                    reactionsEnabled = viewStyle.reactionsEnabled && channelConfig.isReactionsEnabled,
                    flagEnabled = viewStyle.flagEnabled,
                    pinMessageEnabled = viewStyle.pinMessageEnabled,
                    muteEnabled = viewStyle.muteEnabled,
                    blockEnabled = viewStyle.blockEnabled,
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

    fun setPinMessageListener(onPin: () -> Unit) {
        binding.pinTV.setOnClickListener {
            onPin()
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

    private fun TextView.configureListItem(textStyle: TextStyle, icon: Int) {
        setLeftDrawable(icon)
        textStyle.apply(this)
    }
}
