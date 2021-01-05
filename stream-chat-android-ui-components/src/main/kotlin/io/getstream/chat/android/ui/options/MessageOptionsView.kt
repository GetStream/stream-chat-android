package io.getstream.chat.android.ui.options

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import java.io.Serializable

public class MessageOptionsView : FrameLayout {

    private val binding: StreamUiMessageOptionsViewBinding =
        StreamUiMessageOptionsViewBinding.inflate(context.inflater, this, true)

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    internal fun configure(configuration: Configuration, isMessageTheirs: Boolean, syncStatus: SyncStatus) {
        if (isMessageTheirs) {
            configureTheirsMessage(configuration, syncStatus)
        } else {
            configureMineMessage(configuration, syncStatus)
        }
    }

    private fun configureTheirsMessage(configuration: Configuration, syncStatus: SyncStatus) {
        val iconsTint = configuration.iconsTint

        // Uncomment when the feature will be deployed to our backend
        // binding.replyTV.isVisible = true
        binding.replyTV.configureListItem(configuration.replyText, configuration.replyIcon, iconsTint)

        if (configuration.threadEnabled) {
            binding.threadReplyTV.configureListItem(
                configuration.threadReplyText,
                configuration.threadReplyIcon,
                iconsTint
            )
        } else {
            binding.threadReplyTV.isVisible = false
        }

        configureCopyMessage(iconsTint, configuration)

        binding.flagTV.configureListItem(configuration.flagText, configuration.flagIcon, iconsTint)
        binding.muteTV.configureListItem(configuration.muteText, configuration.muteIcon, iconsTint)
        binding.blockTV.configureListItem(configuration.blockText, configuration.blockIcon, iconsTint)
        binding.deleteTV.isVisible = false
    }

    private fun configureMineMessage(configuration: Configuration, syncStatus: SyncStatus) {
        val iconsTint = configuration.iconsTint

        // Uncomment when the feature will be deployed to our backend
        // binding.replyTV.isVisible = true
        binding.replyTV.configureListItem(configuration.replyText, configuration.replyIcon, iconsTint)

        if (configuration.threadEnabled) {
            binding.threadReplyTV.configureListItem(
                configuration.threadReplyText,
                configuration.threadReplyIcon,
                iconsTint
            )
        } else {
            binding.threadReplyTV.isVisible = false
        }

        when (syncStatus) {
            SyncStatus.FAILED_PERMANENTLY -> {
                binding.retryTV.configureListItem(
                    configuration.retryText,
                    configuration.retryIcon,
                    ContextCompat.getColor(context, R.color.stream_ui_blue)
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

        configureCopyMessage(iconsTint, configuration)

        binding.editTV.configureListItem(configuration.editText, configuration.editIcon, iconsTint)
        binding.flagTV.isVisible = false
        binding.muteTV.isVisible = false
        binding.blockTV.isVisible = false
        binding.deleteTV.run {
            configureListItem(configuration.deleteText, configuration.deleteIcon, iconsTint)
            setTextColor(ContextCompat.getColor(context, R.color.stream_ui_light_red))
        }
    }

    private fun configureCopyMessage(iconsTint: Int, configuration: Configuration) {
        if (configuration.copyTextEnabled) {
            binding.copyTV.isVisible = true
            binding.copyTV.configureListItem(configuration.copyText, configuration.copyIcon, iconsTint)
        } else {
            binding.copyTV.isVisible = false
        }
    }

    internal data class Configuration(
        val iconsTint: Int,
        val replyText: String,
        val replyIcon: Int,
        val threadReplyText: String,
        val threadReplyIcon: Int,
        val threadEnabled: Boolean = true,
        val retryText: String,
        val retryIcon: Int,
        val copyText: String,
        val copyIcon: Int,
        val editText: String,
        val editIcon: Int,
        val flagText: String,
        val flagIcon: Int,
        val muteText: String,
        val muteIcon: Int,
        val blockText: String,
        val blockIcon: Int,
        val deleteText: String,
        val deleteIcon: Int,
        val copyTextEnabled: Boolean,
        val deleteConfirmationEnabled: Boolean,
        val deleteConfirmationTitle: String,
        val deleteConfirmationMessage: String,
        val deleteConfirmationPositiveButton: String,
        val deleteConfirmationNegativeButton: String,
    ) : Serializable

    public fun setReplyListener(onReplyListener: () -> Unit) {
        binding.replyTV.setOnClickListener {
            onReplyListener()
        }
    }

    public fun setThreadListener(onThreadReply: () -> Unit) {
        binding.threadReplyTV.setOnClickListener {
            onThreadReply()
        }
    }

    public fun setRetryListener(onRetry: () -> Unit) {
        binding.retryTV.setOnClickListener {
            onRetry()
        }
    }

    public fun setCopyListener(onCopy: () -> Unit) {
        binding.copyTV.setOnClickListener {
            onCopy()
        }
    }

    public fun setEditMessageListener(onEdit: () -> Unit) {
        binding.editTV.setOnClickListener {
            onEdit()
        }
    }

    public fun setFlagMessageListener(onFlag: () -> Unit) {
        binding.flagTV.setOnClickListener {
            onFlag()
        }
    }

    public fun setDeleteMessageListener(onDelete: () -> Unit) {
        binding.deleteTV.setOnClickListener {
            onDelete()
        }
    }

    public fun setMuteUserListener(onMute: () -> Unit) {
        binding.muteTV.setOnClickListener {
            onMute()
        }
    }

    public fun setBlockUserListener(onBlock: () -> Unit) {
        binding.blockTV.setOnClickListener {
            onBlock()
        }
    }

    private fun TextView.configureListItem(text: String, icon: Int, iconTint: Int) {
        this.text = text
        this.setLeftDrawable(icon, iconTint)
    }

    private fun TextView.setLeftDrawable(icon: Int, iconTint: Int) {
        setCompoundDrawablesWithIntrinsicBounds(
            ResourcesCompat.getDrawable(resources, icon, null)?.apply { setTint(iconTint) },
            null,
            null,
            null
        )
    }
}
