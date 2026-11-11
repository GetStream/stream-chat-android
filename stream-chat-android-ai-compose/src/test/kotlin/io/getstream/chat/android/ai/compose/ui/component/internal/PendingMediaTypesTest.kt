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

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
internal class PendingMediaTypesTest {

    // Says the type of each document by its last path segment, as a document provider would.
    internal class Documents : ContentProvider() {
        override fun onCreate() = true
        override fun getType(uri: Uri): String? = when (uri.lastPathSegment) {
            "plan" -> "application/pdf"
            "photo" -> "image/jpeg"
            else -> null
        }
        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?,
        ): Cursor? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, s: String?, a: Array<out String>?) = 0
        override fun update(uri: Uri, v: ContentValues?, s: String?, a: Array<out String>?) = 0
    }

    private val resolver get() = ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver

    @Before
    fun setUp() {
        Robolectric.setupContentProvider(Documents::class.java, "test.documents")
    }

    @Test
    fun `a document whose type the provider says is not an image is a file`() {
        val plan = "content://test.documents/plan".toUri()
        val pending = AIPendingMessage(text = "", attachments = listOf(AIMessageAttachment.image(plan)))

        val typed = pending.withMediaTypes(resolver)

        assertEquals(AIMessageAttachment.Kind.File, typed.attachments.single().kind)
        assertEquals("application/pdf", typed.attachments.single().mimeType)
    }

    @Test
    fun `an image stays an image, and one of unknown type keeps its preview`() {
        val photo = AIMessageAttachment.image("content://test.documents/photo".toUri())
        val unknown = AIMessageAttachment.image("content://test.documents/other".toUri())
        val pending = AIPendingMessage(text = "", attachments = listOf(photo, unknown))

        val typed = pending.withMediaTypes(resolver)

        assertEquals(listOf(AIMessageAttachment.Kind.Image, AIMessageAttachment.Kind.Image), typed.attachments.map { it.kind })
        assertEquals(unknown, typed.attachments[1])
    }
}
