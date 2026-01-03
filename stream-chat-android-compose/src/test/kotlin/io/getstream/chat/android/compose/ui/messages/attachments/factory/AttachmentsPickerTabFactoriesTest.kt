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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.ui.common.permissions.SystemAttachmentsPickerConfig
import io.getstream.chat.android.ui.common.permissions.VisualMediaType
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.Test

internal class AttachmentsPickerTabFactoriesTest {

    @Test
    fun testDefaultFactoriesWithoutStoragePermissions() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        // then
        factories.size `should be equal to` 1
        factories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val factory = factories[0] as AttachmentsPickerSystemTabFactory
        factory.config.visualMediaAllowed `should be equal to` true
        factory.config.visualMediaAllowMultiple `should be equal to` false
        factory.config.visualMediaType `should be equal to` VisualMediaType.IMAGE_AND_VIDEO
        factory.config.filesAllowed `should be equal to` true
        factory.config.captureImageAllowed `should be equal to` true
        factory.config.captureVideoAllowed `should be equal to` true
        factory.config.pollAllowed `should be equal to` true
    }

    /**
     * Theoretic case, in reality we should never disable all options.
     */
    @Test
    fun testDefaultFactoriesWithoutStoragePermissionsAllDisabled() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = false,
            mediaAllowed = false,
            captureImageAllowed = false,
            captureVideoAllowed = false,
            pollAllowed = false,
        )
        // then
        factories.size `should be equal to` 1
        factories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val factory = factories[0] as AttachmentsPickerSystemTabFactory
        factory.config.visualMediaAllowed `should be equal to` false
        factory.config.visualMediaAllowMultiple `should be equal to` false
        factory.config.visualMediaType `should be equal to` VisualMediaType.IMAGE_AND_VIDEO
        factory.config.filesAllowed `should be equal to` false
        factory.config.captureImageAllowed `should be equal to` false
        factory.config.captureVideoAllowed `should be equal to` false
        factory.config.pollAllowed `should be equal to` false
    }

    @Test
    fun testSystemAttachmentsPickerTabFactoriesWithDefaultConfig() {
        // given
        val config = SystemAttachmentsPickerConfig()
        // when
        val factories = AttachmentsPickerTabFactories.systemAttachmentsPickerTabFactories(config)
        // then
        factories.size `should be equal to` 1
        factories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val factory = factories[0] as AttachmentsPickerSystemTabFactory
        factory.config.visualMediaAllowed `should be equal to` true
        factory.config.visualMediaAllowMultiple `should be equal to` false
        factory.config.visualMediaType `should be equal to` VisualMediaType.IMAGE_AND_VIDEO
        factory.config.filesAllowed `should be equal to` true
        factory.config.captureImageAllowed `should be equal to` true
        factory.config.captureVideoAllowed `should be equal to` true
        factory.config.pollAllowed `should be equal to` true
    }

    @Test
    fun testDefaultFactories() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactories()
        // then
        factories.size `should be equal to` 4
        factories[0] `should be instance of` AttachmentsPickerImagesTabFactory::class
        factories[1] `should be instance of` AttachmentsPickerFilesTabFactory::class
        factories[2] `should be instance of` AttachmentsPickerMediaCaptureTabFactory::class
        factories[3] `should be instance of` AttachmentsPickerPollTabFactory::class
    }

    /**
     * Theoretic case, in reality we should never disable all options.
     */
    @Test
    fun testDefaultFactoriesAllDisabled() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactories(
            imagesTabEnabled = false,
            filesTabEnabled = false,
            takeImageEnabled = false,
            recordVideoEnabled = false,
            pollEnabled = false,
        )
        // then
        factories.size `should be equal to` 0
    }

    @Test
    fun testDefaultFactoriesTakeImageDisabled() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactories(
            imagesTabEnabled = true,
            filesTabEnabled = true,
            takeImageEnabled = false,
            recordVideoEnabled = true,
            pollEnabled = true,
        )
        // then
        factories.size `should be equal to` 4
        factories[0] `should be instance of` AttachmentsPickerImagesTabFactory::class
        factories[1] `should be instance of` AttachmentsPickerFilesTabFactory::class
        factories[2] `should be instance of` AttachmentsPickerMediaCaptureTabFactory::class
        factories[3] `should be instance of` AttachmentsPickerPollTabFactory::class
    }

    @Test
    fun testDefaultFactoriesRecordVideoDisabled() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactories(
            imagesTabEnabled = true,
            filesTabEnabled = true,
            takeImageEnabled = true,
            recordVideoEnabled = false,
            pollEnabled = true,
        )
        // then
        factories.size `should be equal to` 4
        factories[0] `should be instance of` AttachmentsPickerImagesTabFactory::class
        factories[1] `should be instance of` AttachmentsPickerFilesTabFactory::class
        factories[2] `should be instance of` AttachmentsPickerMediaCaptureTabFactory::class
        factories[3] `should be instance of` AttachmentsPickerPollTabFactory::class
    }
}
