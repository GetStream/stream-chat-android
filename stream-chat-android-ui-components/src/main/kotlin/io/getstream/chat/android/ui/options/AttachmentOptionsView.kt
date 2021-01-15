package io.getstream.chat.android.ui.options

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiAttachmentOptionsViewBinding
import io.getstream.chat.android.ui.utils.extensions.setLeftDrawable
import java.io.Serializable

internal class AttachmentOptionsView : FrameLayout {

    private val binding: StreamUiAttachmentOptionsViewBinding =
        StreamUiAttachmentOptionsViewBinding.inflate(context.inflater, this, true)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context,
        attrs,
        defStyleAttr,
        defStyleRes)

    public fun setReplyClickListener(listener: ReplyClickListener) {
        binding.reply.setOnClickListener { listener.onClick() }
    }

    public fun setDeleteClickListener(listener: DeleteClickListener) {
        binding.delete.setOnClickListener { listener.onClick() }
    }

    public fun setShowInChatClickListener(listener: ShowInChatClickListener) {
        binding.showInChat.setOnClickListener { listener.onClick() }
    }

    public fun setSaveImageClickListener(listener: SaveImageClickListener) {
        binding.saveImage.setOnClickListener { listener.onClick() }
    }

    internal fun configure(config: Configuration) = config.apply {
        binding.reply.configureListItem(replyText, replyIcon, iconsDefaultTint)
        binding.showInChat.configureListItem(showInChatText, showInChatIcon, iconsDefaultTint)
        binding.saveImage.configureListItem(saveImageText, saveImageIcon, iconsDefaultTint)
        binding.delete.configureListItem(deleteText, deleteIcon, deleteIconTint)
    }

    internal data class Configuration(
        val iconsDefaultTint: Int,
        val replyText: String,
        val replyIcon: Int,
        val showInChatIcon: Int,
        val showInChatText: String,
        val saveImageIcon: Int,
        val saveImageText: String,
        val deleteIcon: Int,
        val deleteIconTint: Int,
        val deleteText: String,
        ) : Serializable

    interface ReplyClickListener { fun onClick() }
    interface DeleteClickListener { fun onClick() }
    interface ShowInChatClickListener { fun onClick() }
    interface SaveImageClickListener { fun onClick() }

    private fun TextView.configureListItem(text: String, icon: Int, iconTint: Int) {
        this.text = text
        this.setLeftDrawable(icon, iconTint)
    }

}
