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

package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment

/**
 * Style for [AttachmentSelectionDialogFragment].
 *
 * @param imageAttachmentsTabIconDrawable The icon for the image attachments tab.
 * @param fileAttachmentsTabIconDrawable The icon for the file attachments tab.
 * @param cameraAttachmentsTabIconDrawable The icon for the camera attachments tab.
 */
public data class AttachmentsPickerDialogStyle(
    val imageAttachmentsTabIconDrawable: Drawable,
    val fileAttachmentsTabIconDrawable: Drawable,
    val cameraAttachmentsTabIconDrawable: Drawable,
) {
    public companion object {
        internal operator fun invoke(context: Context, attrs: AttributeSet?): AttachmentsPickerDialogStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AttachmentsPickerDialog,
                R.attr.streamUiAttachmentsPickerDialogStyle,
                R.style.StreamUi_AttachmentsPickerDialog,
            ).use { a ->
                val imageAttachmentsTabIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerImageAttachmentsTabIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

                val fileAttachmentsTabIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsTabIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

                val cameraAttachmentsTabIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerCameraAttachmentsTabIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

                return AttachmentsPickerDialogStyle(
                    imageAttachmentsTabIconDrawable = imageAttachmentsTabIconDrawable,
                    fileAttachmentsTabIconDrawable = fileAttachmentsTabIconDrawable,
                    cameraAttachmentsTabIconDrawable = cameraAttachmentsTabIconDrawable,
                ).let(TransformStyle.attachmentsPickerStyleTransformer::transform)
            }
        }
    }
}
