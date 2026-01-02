/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.feature.messages.list.DefaultQuotedAttachmentViewStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.load
import io.getstream.chat.android.ui.utils.loadAttachmentThumb

/**
 * View tasked to show the attachments supported by default.
 */
internal class DefaultQuotedAttachmentView : AppCompatImageView {

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(context)
    }

    private lateinit var style: DefaultQuotedAttachmentViewStyle
    private var attachment: Attachment? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        layoutParams = layoutParams.apply {
            when (attachment?.type) {
                AttachmentType.FILE, AttachmentType.VIDEO -> {
                    width = style.fileAttachmentWidth
                    height = style.fileAttachmentHeight
                }

                else -> {
                    width = style.imageAttachmentWidth
                    height = style.imageAttachmentHeight
                }
            }
        }
    }

    /**
     * Show the attachment sent inside the message.
     *
     * @param attachment The attachment we wish to show.
     */
    fun showAttachment(attachment: Attachment) {
        this.attachment = attachment
        when (attachment.type) {
            AttachmentType.FILE, AttachmentType.VIDEO, AttachmentType.AUDIO_RECORDING -> loadAttachmentThumb(attachment)
            AttachmentType.IMAGE -> showAttachmentThumb(
                attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatUI.streamCdnImageResizing),
            )
            AttachmentType.GIPHY -> showAttachmentThumb(attachment.thumbUrl)
            else -> showAttachmentThumb(attachment.image)
        }
    }

    /**
     * Sets the attachment thumbnail in case of images and giphy.
     *
     * @param url The url to the image resource.
     */
    private fun showAttachmentThumb(url: String?) {
        load(
            data = url,
            transformation = StreamImageLoader.ImageTransformation.RoundedCorners(style.quotedImageRadius.toFloat()),
        )
    }

    private fun init(context: Context) {
        this.style = DefaultQuotedAttachmentViewStyle(context)
    }
}
