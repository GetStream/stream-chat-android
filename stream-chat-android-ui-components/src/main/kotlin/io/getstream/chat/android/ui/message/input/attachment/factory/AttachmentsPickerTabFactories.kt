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

package io.getstream.chat.android.ui.message.input.attachment.factory

import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.factory.camera.AttachmentsPickerCameraTabFactory
import io.getstream.chat.android.ui.message.input.attachment.factory.file.AttachmentsPickerFileTabFactory
import io.getstream.chat.android.ui.message.input.attachment.factory.media.AttachmentsPickerMediaTabFactory

/**
 * Provides the default list of tab factories for the attachment picker.
 */
public object AttachmentsPickerTabFactories {

    /**
     * Creates a list of factories for the tabs that will be displayed in the attachment picker.
     *
     * @param style Style for the dialog.
     * @return The list factories for the tabs that will be displayed in the attachment picker.
     */
    public fun defaultFactories(style: MessageInputViewStyle): List<AttachmentsPickerTabFactory> {
        val dialogStyle = style.attachmentSelectionDialogStyle
        return listOfNotNull(
            if (dialogStyle.mediaAttachmentsTabEnabled) AttachmentsPickerMediaTabFactory() else null,
            if (dialogStyle.fileAttachmentsTabEnabled) AttachmentsPickerFileTabFactory() else null,
            if (dialogStyle.cameraAttachmentsTabEnabled) AttachmentsPickerCameraTabFactory() else null,
        )
    }
}
