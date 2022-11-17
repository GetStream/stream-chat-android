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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory

import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.camera.AttachmentsPickerCameraTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.file.AttachmentsPickerFileTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.media.AttachmentsPickerMediaTabFactory

/**
 * Provides the default list of tab factories for the attachment picker.
 */
public object AttachmentsPickerTabFactories {

    /**
     * Creates a list of factories for the tabs that will be displayed in the attachment picker.
     *
     * @param mediaAttachmentsTabEnabled If the media attachments tab will be displayed in the picker.
     * @param fileAttachmentsTabEnabled If the file attachments tab will be displayed in the picker.
     * @param cameraAttachmentsTabEnabled If the camera attachments tab will be displayed in the picker.
     * @return The list factories for the tabs that will be displayed in the attachment picker.
     */
    public fun defaultFactories(
        mediaAttachmentsTabEnabled: Boolean,
        fileAttachmentsTabEnabled: Boolean,
        cameraAttachmentsTabEnabled: Boolean,
    ): List<AttachmentsPickerTabFactory> {
        return listOfNotNull(
            if (mediaAttachmentsTabEnabled) AttachmentsPickerMediaTabFactory() else null,
            if (fileAttachmentsTabEnabled) AttachmentsPickerFileTabFactory() else null,
            if (cameraAttachmentsTabEnabled) AttachmentsPickerCameraTabFactory() else null,
        )
    }
}
