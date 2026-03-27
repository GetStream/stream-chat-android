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

package io.getstream.chat.android.compose.viewmodel.channel

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

internal class GroupChannelEditViewModelTest {

    @Test
    fun `initial state is not saving`() = runTest {
        val sut = Fixture().get()

        sut.state.test {
            assertFalse(awaitItem().isSaving)
        }
    }

    @Test
    fun `save with name only emits success`() = runTest {
        val name = randomString()
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = name, imageFile = null, removeImage = false)
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
        }
        fixture.verifyUpdatePartial(mapOf("name" to name))
    }

    @Test
    fun `save with name only does not call uploadImage`() = runTest {
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = randomString(), imageFile = null, removeImage = false)
            awaitItem()
        }
        fixture.verifyUploadImageNeverCalled()
    }

    @Test
    fun `save with image uploads then updates channel`() = runTest {
        val name = randomString()
        val imageUrl = "https://cdn.example.com/uploaded.jpg"
        val imageFile = File.createTempFile("test", ".jpg")
        val fixture = Fixture()
            .givenUploadImage(imageFile, imageUrl)
            .givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = name, imageFile = imageFile, removeImage = false)
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
        }
        fixture.verifyUpdatePartial(mapOf("name" to name, "image" to imageUrl))
        imageFile.delete()
    }

    @Test
    fun `save with removeImage sets image to empty string`() = runTest {
        val name = randomString()
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = name, imageFile = null, removeImage = true)
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
        }
        fixture.verifyUpdatePartial(mapOf("name" to name, "image" to ""))
    }

    @Test
    fun `save emits error when upload fails`() = runTest {
        val imageFile = File.createTempFile("test", ".jpg")
        val fixture = Fixture().givenUploadImageError(imageFile)
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = randomString(), imageFile = imageFile, removeImage = false)
            assertEquals(GroupChannelEditViewEvent.SaveError, awaitItem())
        }
        fixture.verifyUpdatePartialNeverCalled()
        imageFile.delete()
    }

    @Test
    fun `save emits error when updatePartial fails`() = runTest {
        val fixture = Fixture().givenUpdatePartialError()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = randomString(), imageFile = null, removeImage = false)
            assertEquals(GroupChannelEditViewEvent.SaveError, awaitItem())
        }
    }

    @Test
    fun `isSaving is true during save and false after`() = runTest {
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.state.test {
            assertFalse(awaitItem().isSaving) // initial
            sut.save(name = randomString(), imageFile = null, removeImage = false)
            assertTrue(awaitItem().isSaving) // saving
            assertFalse(awaitItem().isSaving) // done
        }
    }

    @Test
    fun `isSaving resets to false after upload error`() = runTest {
        val imageFile = File.createTempFile("test", ".jpg")
        val fixture = Fixture().givenUploadImageError(imageFile)
        val sut = fixture.get()

        sut.state.test {
            assertFalse(awaitItem().isSaving)
            sut.save(name = randomString(), imageFile = imageFile, removeImage = false)
            assertTrue(awaitItem().isSaving)
            assertFalse(awaitItem().isSaving)
        }
        imageFile.delete()
    }

    @Test
    fun `duplicate save is ignored while saving`() = runTest {
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = "first", imageFile = null, removeImage = false)
            sut.save(name = "second", imageFile = null, removeImage = false)
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
            expectNoEvents()
        }
    }

    private class Fixture {
        private val channelClient: ChannelClient = mock()
        private val chatClient: ChatClient = mock {
            on { channel(any<String>()) } doReturn channelClient
        }

        fun givenUploadImage(file: File, url: String) = apply {
            whenever(chatClient.uploadImage(file, null)) doReturn UploadedFile(file = url).asCall()
        }

        fun givenUploadImageError(file: File) = apply {
            val error = Error.GenericError(message = "Upload failed")
            whenever(chatClient.uploadImage(file, null)) doReturn error.asCall()
        }

        fun givenUpdatePartial() = apply {
            whenever(channelClient.updatePartial(any(), any())) doReturn mock<Channel>().asCall()
        }

        fun givenUpdatePartialError() = apply {
            val error = Error.GenericError(message = "Update failed")
            whenever(channelClient.updatePartial(any(), any())) doReturn error.asCall()
        }

        fun verifyUpdatePartial(expected: Map<String, Any>) {
            verify(channelClient).updatePartial(set = expected, unset = emptyList())
        }

        fun verifyUpdatePartialNeverCalled() {
            verify(channelClient, never()).updatePartial(any(), any())
        }

        fun verifyUploadImageNeverCalled() {
            verify(chatClient, never()).uploadImage(any(), any())
        }

        fun get() = GroupChannelEditViewModel(
            cid = randomCID(),
            chatClient = chatClient,
        )
    }
}
