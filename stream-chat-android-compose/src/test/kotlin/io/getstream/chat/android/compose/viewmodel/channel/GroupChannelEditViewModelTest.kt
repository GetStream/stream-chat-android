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

import android.net.Uri
import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupChannelEditViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle with no pending changes`() = runTest {
        val sut = Fixture().get()

        sut.state.test {
            val state = awaitItem()
            assertFalse(state.isSaving)
            assertFalse(state.isImporting)
            assertFalse(state.isBusy)
            assertNull(state.pendingImageFile)
            assertFalse(state.removeImage)
        }
    }

    @Test
    fun `gallery import toggles importing and busy flags`() = runTest {
        val sut = Fixture().get()

        sut.state.test {
            awaitItem()
            sut.importGalleryImage(Uri.parse("content://test"))
            val importing = awaitItem()
            assertTrue(importing.isImporting)
            assertTrue(importing.isBusy)
            val idle = awaitItem()
            assertFalse(idle.isImporting)
            assertFalse(idle.isBusy)
        }
    }

    @Test
    fun `gallery import stores pending image file in state`() = runTest {
        val sut = Fixture().get()

        sut.state.test {
            awaitItem()
            sut.importGalleryImage(Uri.parse("content://test"))
            skipItems(1) // importing
            val done = awaitItem()
            assertTrue(done.pendingImageFile?.exists() == true)
            assertFalse(done.removeImage)
        }
    }

    @Test
    fun `setPendingImage stores file and clears removeImage`() = runTest {
        val file = File.createTempFile("camera", ".jpg").also { it.deleteOnExit() }
        val sut = Fixture().get()

        sut.removeImage()
        sut.setPendingImage(file)

        sut.state.test {
            val state = awaitItem()
            assertEquals(file, state.pendingImageFile)
            assertFalse(state.removeImage)
        }
    }

    @Test
    fun `removeImage clears pending file and sets flag`() = runTest {
        val file = File.createTempFile("camera", ".jpg").also { it.deleteOnExit() }
        val sut = Fixture().get()

        sut.setPendingImage(file)
        sut.removeImage()

        sut.state.test {
            val state = awaitItem()
            assertNull(state.pendingImageFile)
            assertTrue(state.removeImage)
        }
    }

    @Test
    fun `save is ignored while gallery import is in progress`() = runTest {
        val copyEntered = CountDownLatch(1)
        val unblockCopy = CountDownLatch(1)
        val blockingCopier = GalleryImageCopier {
            copyEntered.countDown()
            unblockCopy.await()
            File.createTempFile("gallery", ".jpg").also { it.deleteOnExit() }
        }
        val fixture = Fixture(galleryImageCopier = blockingCopier).givenUpdatePartial()
        val sut = fixture.get()
        sut.importGalleryImage(Uri.parse("content://test"))

        assertTrue(copyEntered.await(5, TimeUnit.SECONDS))

        sut.events.test {
            sut.save(name = randomString())
            expectNoEvents()
        }
        unblockCopy.countDown()
        fixture.verifyUpdatePartialNeverCalled()
    }

    @Test
    fun `save with name only emits success`() = runTest {
        val name = randomString()
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = name)
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
        }
        fixture.verifyUpdatePartial(mapOf("name" to name))
    }

    @Test
    fun `save with name only does not call uploadImage`() = runTest {
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = randomString())
            awaitItem()
        }
        fixture.verifyUploadImageNeverCalled()
    }

    @Test
    fun `save with pending image uploads then updates channel`() = runTest {
        val name = randomString()
        val imageUrl = "https://cdn.example.com/uploaded.jpg"
        val imageFile = File.createTempFile("test", ".jpg")
        val fixture = Fixture()
            .givenUploadImage(imageFile, imageUrl)
            .givenUpdatePartial()
        val sut = fixture.get()

        sut.setPendingImage(imageFile)
        sut.events.test {
            sut.save(name = name)
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

        sut.removeImage()
        sut.events.test {
            sut.save(name = name)
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
        }
        fixture.verifyUpdatePartial(mapOf("name" to name, "image" to ""))
    }

    @Test
    fun `save emits error when upload fails`() = runTest {
        val imageFile = File.createTempFile("test", ".jpg")
        val fixture = Fixture().givenUploadImageError(imageFile)
        val sut = fixture.get()

        sut.setPendingImage(imageFile)
        sut.events.test {
            sut.save(name = randomString())
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
            sut.save(name = randomString())
            assertEquals(GroupChannelEditViewEvent.SaveError, awaitItem())
        }
    }

    @Test
    fun `isSaving is true during save and false after`() = runTest {
        val fixture = Fixture().givenUpdatePartial()
        val sut = fixture.get()

        sut.state.test {
            assertFalse(awaitItem().isSaving) // initial
            sut.save(name = randomString())
            assertTrue(awaitItem().isSaving) // saving
            assertFalse(awaitItem().isSaving) // done
        }
    }

    @Test
    fun `isSaving resets to false after upload error`() = runTest {
        val imageFile = File.createTempFile("test", ".jpg")
        val fixture = Fixture().givenUploadImageError(imageFile)
        val sut = fixture.get()

        sut.setPendingImage(imageFile)
        sut.state.test {
            skipItems(1) // initial + setPendingImage (conflated)
            sut.save(name = randomString())
            assertTrue(awaitItem().isSaving)
            assertFalse(awaitItem().isSaving)
        }
        imageFile.delete()
    }

    @Test
    fun `duplicate save is ignored while saving`() = runTest {
        val fixture = Fixture().givenUpdatePartialDelayed(this)
        val sut = fixture.get()

        sut.events.test {
            sut.save(name = "first")
            sut.save(name = "second")
            assertEquals(GroupChannelEditViewEvent.SaveSuccess, awaitItem())
            expectNoEvents()
        }
        fixture.verifyUpdatePartialCallCount(1)
    }

    private class Fixture(
        private val galleryImageCopier: GalleryImageCopier = GalleryImageCopier {
            File.createTempFile("gallery", ".jpg").also { it.deleteOnExit() }
        },
    ) {
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

        fun givenUpdatePartialDelayed(scope: CoroutineScope) = apply {
            whenever(channelClient.updatePartial(any(), any())) doReturn CoroutineCall(scope) {
                delay(100)
                Result.Success(mock())
            }
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

        fun verifyUpdatePartialCallCount(count: Int) {
            verify(channelClient, times(count)).updatePartial(any(), any())
        }

        fun verifyUploadImageNeverCalled() {
            verify(chatClient, never()).uploadImage(any(), any())
        }

        fun get() = GroupChannelEditViewModel(
            cid = randomCID(),
            galleryImageCopier = galleryImageCopier,
            chatClient = chatClient,
        )
    }
}
