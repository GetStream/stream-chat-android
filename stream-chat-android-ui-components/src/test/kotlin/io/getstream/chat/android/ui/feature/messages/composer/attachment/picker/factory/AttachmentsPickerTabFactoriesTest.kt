/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.poll.AttachmentsPickerPollTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system.AttachmentsPickerSystemTabFactory
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.Test

internal class AttachmentsPickerTabFactoriesTest {

    @Test
    fun testDefaultFactoriesWithoutPermissions() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutPermissions(
            mediaAttachmentsTabEnabled = true,
            fileAttachmentsTabEnabled = true,
            cameraAttachmentsTabEnabled = true,
            pollAttachmentsTabEnabled = true,
        )
        // then
        factories.size `should be equal to` 1
        factories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
    }

    @Test
    fun testDefaultFactories() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactories(
            mediaAttachmentsTabEnabled = true,
            fileAttachmentsTabEnabled = true,
            cameraAttachmentsTabEnabled = true,
            pollAttachmentsTabEnabled = true,
        )
        // then
        factories.size `should be equal to` 4
        factories[0] `should be instance of` AttachmentsPickerMediaTabFactory::class
        factories[1] `should be instance of` AttachmentsPickerFileTabFactory::class
        factories[2] `should be instance of` AttachmentsPickerCameraTabFactory::class
        factories[3] `should be instance of` AttachmentsPickerPollTabFactory::class
    }

    /**
     * Theoretic case, in reality we should never disable all options.
     */
    @Test
    fun testDefaultFactoriesAllDisabled() {
        // when
        val factories = AttachmentsPickerTabFactories.defaultFactories(
            mediaAttachmentsTabEnabled = false,
            fileAttachmentsTabEnabled = false,
            cameraAttachmentsTabEnabled = false,
            pollAttachmentsTabEnabled = false,
        )
        // then
        factories.size `should be equal to` 0
    }
}
