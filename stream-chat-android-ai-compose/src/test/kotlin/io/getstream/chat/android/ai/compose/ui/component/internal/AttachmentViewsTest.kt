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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.ui.component.AIMessageAttachmentItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class AttachmentViewsTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `tapping a file opens it`() {
        val uri = "https://example.com/files/itinerary.pdf".toUri()
        rule.setContent { AIMessageAttachmentItem(AIMessageAttachment.file(uri, size = 2_400_000)) }

        rule.onNodeWithText("itinerary.pdf")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher("is labelled Open") { it.config[SemanticsActions.OnClick].label == "Open" })
            .performClick()

        val intent = shadowOf(rule.activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(uri, intent.data)
    }

    @Test
    fun `a file shows its type and size`() {
        rule.setContent {
            AIMessageAttachmentItem(AIMessageAttachment.file("https://example.com/a".toUri(), name = "Notes.pdf", size = 1))
        }

        rule.onNodeWithText("Notes.pdf").assertExists()
        rule.onNodeWithText("PDF", substring = true).assertExists()
    }

    @Test
    fun `a file without a uri can't be tapped`() {
        rule.setContent { AIMessageAttachmentItem(AIMessageAttachment(kind = AIMessageAttachment.Kind.File, name = "Notes")) }

        rule.onNodeWithText("Notes").assertHasNoClickAction()
        assertNull(shadowOf(rule.activity).nextStartedActivity)
    }

    @Test
    fun `an image the app holds shows at once, named for people who can't see it`() {
        val bitmap = solid(android.graphics.Color.RED).asImageBitmap()
        rule.setContent { AIMessageAttachmentItem(AIMessageAttachment.image(bitmap, name = "Sunset.jpg"), imageSize = 40.dp) }

        rule.onNodeWithContentDescription("Sunset.jpg")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertHasNoClickAction()
        assertEquals(Color.Red, centerOf("Sunset.jpg"))
    }

    @Test
    fun `an image on this device shows its placeholder, then the image once loaded`() {
        val uri = imageFile(android.graphics.Color.BLUE)
        rule.setContent { AIMessageAttachmentItem(AIMessageAttachment.image(uri), imageSize = 40.dp) }

        rule.waitUntil(timeoutMillis = 5_000) { centerOf(uri.lastPathSegment!!) == Color.Blue }
    }

    @Test
    fun `an image already in memory shows without loading`() {
        val uri = imageFile(android.graphics.Color.GREEN)
        val sizePx = with(rule.density) { 40.dp.roundToPx() }
        runBlocking { AttachmentImageLoader.load(rule.activity, uri, sizePx) }
        // Previews don't load, so only the image kept in memory can show.
        rule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                AIMessageAttachmentItem(AIMessageAttachment.image(uri), imageSize = 40.dp)
            }
        }

        assertEquals(Color.Green, centerOf(uri.lastPathSegment!!))
    }

    @Test
    fun `previews show an image's placeholder without loading it`() {
        val uri = imageFile(android.graphics.Color.RED)
        rule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                AIMessageAttachmentItem(AIMessageAttachment.image(uri), imageSize = 40.dp)
            }
        }
        rule.waitForIdle()

        assertNotEquals(Color.Red, centerOf(uri.lastPathSegment!!))
        assertNull(AttachmentImageLoader.cached(uri, with(rule.density) { 40.dp.roundToPx() }))
    }

    @Test
    fun `an image with nothing to show shows as a file`() {
        rule.setContent {
            AIMessageAttachmentItem(AIMessageAttachment(kind = AIMessageAttachment.Kind.Image, name = "Lost.jpg"))
        }

        rule.onNodeWithText("Lost.jpg").assertExists()
        rule.onNodeWithContentDescription("Lost.jpg").assertDoesNotExist()
    }

    @Test
    fun `an image as a file can be tapped to open it`() {
        rule.setContent {
            AIMessageAttachmentItem(AIMessageAttachment.file("content://com.example/photo.jpg".toUri(), mimeType = "image/jpeg"))
        }

        rule.onNodeWithText("photo.jpg").assertHasClickAction()
    }

    // The color at the middle of a node, as drawn in software: what the person would see there.
    private fun centerOf(description: String): Color {
        val bounds = rule.onNodeWithContentDescription(description).fetchSemanticsNode().boundsInRoot
        val root = rule.activity.findViewById<View>(android.R.id.content)
        val drawn = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { root.draw(Canvas(drawn)) }
        return Color(drawn.getPixel(bounds.center.x.toInt(), bounds.center.y.toInt()))
    }

    private fun solid(color: Int): Bitmap =
        Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    private fun imageFile(color: Int): Uri {
        val file = File.createTempFile("image", ".png", rule.activity.cacheDir)
        file.outputStream().use { solid(color).compress(Bitmap.CompressFormat.PNG, 100, it) }
        return Uri.fromFile(file)
    }
}
