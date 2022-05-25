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

package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.message.list.DefaultQuotedAttachmentViewStyle

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
        defStyleAttr
    ) {
        init(context)
    }

    private lateinit var style: DefaultQuotedAttachmentViewStyle
    private lateinit var attachment: Attachment

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        layoutParams = layoutParams.apply {
            when (attachment.type) {
                ModelType.attach_file, ModelType.attach_video -> {
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
            ModelType.attach_file, ModelType.attach_video -> loadAttachmentThumb(attachment)
            ModelType.attach_image -> showAttachmentThumb(attachment.imagePreviewUrl)
            ModelType.attach_giphy -> showAttachmentThumb(attachment.thumbUrl)
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
            transformation = StreamImageLoader.ImageTransformation.RoundedCorners(style.quotedImageRadius.toFloat())
        )
    }

    private fun init(context: Context) {
        this.style = DefaultQuotedAttachmentViewStyle(context)
    }
}
