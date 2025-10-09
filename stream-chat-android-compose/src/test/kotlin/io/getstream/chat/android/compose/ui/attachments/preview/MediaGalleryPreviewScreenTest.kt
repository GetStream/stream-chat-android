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

package io.getstream.chat.android.compose.ui.attachments.preview

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewMessageData
import org.junit.Rule
import org.junit.Test

internal class MediaGalleryPreviewScreenTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `media gallery header offline`() = snapshotWithDarkMode {
        MediaGalleryPreviewHeader(
            message = PreviewMessageData.messageWithUserAndAttachment,
            connectionState = ConnectionState.Offline,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery header connecting`() = snapshotWithDarkMode {
        MediaGalleryPreviewHeader(
            message = PreviewMessageData.messageWithUserAndAttachment,
            connectionState = ConnectionState.Connecting,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery header online`() = snapshotWithDarkMode {
        MediaGalleryPreviewHeader(
            message = PreviewMessageData.messageWithUserAndAttachment,
            connectionState = ConnectionState.Connected,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery header message without id`() = snapshotWithDarkMode {
        MediaGalleryPreviewHeader(
            message = PreviewMessageData.messageWithUserAndAttachment.copy(id = ""),
            connectionState = ConnectionState.Connected,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery footer offline`() = snapshotWithDarkMode {
        MediaGalleryPreviewFooter(
            attachments = PreviewMessageData.messageWithUserAndAttachment.attachments,
            currentPage = 0,
            totalPages = PreviewMessageData.messageWithUserAndAttachment.attachments.size,
            connectionState = ConnectionState.Offline,
            isSharingInProgress = false,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery footer connected`() = snapshotWithDarkMode {
        MediaGalleryPreviewFooter(
            attachments = PreviewMessageData.messageWithUserAndAttachment.attachments,
            currentPage = 0,
            totalPages = PreviewMessageData.messageWithUserAndAttachment.attachments.size,
            connectionState = ConnectionState.Connected,
            isSharingInProgress = false,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery footer sharing in progress`() = snapshotWithDarkMode {
        MediaGalleryPreviewFooter(
            attachments = PreviewMessageData.messageWithUserAndAttachment.attachments,
            currentPage = 0,
            totalPages = PreviewMessageData.messageWithUserAndAttachment.attachments.size,
            connectionState = ConnectionState.Connected,
            isSharingInProgress = true,
            onLeadingContentClick = {},
            onTrailingContentClick = {},
        )
    }

    @Test
    fun `media gallery screen offline`() = snapshot {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryPreviewScreen(
            message = message,
            connectionState = ConnectionState.Offline,
            currentUser = message.user,
            initialPage = 0,
            promptedAttachment = null,
            isSharingInProgress = false,
            isShowingOptions = false,
            isShowingGallery = false,
            onOptionClick = { _, _ -> },
            onRequestShareAttachment = {},
        )
    }

    @Test
    fun `media gallery screen connected`() = snapshot {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryPreviewScreen(
            message = message,
            connectionState = ConnectionState.Connected,
            currentUser = message.user,
            initialPage = 0,
            promptedAttachment = null,
            isSharingInProgress = false,
            isShowingOptions = false,
            isShowingGallery = false,
            onOptionClick = { _, _ -> },
            onRequestShareAttachment = {},
        )
    }

    @Test
    fun `media gallery screen with options menu`() = snapshot {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryPreviewScreen(
            message = message,
            connectionState = ConnectionState.Connected,
            currentUser = message.user,
            initialPage = 0,
            promptedAttachment = null,
            isSharingInProgress = false,
            isShowingOptions = true,
            isShowingGallery = false,
            onOptionClick = { _, _ -> },
            onRequestShareAttachment = {},
        )
    }

    @Test
    fun `media gallery screen with share large file prompt`() = snapshot {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryPreviewScreen(
            message = message,
            connectionState = ConnectionState.Connected,
            currentUser = message.user,
            initialPage = 0,
            promptedAttachment = message.attachments[0],
            isSharingInProgress = false,
            isShowingOptions = false,
            isShowingGallery = false,
            onOptionClick = { _, _ -> },
            onRequestShareAttachment = {},
        )
    }

    @Test
    fun `media gallery screen with gallery bottom sheet`() = snapshot {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryPreviewScreen(
            message = message,
            connectionState = ConnectionState.Connected,
            currentUser = message.user,
            initialPage = 0,
            promptedAttachment = null,
            isSharingInProgress = false,
            isShowingOptions = false,
            isShowingGallery = true,
            onOptionClick = { _, _ -> },
            onRequestShareAttachment = {},
        )
    }
}
