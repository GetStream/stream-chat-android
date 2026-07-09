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

package io.getstream.chat.android.compose.ui.attachments.preview

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewMessageData
import org.junit.Rule
import org.junit.Test

internal class MediaGalleryPreviewScreenTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

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
        MediaGalleryScreen(connectionState = ConnectionState.Offline)
    }

    @Test
    fun `media gallery screen offline in dark mode`() = snapshot(isInDarkMode = true) {
        MediaGalleryScreen(connectionState = ConnectionState.Offline)
    }

    @Test
    fun `media gallery screen connected`() = snapshot {
        MediaGalleryScreen()
    }

    @Test
    fun `media gallery screen connected in dark mode`() = snapshot(isInDarkMode = true) {
        MediaGalleryScreen()
    }

    @Test
    fun `media gallery screen with options menu`() = snapshot {
        MediaGalleryScreen(isShowingOptions = true)
    }

    @Test
    fun `media gallery screen with options menu in dark mode`() = snapshot(isInDarkMode = true) {
        MediaGalleryScreen(isShowingOptions = true)
    }

    @Test
    fun `media gallery screen with share large file prompt`() = snapshot {
        MediaGalleryScreen(promptShareAttachment = true)
    }

    @Test
    fun `media gallery screen with share large file prompt in dark mode`() = snapshot(isInDarkMode = true) {
        MediaGalleryScreen(promptShareAttachment = true)
    }

    @Test
    fun `media gallery screen with gallery bottom sheet`() = snapshot {
        MediaGalleryScreen(isShowingGallery = true)
    }

    @Test
    fun `media gallery screen with gallery bottom sheet in dark mode`() = snapshot(isInDarkMode = true) {
        MediaGalleryScreen(isShowingGallery = true)
    }

    @Composable
    private fun MediaGalleryScreen(
        connectionState: ConnectionState = ConnectionState.Connected,
        promptShareAttachment: Boolean = false,
        isShowingOptions: Boolean = false,
        isShowingGallery: Boolean = false,
    ) {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryPreviewScreen(
            message = message,
            connectionState = connectionState,
            currentUser = message.user,
            selectedAttachmentUrl = null,
            promptedAttachment = message.attachments.first().takeIf { promptShareAttachment },
            isSharingInProgress = false,
            isShowingOptions = isShowingOptions,
            isShowingGallery = isShowingGallery,
            onOptionClick = { _, _ -> },
            onRequestShareAttachment = {},
        )
    }
}
