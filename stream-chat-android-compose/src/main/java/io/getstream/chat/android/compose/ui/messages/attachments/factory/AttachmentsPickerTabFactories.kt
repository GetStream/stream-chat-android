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

import io.getstream.chat.android.ui.common.permissions.SystemAttachmentsPickerConfig

/**
 * Provides different attachment picker tab factories that build tab icons and tab contents for
 * the attachment picker.
 */
public object AttachmentsPickerTabFactories {

    /**
     * Builds the default list of attachment picker tab factories (without requesting storage permission).
     */
    @Deprecated(
        message = "Use systemAttachmentsPickerTabFactories(config: SystemAttachmentsPickerConfig2) instead.",
        replaceWith = ReplaceWith(expression = "systemAttachmentsPickerTabFactories(config)"),
        level = DeprecationLevel.ERROR,
    )
    public fun defaultFactoriesWithoutStoragePermissions(): List<AttachmentsPickerTabFactory> = systemAttachmentsPickerTabFactories(SystemAttachmentsPickerConfig())

    /**
     * Builds the default list of attachment picker tab factories (without requesting storage permission).
     *
     * @param filesAllowed If the option to pick files is included in the attachments picker.
     * @param mediaAllowed If the option to pick media (images/videos) is included in the attachments picker.
     * @param captureImageAllowed If the option to capture an image is included in the attachments picker.
     * @param captureVideoAllowed If the option to capture a video is included in the attachments picker.
     * @param pollAllowed If the option to create a poll is included in the attachments picker.
     */
    @Deprecated(
        message = "Use systemAttachmentsPickerTabFactories(config: SystemAttachmentsPickerConfig2) instead.",
        replaceWith = ReplaceWith(expression = "systemAttachmentsPickerTabFactories(config)"),
        level = DeprecationLevel.WARNING,
    )
    public fun defaultFactoriesWithoutStoragePermissions(
        filesAllowed: Boolean = true,
        mediaAllowed: Boolean = true,
        captureImageAllowed: Boolean = true,
        captureVideoAllowed: Boolean = true,
        pollAllowed: Boolean = true,
    ): List<AttachmentsPickerTabFactory> {
        val config = SystemAttachmentsPickerConfig(
            filesAllowed = filesAllowed,
            visualMediaAllowed = mediaAllowed,
            captureImageAllowed = captureImageAllowed,
            captureVideoAllowed = captureVideoAllowed,
            pollAllowed = pollAllowed,
        )
        return systemAttachmentsPickerTabFactories(config)
    }

    /**
     * Builds the default list of attachment picker tab factories (without requesting storage permission).
     *
     * @param config The configuration for the system attachment picker.
     */
    public fun systemAttachmentsPickerTabFactories(
        config: SystemAttachmentsPickerConfig = SystemAttachmentsPickerConfig(),
    ): List<AttachmentsPickerTabFactory> {
        val factory = AttachmentsPickerSystemTabFactory(config)
        return listOf(factory)
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
    ): List<AttachmentsPickerTabFactory> = listOfNotNull(
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
