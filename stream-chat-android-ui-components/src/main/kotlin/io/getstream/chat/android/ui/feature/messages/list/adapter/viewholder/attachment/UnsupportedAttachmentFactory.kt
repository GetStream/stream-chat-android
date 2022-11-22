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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.databinding.StreamUiUnsupportedAttachmentViewBinding
import io.getstream.chat.android.ui.feature.messages.list.UnsupportedAttachmentViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Fallback factory for unsupported attachment types.
 */
public class UnsupportedAttachmentFactory : AttachmentFactory {

    /**
     * Checks if the message contains unsupported attachments.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @return True it the message contains unsupported attachment.
     */
    override fun canHandle(message: Message): Boolean {
        return message.attachments.isNotEmpty() && message.attachments.all { !it.isSupported() }
    }

    /**
     * Creates fallback UI that represents unsupported attachments.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @param listeners [MessageListListenerContainer] with listeners for the message list.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     * @return An inner ViewHolder with the fallback attachment content view.
     */
    override fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup,
    ): InnerAttachmentViewHolder {
        return object : InnerAttachmentViewHolder(UnsupportedAttachmentsView(parent.context)) {}
    }

    /**
     * View used to display unsupported attachments.
     */
    private class UnsupportedAttachmentsView : FrameLayout {

        private val binding = StreamUiUnsupportedAttachmentViewBinding.inflate(streamThemeInflater, this, true)

        private lateinit var style: UnsupportedAttachmentViewStyle

        constructor(context: Context) : this(context, null, 0)

        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context.createStreamThemeWrapper(),
            attrs,
            defStyleAttr
        ) {
            init(attrs)
        }

        private fun init(attrs: AttributeSet?) {
            style = UnsupportedAttachmentViewStyle(context, attrs)

            val shapeAppearanceModel = ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
                .build()
            binding.attachmentContainer.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
                fillColor = ColorStateList.valueOf(style.backgroundColor)
                strokeColor = ColorStateList.valueOf(style.strokeColor)
                strokeWidth = style.strokeWidth.toFloat()
            }
            binding.titleImageView.setTextStyle(style.titleTextStyle)
        }
    }

    /**
     * Checks if the attachment type is supported.
     *
     * @return True if the attachment type is supported.
     */
    private fun Attachment.isSupported(): Boolean {
        return SUPPORTED_ATTACHMENT_TYPES.contains(type)
    }

    private companion object {
        /**
         * The list of supported attachment types.
         */
        private val SUPPORTED_ATTACHMENT_TYPES: Set<String> = setOf(
            AttachmentType.IMAGE,
            AttachmentType.GIPHY,
            AttachmentType.VIDEO,
            AttachmentType.AUDIO,
            AttachmentType.FILE,
            AttachmentType.LINK,
        )
    }
}
