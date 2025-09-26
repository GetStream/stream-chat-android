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

package io.getstream.chat.android.compose.viewmodel.channel

import android.net.Uri
import androidx.core.net.toUri
import app.cash.turbine.test
import io.getstream.chat.android.compose.util.AttachmentFileController
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomIntBetween
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelMediaAttachmentsPreviewViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val sut = Fixture().get()

        val actual = sut.state.value

        assertFalse(actual.isPreparingToShare)
        assertNull(actual.promptedAttachment)
    }

    @Test
    fun `onViewAction ShareClick with small file, should share it directly`() = runTest {
        val attachment = randomAttachment(
            // Less than 10MB
            fileSize = positiveRandomInt(maxInt = (10 * 1024 * 1024) - 1),
        )
        val localUri = randomFile().toUri()
        val sut = Fixture()
            .givenDownloadFileResult(attachment, result = Result.Success(localUri))
            .get()

        sut.events.test {
            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

            val event = awaitItem()
            assertInstanceOf<ChannelMediaAttachmentsPreviewViewEvent.ShareLocalFile>(event)
            assertEquals(event.uri, localUri)
            assertEquals(event.mimeType, attachment.mimeType)
            assertEquals(event.text, attachment.title)

            val endState = sut.state.value
            assertFalse(endState.isPreparingToShare)
            assertNull(endState.promptedAttachment)
        }
    }

    @Test
    fun `onViewAction ShareClick with large file not in cache, should set promptedAttachment`() = runTest {
        val attachment = randomAttachment(
            // Greater than or equal to 10MB
            fileSize = randomIntBetween(
                min = 10 * 1024 * 1024,
                max = Int.MAX_VALUE - 1,
            ),
        )
        val sut = Fixture()
            .givenCacheFileResult(
                attachment,
                result = Result.Failure(Error.GenericError(message = randomString())),
            )
            .get()

        sut.events.test {
            sut.state.test {
                sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

                val startState = awaitItem()
                assertFalse(startState.isPreparingToShare)
                assertNull(startState.promptedAttachment)

                val endState = awaitItem()
                assertFalse(endState.isPreparingToShare)
                assertEquals(attachment, endState.promptedAttachment)
            }

            expectNoEvents()
        }
    }

    @Test
    fun `onViewAction ShareClick with large file in cache, should share it directly`() = runTest {
        val attachment = randomAttachment(
            // Greater than or equal to 10MB
            fileSize = randomIntBetween(
                min = 10 * 1024 * 1024,
                max = Int.MAX_VALUE - 1,
            ),
        )
        val localUri = randomFile().toUri()
        val sut = Fixture()
            .givenCacheFileResult(attachment, result = Result.Success(localUri))
            .get()

        sut.events.test {
            sut.state.test {
                sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

                val state = awaitItem()
                assertFalse(state.isPreparingToShare)
                assertNull(state.promptedAttachment)
            }

            val event = awaitItem()
            assertInstanceOf<ChannelMediaAttachmentsPreviewViewEvent.ShareLocalFile>(event)
            assertEquals(event.uri, localUri)
            assertEquals(event.mimeType, attachment.mimeType)
            assertEquals(event.text, attachment.title)
        }
    }

    @Test
    fun `onViewAction ShareClick when already preparing, should cancel sharing`() = runTest {
        val attachment = randomAttachment(
            // Less than 10MB
            fileSize = positiveRandomInt(maxInt = (10 * 1024 * 1024) - 1),
        )
        val localUri = randomFile().toUri()
        val sut = Fixture()
            .givenDownloadFileResult(attachment, result = Result.Success(localUri), delayInMillis = Long.MAX_VALUE)
            .get()

        sut.state.test {
            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

            val startState = awaitItem()
            assertFalse(startState.isPreparingToShare)
            assertNull(startState.promptedAttachment)

            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

            val progressState = awaitItem()
            assertTrue(progressState.isPreparingToShare)
            assertNull(progressState.promptedAttachment)

            val endState = awaitItem()
            assertFalse(endState.isPreparingToShare)
            assertNull(endState.promptedAttachment)
        }
    }

    @Test
    fun `onViewAction ConfirmSharingClick, should trigger share`() = runTest {
        val attachment = randomAttachment()
        val localUri = randomFile().toUri()
        val sut = Fixture()
            .givenDownloadFileResult(attachment, result = Result.Success(localUri))
            .get()

        sut.state.test {
            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ConfirmSharingClick(attachment))

            val startState = awaitItem()
            assertFalse(startState.isPreparingToShare)
            assertNull(startState.promptedAttachment)

            val progressState = awaitItem()
            assertTrue(progressState.isPreparingToShare)
            assertNull(progressState.promptedAttachment)

            val endState = awaitItem()
            assertFalse(endState.isPreparingToShare)
            assertNull(endState.promptedAttachment)
        }
    }

    @Test
    fun `onViewAction DismissSharingClick, should clear promptedAttachment`() = runTest {
        val attachment = randomAttachment(
            // Greater than or equal to 10MB
            fileSize = randomIntBetween(
                min = 10 * 1024 * 1024,
                max = Int.MAX_VALUE - 1,
            ),
        )
        val sut = Fixture()
            .givenCacheFileResult(
                attachment,
                result = Result.Failure(Error.GenericError(message = randomString())),
            )
            .get()

        sut.state.test {
            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

            val startState = awaitItem()
            assertFalse(startState.isPreparingToShare)
            assertNull(startState.promptedAttachment)

            val promptedState = awaitItem()
            assertFalse(promptedState.isPreparingToShare)
            assertEquals(attachment, promptedState.promptedAttachment)

            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.DismissSharingClick)

            val endState = awaitItem()
            assertFalse(endState.isPreparingToShare)
            assertNull(endState.promptedAttachment)
        }
    }

    @Test
    fun `share emits SharingError on failure`() = runTest {
        val attachment = randomAttachment(
            // Less than 10MB
            fileSize = positiveRandomInt(maxInt = (10 * 1024 * 1024) - 1),
        )
        val shareError = Error.GenericError(message = randomString())
        val sut = Fixture()
            .givenDownloadFileResult(attachment, result = Result.Failure(shareError))
            .get()

        sut.events.test {
            sut.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))

            val event = awaitItem()
            assertInstanceOf<ChannelMediaAttachmentsPreviewViewEvent.SharingError>(event)
            assertEquals(event.error, shareError)

            val endState = sut.state.value
            assertFalse(endState.isPreparingToShare)
            assertNull(endState.promptedAttachment)
        }
    }

    private class Fixture {
        private val attachmentFileController: AttachmentFileController = mock()

        suspend fun givenDownloadFileResult(
            attachment: Attachment,
            result: Result<Uri>,
            delayInMillis: Long = 0,
        ) = apply {
            whenever(attachmentFileController.downloadFile(attachment)) doSuspendableAnswer {
                delay(delayInMillis)
                result
            }
        }

        suspend fun givenCacheFileResult(
            attachment: Attachment,
            result: Result<Uri>,
        ) = apply {
            whenever(attachmentFileController.getFileFromCache(attachment)) doReturn result
        }

        fun get() = ChannelMediaAttachmentsPreviewViewModel(
            attachmentFileController = attachmentFileController,
        )
    }
}
