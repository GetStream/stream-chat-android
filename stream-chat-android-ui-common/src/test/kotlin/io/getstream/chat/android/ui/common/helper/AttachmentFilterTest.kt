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

package io.getstream.chat.android.ui.common.helper

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AttachmentFilterTest {

    private val chatClient: ChatClient = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

    @ParameterizedTest
    @MethodSource("attachmentFilterArguments")
    fun `Given file and image upload configs When filtering attachments Should return only valid attachments`(
        attachmentFilterTestData: AttachmentFilterTestData,
    ) {
        whenever(chatClient.getAppSettings().app.fileUploadConfig) doReturn attachmentFilterTestData.fileUploadConfig
        whenever(chatClient.getAppSettings().app.imageUploadConfig) doReturn attachmentFilterTestData.imageUploadConfig
        val attachmentFilter = AttachmentFilter(chatClient)

        val filteredAttachments = attachmentFilter.filterAttachments(attachments)

        filteredAttachments.size `should be equal to` attachmentFilterTestData.expectedAttachmentCount
    }

    internal data class AttachmentFilterTestData(
        val fileUploadConfig: FileUploadConfig,
        val imageUploadConfig: FileUploadConfig,
        val expectedAttachmentCount: Int,
    )

    private companion object {
        private val attachments = listOf(
            AttachmentMetaData(
                mimeType = "image/jpeg",
                title = "IMG_123.jpeg",
                type = "image"
            ),
            AttachmentMetaData(
                mimeType = "video/mp4",
                title = "VID_123.mp4",
                type = "file"
            )
        )

        @JvmStatic
        fun attachmentFilterArguments() = listOf(
            Arguments.of(
                AttachmentFilterTestData(
                    fileUploadConfig = fileUploadConfig(),
                    imageUploadConfig = fileUploadConfig(),
                    expectedAttachmentCount = 2
                )
            ),
            Arguments.of(
                AttachmentFilterTestData(
                    fileUploadConfig = fileUploadConfig(allowedFileExtensions = listOf(".mp4")),
                    imageUploadConfig = fileUploadConfig(allowedFileExtensions = listOf(".jpeg")),
                    expectedAttachmentCount = 2
                )
            ),
            Arguments.of(
                AttachmentFilterTestData(
                    fileUploadConfig = fileUploadConfig(blockedFileExtensions = listOf(".mp4")),
                    imageUploadConfig = fileUploadConfig(blockedFileExtensions = listOf(".jpeg")),
                    expectedAttachmentCount = 0
                )
            ),
            Arguments.of(
                AttachmentFilterTestData(
                    fileUploadConfig = fileUploadConfig(allowedMimeTypes = listOf("video/mp4")),
                    imageUploadConfig = fileUploadConfig(allowedMimeTypes = listOf("image/jpeg")),
                    expectedAttachmentCount = 2
                )
            ),
            Arguments.of(
                AttachmentFilterTestData(
                    fileUploadConfig = fileUploadConfig(blockedMimeTypes = listOf("video/mp4")),
                    imageUploadConfig = fileUploadConfig(blockedMimeTypes = listOf("image/jpeg")),
                    expectedAttachmentCount = 0
                )
            ),
        )

        private fun fileUploadConfig(
            allowedFileExtensions: List<String> = emptyList(),
            allowedMimeTypes: List<String> = emptyList(),
            blockedFileExtensions: List<String> = emptyList(),
            blockedMimeTypes: List<String> = emptyList(),
        ): FileUploadConfig {
            return FileUploadConfig(
                allowedFileExtensions = allowedFileExtensions,
                allowedMimeTypes = allowedMimeTypes,
                blockedFileExtensions = blockedFileExtensions,
                blockedMimeTypes = blockedMimeTypes,
            )
        }
    }
}
