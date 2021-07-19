package io.getstream.chat.android.ui.message.list.options.message.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawableWithTint
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
    ) {
        if (isMessageTheirs) {
            configureTheirsMessage(
                configuration = configuration,
                style = style,
                isMessageAuthorMuted = isMessageAuthorMuted,
            )
        } else {
            configureMineMessage(configuration = configuration, style = style, syncStatus = syncStatus)
        }
        binding.blockTV.isVisible = false
        binding.messageOptionsContainer.setCardBackgroundColor(style.messageOptionsBackgroundColor)
    }

    private fun configureTheirsMessage(
        configuration: Configuration,
        style: MessageListViewStyle,
        isMessageAuthorMuted: Boolean,
    ) {
        @Suppress("DEPRECATION_ERROR")
        val iconsTint = style.iconsTint
        val textStyle = style.messageOptionsText

        configureReply(configuration, style, iconsTint)

        if (configuration.threadsEnabled) {
            binding.threadReplyTV.configureListItem(textStyle, style.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        configureCopyMessage(iconsTint, configuration, style)

        binding.flagTV.configureListItem(textStyle, style.flagIcon, iconsTint)
        binding.muteTV.configureListItem(textStyle, style.muteIcon, iconsTint)
        binding.blockTV.configureListItem(textStyle, style.blockIcon, iconsTint)
        binding.editTV.isVisible = false
        binding.deleteTV.isVisible = false
        configureBlock(configuration = configuration, style = style, iconTint = iconsTint)
        configureMute(
            configuration = configuration,
            style = style,
            iconTint = iconsTint,
            isMessageAuthorMuted = isMessageAuthorMuted,
        )
        configureFlag(configuration = configuration, style = style, iconTint = iconsTint)
    }

    private fun configureMineMessage(
        configuration: Configuration,
        style: MessageListViewStyle,
        syncStatus: SyncStatus,
    ) {
        @Suppress("DEPRECATION_ERROR")
        val iconsTint = style.iconsTint

        configureReply(configuration, style, iconsTint)

        if (configuration.threadsEnabled) {
            binding.threadReplyTV.configureListItem(style.messageOptionsText, style.threadReplyIcon, iconsTint)
        } else {
            binding.threadReplyTV.isVisible = false
        }

        when (syncStatus) {
            SyncStatus.FAILED_PERMANENTLY -> {
                binding.retryTV.configureListItem(
                    style.messageOptionsText,
                    style.retryIcon,
                    ContextCompat.getColor(context, R.color.stream_ui_accent_blue)
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
                @Suppress("DEPRECATION_ERROR")
                configureListItem(style.messageOptionsText, style.editIcon, style.iconsTint)
            } else {
                isVisible = false
            }
        }
    }

    private fun configureReply(configuration: Configuration, style: MessageListViewStyle, iconTint: Int?) {
        if (configuration.replyEnabled) {
            binding.replyTV.configureListItem(style.messageOptionsText, style.replyIcon, iconTint)
        } else {
            binding.replyTV.isVisible = false
        }
    }

    private fun configureFlag(configuration: Configuration, style: MessageListViewStyle, iconTint: Int?) {
        if (configuration.flagEnabled) {
            binding.flagTV.configureListItem(style.messageOptionsText, style.flagIcon, iconTint)
        } else {
            binding.flagTV.isVisible = false
        }
    }

    private fun configureMute(
        configuration: Configuration,
        style: MessageListViewStyle,
        iconTint: Int?,
        isMessageAuthorMuted: Boolean,
    ) {
        if (configuration.muteEnabled) {
            val icon = if (isMessageAuthorMuted) style.unmuteIcon else style.muteIcon
            binding.muteTV.configureListItem(style.messageOptionsText, icon, iconTint)
            binding.muteTV.setText(if (isMessageAuthorMuted) R.string.stream_ui_message_list_unmute_user else R.string.stream_ui_message_list_mute_user)
        } else {
            binding.muteTV.isVisible = false
        }
    }

    private fun configureBlock(configuration: Configuration, style: MessageListViewStyle, iconTint: Int?) {
        if (configuration.blockEnabled) {
            binding.blockTV.configureListItem(style.messageOptionsText, style.replyIcon, iconTint)
        } else {
            binding.blockTV.isVisible = false
        }
    }

    private fun configureCopyMessage(iconsTint: Int?, configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.copyTextEnabled) {
            binding.copyTV.isVisible = true
            binding.copyTV.configureListItem(style.messageOptionsText, style.copyIcon, iconsTint)
        } else {
            binding.copyTV.isVisible = false
        }
    }

    @Suppress("DEPRECATION_ERROR")
    private fun configureDeleteMessage(configuration: Configuration, style: MessageListViewStyle) {
        if (configuration.deleteMessageEnabled) {
            binding.deleteTV.apply {
                isVisible = true
                configureListItem(style.warningMessageOptionsText, style.deleteIcon, style.warningActionsTintColor)
                if (style.warningActionsTintColor != null) {
                    setTextColor(style.warningActionsTintColor)
                }
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

    private fun TextView.configureListItem(textStyle: TextStyle, icon: Int, iconTint: Int?) {
        if (iconTint != null) {
            this.setLeftDrawableWithTint(icon, iconTint)
        } else {
            setLeftDrawable(icon)
        }
        textStyle.apply(this)
    }
}
