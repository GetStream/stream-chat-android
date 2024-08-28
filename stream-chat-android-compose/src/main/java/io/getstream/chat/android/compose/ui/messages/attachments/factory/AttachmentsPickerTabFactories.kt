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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

/**
 * Provides different attachment picker tab factories that build tab icons and tab contents for
 * the attachment picker.
 */
public object AttachmentsPickerTabFactories {

    public fun defaultFactoriesWithoutStoragePermissions(): List<AttachmentsPickerTabFactory> {
        val otherFactories = defaultFactories(
            imagesTabEnabled = false,
            filesTabEnabled = false,
            takeImageEnabled = true,
            recordVideoEnabled = true,
            pollEnabled = true
        )
        return listOf(AttachmentsPickerSystemTabFactory(otherFactories))
    }

    /**
     * Builds the default list of attachment picker tab factories.
     *
     * @param imagesTabEnabled If the factory that allows users to pick images is included in the resulting list.
     * @param filesTabEnabled If the factory that allows users to pick files is included in the resulting list.
     * @param takeImageEnabled If the factory that allows users to start image capture is included in the resulting list.
     * @param recordVideoEnabled If the factory that allows users to start video capture is included in the resulting list.
     * @param pollEnabled If the factory that allows users to create a poll.
     * @return The default list of attachment picker tab factories.
     */
    public fun defaultFactories(
        imagesTabEnabled: Boolean = true,
        filesTabEnabled: Boolean = true,
        takeImageEnabled: Boolean = true,
        recordVideoEnabled: Boolean = true,
        pollEnabled: Boolean = true,
    ): List<AttachmentsPickerTabFactory> {
        return listOfNotNull(
            if (imagesTabEnabled) AttachmentsPickerImagesTabFactory() else null,
            if (filesTabEnabled) AttachmentsPickerFilesTabFactory() else null,
            when {
                takeImageEnabled && recordVideoEnabled ->
                    AttachmentsPickerMediaCaptureTabFactory(
                        AttachmentsPickerMediaCaptureTabFactory.PickerMediaMode.PHOTO_AND_VIDEO,
                    )

                takeImageEnabled ->
                    AttachmentsPickerMediaCaptureTabFactory(
                        AttachmentsPickerMediaCaptureTabFactory.PickerMediaMode.PHOTO,
                    )

                recordVideoEnabled ->
                    AttachmentsPickerMediaCaptureTabFactory(
                        AttachmentsPickerMediaCaptureTabFactory.PickerMediaMode.VIDEO,
                    )

                else -> null
            },
            if (pollEnabled) AttachmentsPickerPollTabFactory() else null,
        )
    }
}
