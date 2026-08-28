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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.AudioAttachmentItemParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Attachment.UploadState
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class AudioAttachmentContentInteractionTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tapping play reports the attachment to toggle`() {
        val toggled = mutableListOf<Attachment>()
        setContent(onPlayToggleClick = toggled::add)

        composeTestRule.onNodeWithContentDescription(PLAY).performClick()

        assertEquals(listOf(attachment()), toggled)
    }

    @Test
    fun `the play button is disabled while the attachment is uploading`() {
        setContent(
            attachment = attachment().copy(
                uploadState = UploadState.InProgress(bytesUploaded = 1, totalBytes = 2),
            ),
        )

        composeTestRule.onNodeWithContentDescription(PLAY).assertIsNotEnabled()
    }

    @Test
    fun `the play button is disabled when the attachment has no source`() {
        setContent(attachment = attachment().copy(assetUrl = null))

        composeTestRule.onNodeWithContentDescription(PLAY).assertIsNotEnabled()
    }

    @Test
    fun `dragging the seek bar reports the start and the released progress`() {
        var dragStarted = false
        val released = mutableListOf<Float>()
        setContent(
            onThumbDragStart = { dragStarted = true },
            onThumbDragStop = { _, progress -> released += progress },
        )

        // Split so the slider can recompose between the move and the release: a drag ends with no position, so the
        // released progress is whatever the last recomposition carried.
        val slider = composeTestRule.onNodeWithTag(SLIDER)
        slider.performTouchInput { down(centerLeft) }
        composeTestRule.waitForIdle()
        slider.performTouchInput { moveTo(center) }
        composeTestRule.waitForIdle()
        slider.performTouchInput { up() }
        composeTestRule.waitForIdle()

        assertTrue("drag start was not reported", dragStarted)
        assertEquals(1, released.size)
        assertTrue("released progress should be past the start, was ${released.first()}", released.first() > 0f)
    }

    @Test
    fun `the factory slot renders the player through the audio player view model`() {
        composeTestRule.setContent {
            ChatTheme {
                ChatTheme.componentFactory.AudioAttachmentItem(
                    params = AudioAttachmentItemParams(attachment = attachment(), isMine = false),
                )
            }
        }

        composeTestRule.onNodeWithTag("Stream_AudioAttachmentName").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(PLAY).assertIsDisplayed()
    }

    private fun setContent(
        attachment: Attachment = attachment(),
        onPlayToggleClick: (Attachment) -> Unit = {},
        onThumbDragStart: (Attachment) -> Unit = {},
        onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    ) {
        composeTestRule.setContent {
            ChatTheme {
                AudioAttachmentContentItem(
                    attachment = attachment,
                    playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
                    onPlayToggleClick = onPlayToggleClick,
                    onThumbDragStart = onThumbDragStart,
                    onThumbDragStop = onThumbDragStop,
                )
            }
        }
    }

    private fun attachment() = Attachment(
        type = AttachmentType.AUDIO,
        name = "audio-snippet.mp3",
        mimeType = "audio/mpeg",
        assetUrl = "https://example.com/audio.mp3",
    )

    private companion object {
        const val PLAY = "Play"
        const val SLIDER = "Stream_AudioAttachmentSlider"
    }
}
