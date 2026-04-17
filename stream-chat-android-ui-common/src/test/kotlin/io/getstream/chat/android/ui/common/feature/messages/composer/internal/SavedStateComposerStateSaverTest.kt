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

package io.getstream.chat.android.ui.common.feature.messages.composer.internal

import androidx.lifecycle.SavedStateHandle
import io.getstream.chat.android.models.Attachment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class SavedStateComposerStateSaverTest {

    @Test
    fun `save and restore attachments with parcel-safe extraData`() {
        val store = SavedStateComposerStateSaver(SavedStateHandle())
        val attachments = listOf(
            Attachment(
                upload = File("/tmp/photo.jpg"),
                type = "image",
                name = "photo.jpg",
                fileSize = 5000,
                mimeType = "image/jpeg",
                title = "Photo",
            ),
            Attachment(
                upload = File("/tmp/recording.aac"),
                type = "voicemail",
                name = "recording.aac",
                mimeType = "audio/aac",
                extraData = mapOf("duration" to 5.2f),
            ),
        )

        store.saveAttachments(attachments)
        val restored = store.restoreAttachments()!!

        assertEquals(2, restored.size)
        assertEquals("/tmp/photo.jpg", restored[0].upload?.absolutePath)
        assertEquals("image", restored[0].type)
        assertEquals("photo.jpg", restored[0].name)
        assertEquals(5000, restored[0].fileSize)
        assertEquals("/tmp/recording.aac", restored[1].upload?.absolutePath)
        assertEquals("voicemail", restored[1].type)
        assertEquals(5.2f, restored[1].extraData["duration"])
    }

    @Test
    fun `restore attachments returns null when not saved`() {
        val store = SavedStateComposerStateSaver(SavedStateHandle())

        assertNull(store.restoreAttachments())
    }

    @Test
    fun `save attachments skips when extraData is not parcel-safe`() {
        val store = SavedStateComposerStateSaver(SavedStateHandle())
        val attachments = listOf(
            Attachment(extraData = mapOf("unsafe" to object {})),
        )

        store.saveAttachments(attachments)

        assertNull(store.restoreAttachments())
    }

    @Test
    fun `save attachments removes previous value when extraData becomes unsafe`() {
        val store = SavedStateComposerStateSaver(SavedStateHandle())

        // First save with safe data
        store.saveAttachments(listOf(Attachment(type = "image", name = "safe.jpg")))
        assertEquals(1, store.restoreAttachments()!!.size)

        // Second save with unsafe data — should remove the key
        store.saveAttachments(listOf(Attachment(extraData = mapOf("bad" to object {}))))
        assertNull(store.restoreAttachments())
    }

    @Test
    fun `clear removes all saved state`() {
        val store = SavedStateComposerStateSaver(SavedStateHandle())

        store.saveAttachments(listOf(Attachment(type = "image")))

        store.clear()

        assertNull(store.restoreAttachments())
    }

    @Test
    fun `save empty attachments list restores as empty list`() {
        val store = SavedStateComposerStateSaver(SavedStateHandle())

        store.saveAttachments(emptyList())
        val restored = store.restoreAttachments()!!

        assertTrue(restored.isEmpty())
    }
}
