package io.getstream.chat.android.ui.message.list.options.attachment.internal

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawableWithTint
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.databinding.StreamUiAttachmentOptionsViewBinding
import java.io.Serializable

internal class AttachmentOptionsView : FrameLayout {

    private val binding = StreamUiAttachmentOptionsViewBinding.inflate(streamThemeInflater, this, true)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs)
    }

    fun setReplyClickListener(listener: ReplyClickListener) {
        binding.reply.setOnClickListener { listener.onClick() }
    }

    fun setDeleteClickListener(listener: DeleteClickListener) {
        binding.delete.setOnClickListener { listener.onClick() }
    }

    fun setShowInChatClickListener(listener: ShowInChatClickListener) {
        binding.showInChat.setOnClickListener { listener.onClick() }
    }

    fun setSaveImageClickListener(listener: SaveImageClickListener) {
        binding.saveImage.setOnClickListener { listener.onClick() }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.AttachmentOptionsView).use { array ->
            readConfiguration(array).run {
                binding.reply.configureListItem(replyIcon, iconsDefaultTint)
                binding.showInChat.configureListItem(showInChatIcon, iconsDefaultTint)
                binding.saveImage.configureListItem(saveImageIcon, iconsDefaultTint)
                binding.delete.configureListItem(deleteIcon, deleteIconTint, deleteTextTint)
            }
        }
    }

    fun setDeleteItemVisiblity(visible: Boolean) {
        binding.delete.isVisible = visible
    }

    private fun readConfiguration(array: TypedArray): Configuration {
        val iconsTint = array.getColorOrNull(R.styleable.AttachmentOptionsView_streamUiIconsDefaultTint)

        val replyIcon = array.getResourceId(
            R.styleable.AttachmentOptionsView_streamUiReplyIcon,
            R.drawable.stream_ui_ic_arrow_curve_left_grey,
        )

        val showInChatIcon = array.getResourceId(
            R.styleable.AttachmentOptionsView_streamUiShowInChatIcon,
            R.drawable.stream_ui_ic_show_in_chat,
        )

        val saveImageIcon = array.getResourceId(
            R.styleable.AttachmentOptionsView_streamUiSaveImageIcon,
            R.drawable.stream_ui_ic_download,
        )

        val deleteIcon = array.getResourceId(
            R.styleable.AttachmentOptionsView_streamUiDeleteIcon,
            R.drawable.stream_ui_ic_delete,
        )
        val deleteIconTint = array.getColorOrNull(R.styleable.AttachmentOptionsView_streamUiDeleteIconTint)

        val deleteTextTint = array.getColor(
            R.styleable.AttachmentOptionsView_streamUiDeleteTextTint,
            ContextCompat.getColor(context, R.color.stream_ui_accent_red)
        )

        return Configuration(
            iconsDefaultTint = iconsTint,
            replyIcon = replyIcon,
            showInChatIcon = showInChatIcon,
            saveImageIcon = saveImageIcon,
            deleteIcon = deleteIcon,
            deleteIconTint = deleteIconTint,
            deleteTextTint = deleteTextTint
        )
    }

    internal data class Configuration(
        val iconsDefaultTint: Int?,
        val replyIcon: Int,
        val showInChatIcon: Int,
        val saveImageIcon: Int,
        val deleteIcon: Int,
        val deleteIconTint: Int?,
        val deleteTextTint: Int,
    ) : Serializable

    interface ReplyClickListener {
        fun onClick()
    }

    interface DeleteClickListener {
        fun onClick()
    }

    interface ShowInChatClickListener {
        fun onClick()
    }

    interface SaveImageClickListener {
        fun onClick()
    }

    private fun TextView.configureListItem(icon: Int, iconTint: Int?) {
        if (iconTint != null) {
            this.setLeftDrawableWithTint(icon, iconTint)
        } else {
            setLeftDrawable(icon)
        }
    }

    private fun TextView.configureListItem(icon: Int, iconTint: Int?, textTint: Int) {
        this.setTextColor(textTint)
        configureListItem(icon, iconTint)
    }
}
