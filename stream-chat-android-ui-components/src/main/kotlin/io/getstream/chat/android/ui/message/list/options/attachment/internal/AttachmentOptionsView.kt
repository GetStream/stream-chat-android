/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.databinding.StreamUiAttachmentOptionsViewBinding
import java.io.Serializable

internal class AttachmentOptionsView : FrameLayout {

    private val binding = StreamUiAttachmentOptionsViewBinding.inflate(streamThemeInflater, this, true)

    var isDeleteEnabled: Boolean = false
        private set

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
        context.obtainStyledAttributes(
            attrs,
            R.styleable.AttachmentOptionsView,
            R.attr.streamUiAttachmentGalleryOptionsStyle,
            R.style.StreamUi_AttachmentGallery_Options
        ).use { array ->
            readConfiguration(array).run {
                if (replyEnabled) {
                    binding.reply.setStartDrawable(replyIcon)
                } else {
                    binding.reply.isVisible = false
                }

                if (showInChatEnabled) {
                    binding.showInChat.setStartDrawable(showInChatIcon)
                } else {
                    binding.showInChat.isVisible = false
                }

                if (saveImageEnabled) {
                    binding.saveImage.setStartDrawable(saveImageIcon)
                } else {
                    binding.saveImage.isVisible = false
                }

                isDeleteEnabled = deleteEnabled
                if (deleteEnabled) {
                    binding.delete.configureListItem(deleteIcon, deleteTextTint)
                } else {
                    binding.delete.isVisible = false
                }
            }
        }
    }

    fun setDeleteItemVisiblity(visible: Boolean) {
        binding.delete.isVisible = visible
    }

    private fun readConfiguration(array: TypedArray): Configuration {

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

        val deleteTextTint = array.getColor(
            R.styleable.AttachmentOptionsView_streamUiDeleteTextTint,
            ContextCompat.getColor(context, R.color.stream_ui_accent_red)
        )

        val replyEnabled = array.getBoolean(R.styleable.AttachmentOptionsView_streamUiAttachmentReplyEnabled, true)
        val showInChatEnabled = array.getBoolean(R.styleable.AttachmentOptionsView_streamUiShowInChatEnabled, true)
        val saveImageEnabled = array.getBoolean(R.styleable.AttachmentOptionsView_streamUiSaveImageEnabled, true)
        val deleteEnabled = array.getBoolean(R.styleable.AttachmentOptionsView_streamUiDeleteEnabled, true)

        return Configuration(
            replyIcon = replyIcon,
            showInChatIcon = showInChatIcon,
            saveImageIcon = saveImageIcon,
            deleteIcon = deleteIcon,
            deleteTextTint = deleteTextTint,
            replyEnabled = replyEnabled,
            showInChatEnabled = showInChatEnabled,
            saveImageEnabled = saveImageEnabled,
            deleteEnabled = deleteEnabled
        )
    }

    internal data class Configuration(
        val replyIcon: Int,
        val showInChatIcon: Int,
        val saveImageIcon: Int,
        val deleteIcon: Int,
        val deleteTextTint: Int,
        val replyEnabled: Boolean,
        val showInChatEnabled: Boolean,
        val saveImageEnabled: Boolean,
        val deleteEnabled: Boolean,
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

    private fun TextView.configureListItem(icon: Int, textTint: Int) {
        this.setTextColor(textTint)
        setStartDrawable(icon)
    }
}
