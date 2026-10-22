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

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
internal class OpenAttachmentTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("pdf", "application/pdf")
    }

    @Test
    fun `a file on the web opens in the browser`() {
        val uri = "https://example.com/files/itinerary.pdf".toUri()

        openAttachment(application, AIMessageAttachment.file(uri))

        val intent = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(uri, intent.data)
        assertNull("the browser picks the type", intent.type)
        assertFalse(intent.grants(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    }

    @Test
    fun `a file on this device opens in an app for its type, which may read it`() {
        val uri = "content://com.example.files/docs/itinerary.pdf".toUri()

        openAttachment(application, AIMessageAttachment.file(uri))

        val intent = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(uri, intent.data)
        assertEquals("application/pdf", intent.type)
        assertTrue(intent.grants(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    }

    @Test
    fun `a file's own media type wins over its extension`() {
        val uri = "content://com.example.files/docs/itinerary.pdf".toUri()

        openAttachment(application, AIMessageAttachment.file(uri, mimeType = "application/x-custom"))

        assertEquals("application/x-custom", shadowOf(application).nextStartedActivity.type)
    }

    @Test
    fun `a file of no known type opens in any app`() {
        openAttachment(application, AIMessageAttachment.file("content://com.example.files/7f3a".toUri()))

        assertEquals("*/*", shadowOf(application).nextStartedActivity.type)
    }

    @Test
    fun `opening outside an activity starts a new task`() {
        openAttachment(application, AIMessageAttachment.file("https://example.com/a.pdf".toUri()))

        assertTrue(shadowOf(application).nextStartedActivity.grants(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `opening from an activity stays in its task`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

        openAttachment(ContextWrapper(activity), AIMessageAttachment.file("https://example.com/a.pdf".toUri()))

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals("https://example.com/a.pdf".toUri(), intent.data)
        assertFalse(intent.grants(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `an attachment without a uri opens nothing`() {
        openAttachment(application, AIMessageAttachment(kind = AIMessageAttachment.Kind.File, name = "Notes.pdf"))

        assertNull(shadowOf(application).nextStartedActivity)
    }

    @Test
    fun `without an app to open it, it says so`() {
        // Fails to start what no installed app handles, as a device does.
        shadowOf(application).checkActivities(true)

        openAttachment(application, AIMessageAttachment.file("content://com.example.files/itinerary.pdf".toUri()))

        assertNull(shadowOf(application).nextStartedActivity)
        assertEquals("No app can open this file", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `the activity of a context is found through its wrappers`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

        assertSame(activity, activity.findActivity())
        assertSame(activity, ContextWrapper(ContextWrapper(activity)).findActivity())
        assertNull(application.findActivity())
    }

    private fun Intent.grants(flag: Int) = flags and flag == flag
}
