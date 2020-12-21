package io.getstream.chat.android.ui.options

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Message
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

    internal fun configure(configuration: Configuration) {
        configureMessageOptions(configuration)
    }

    internal data class Configuration(
        val iconsTint: Int,
        val replyText: String,
        val replyIcon: Int,
        val threadReplyText: String,
        val threadReplyIcon: Int,
        val copyText: String,
        val copyIcon: Int,
        val muteText: String,
        val muteIcon: Int,
        val blockText: String,
        val blockIcon: Int,
        val deleteText: String,
        val deleteIcon: Int
    ) : Serializable

    private fun configureMessageOptions(configuration: Configuration) {
        val iconsTint = configuration.iconsTint

        binding.replyTV.configureListItem(configuration.replyText, configuration.replyIcon, iconsTint)
        binding.threadReplyTV.configureListItem(configuration.threadReplyText, configuration.threadReplyIcon, iconsTint)
        binding.copyTV.configureListItem(configuration.copyText, configuration.copyIcon, iconsTint)
        binding.muteTV.configureListItem(configuration.muteText, configuration.muteIcon, iconsTint)
        binding.blockTV.configureListItem(configuration.blockText, configuration.blockIcon, iconsTint)

        binding.deleteTV.run {
            text = configuration.deleteText
            setTextColor(ContextCompat.getColor(context, R.color.stream_ui_light_red))
            setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources, configuration.deleteIcon, null),
                null,
                null,
                null
            )
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

    public fun setDeleteMessageListener(onDelete: () -> Unit) {
        binding.deleteTV.setOnClickListener {
            onDelete()
        }
    }
}
