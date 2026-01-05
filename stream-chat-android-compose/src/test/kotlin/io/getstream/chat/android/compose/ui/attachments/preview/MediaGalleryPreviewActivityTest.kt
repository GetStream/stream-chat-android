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

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.ui.common.helper.DefaultDownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MediaGalleryPreviewActivityTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    fun `throws exceptions when message ID is not provided`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MediaGalleryPreviewActivity::class.java)

        assertThrows<IllegalArgumentException> {
            ActivityScenario.launch<MediaGalleryPreviewActivity>(intent)
        }
    }

    @Test
    fun `should set result on reply option click`() {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val intent = createIntent(message)

        ActivityScenario.launchActivityForResult<MediaGalleryPreviewActivity>(intent).use { scenario ->
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Image options").performClick()
            composeTestRule.onNodeWithText("Reply").performClick()

            scenario.assertResult(
                expected = MediaGalleryPreviewResult(
                    messageId = message.id,
                    parentMessageId = message.parentId,
                    resultType = MediaGalleryPreviewResultType.QUOTE,
                ),
            )
        }
    }

    @Test
    fun `should set result on show in chat option click`() {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val intent = createIntent(message)

        ActivityScenario.launchActivityForResult<MediaGalleryPreviewActivity>(intent).use { scenario ->
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Image options").performClick()
            composeTestRule.onNodeWithText("Show in chat").performClick()

            scenario.assertResult(
                expected = MediaGalleryPreviewResult(
                    messageId = message.id,
                    parentMessageId = message.parentId,
                    resultType = MediaGalleryPreviewResultType.SHOW_IN_CHAT,
                ),
            )
        }
    }

    @Test
    fun `should delete on delete option click`() {
        val currentUser = PreviewUserData.user7
        whenever(mockClientState.user) doReturn MutableStateFlow(currentUser)
        val message = PreviewMessageData.messageWithUserAndAttachment.copy(user = currentUser)
        val intent = createIntent(message)

        ActivityScenario.launchActivityForResult<MediaGalleryPreviewActivity>(intent).use { scenario ->
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Image options").performClick()

            composeTestRule.onNodeWithText("Delete").performClick()
        }
    }

    @Test
    fun `should save on save option click`() {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val intent = createIntent(message)

        ActivityScenario.launchActivityForResult<MediaGalleryPreviewActivity>(intent).use { scenario ->
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Image options").performClick()

            composeTestRule.onNodeWithText("Save media").performClick()
        }
    }

    @Test
    fun `should share on share option click`() {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val intent = createIntent(message)

        ActivityScenario.launchActivityForResult<MediaGalleryPreviewActivity>(intent).use { scenario ->
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Share").performClick()
        }
    }

    @Ignore("https://linear.app/stream/issue/AND-741")
    @Test
    fun `should show large file warning dialog when sharing a large file`() {
        val message = PreviewMessageData.messageWithUserAndAttachment.run {
            copy(
                attachments = attachments.map { it.copy(fileSize = 15 * 1024 * 1024) }, // 15 MB
            )
        }
        val intent = createIntent(message)

        ActivityScenario.launchActivityForResult<MediaGalleryPreviewActivity>(intent).use { scenario ->
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription("Share").performClick()

            composeTestRule.onNodeWithText("Large file warning").assertExists()
            composeTestRule.onNodeWithText("15.00 MB needs to be downloaded before sharing.").assertExists()
            composeTestRule.onNodeWithText("Cancel").assertExists()
            composeTestRule.onNodeWithText("OK").assertExists()
        }
    }

    private fun createIntent(message: Message) = MediaGalleryPreviewContract().createIntent(
        context = ApplicationProvider.getApplicationContext(),
        input = MediaGalleryPreviewContract.Input(
            message = message,
            videoThumbnailsEnabled = true,
            downloadAttachmentUriGenerator = DefaultDownloadAttachmentUriGenerator,
            downloadRequestInterceptor = {},
            streamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
        ),
    ).also {
        whenever(mockChatClient.getMessage(message.id)) doReturn TestCall(Result.Success(message))
    }
}

private fun ActivityScenario<MediaGalleryPreviewActivity>.assertResult(expected: MediaGalleryPreviewResult) {
    assertEquals(Activity.RESULT_OK, result.resultCode)
    val actual = result.resultData?.getParcelableExtra(
        "mediaGalleryPreviewResult",
        MediaGalleryPreviewResult::class.java,
    )
    assertEquals(expected, actual)
}
