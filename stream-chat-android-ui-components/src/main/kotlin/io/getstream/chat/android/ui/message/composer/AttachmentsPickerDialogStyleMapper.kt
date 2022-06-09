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
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle

/**
 * For backwards compatibility
 */
internal fun AttachmentsPickerDialogStyle.toMessageInputViewStyle(context: Context): MessageInputViewStyle {
    return MessageInputViewStyle.createDefault(context).copy(
        attachmentSelectionDialogStyle = AttachmentSelectionDialogStyle.createDefault(context).copy(
            pictureAttachmentIcon = imageAttachmentsTabIconDrawable,
            fileAttachmentIcon = fileAttachmentsTabIconDrawable,
            cameraAttachmentIcon = cameraAttachmentsTabIconDrawable
        )
    )
}
