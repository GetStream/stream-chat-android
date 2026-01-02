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

package io.getstream.chat.android.compose.viewmodel.messages

import app.cash.turbine.test
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class AttachmentsPickerViewModelTest {

    private val channelState = MutableStateFlow(mock<ChannelState>())

    @Test
    fun `Given images on the file system When showing attachments picker Should show available images`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(imageAttachment1, imageAttachment2)
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.loadData()

        assertTrue(viewModel.isShowingAttachments)
        assertEquals(Images, viewModel.attachmentsPickerMode)
        assertEquals(2, viewModel.images.size)
        assertEquals(0, viewModel.files.size)
        assertFalse(viewModel.hasPickedImages)
        assertFalse(viewModel.hasPickedFiles)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given files on the file system When showing attachments picker Should show available files`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn listOf(fileAttachment1, fileAttachment2)
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)

        assertTrue(viewModel.isShowingAttachments)
        assertEquals(Files, viewModel.attachmentsPickerMode)
        assertEquals(0, viewModel.images.size)
        assertEquals(2, viewModel.files.size)
        assertFalse(viewModel.hasPickedImages)
        assertFalse(viewModel.hasPickedFiles)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given images on the file system When showing attachments picker and selecting an image Should show the selection`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(imageAttachment1, imageAttachment2)
            whenever(it.getAttachmentsForUpload(any())) doReturn listOf(Attachment(type = "image"))
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.loadData()
        viewModel.changeSelectedAttachments(viewModel.images.first())

        assertTrue(viewModel.isShowingAttachments)
        assertEquals(Images, viewModel.attachmentsPickerMode)
        assertEquals(2, viewModel.images.size)
        assertEquals(0, viewModel.files.size)
        assertTrue(viewModel.hasPickedImages)
        assertFalse(viewModel.hasPickedFiles)
        assertEquals(1, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given files on the file system When showing and hiding attachments picker Should reset the picker state`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn listOf(fileAttachment1, fileAttachment2)
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)
        viewModel.changeAttachmentState(false)

        assertFalse(viewModel.isShowingAttachments)
        assertEquals(Images, viewModel.attachmentsPickerMode)
        assertEquals(0, viewModel.images.size)
        assertEquals(0, viewModel.files.size)
        assertFalse(viewModel.hasPickedImages)
        assertFalse(viewModel.hasPickedFiles)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given hidden attachment picker When create the view model Should not load any files from the file system`() {
        val storageHelper: StorageHelperWrapper = mock()
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        assertFalse(viewModel.isShowingAttachments)
        verify(storageHelper, never()).getFiles()
        verify(storageHelper, never()).getMedia()
    }

    @Test
    fun `Given selected images When getting selected attachments async Should emit attachments for upload`() = runTest {
        val expectedAttachments = listOf(
            Attachment(type = "image", upload = mock()),
            Attachment(type = "image", upload = mock()),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(imageAttachment1, imageAttachment2)
            whenever(it.getAttachmentsForUpload(any())) doReturn expectedAttachments
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.attachmentsForUpload.test {
            viewModel.changeAttachmentState(true)
            viewModel.loadData()
            viewModel.changeSelectedAttachments(viewModel.images.first())
            viewModel.changeSelectedAttachments(viewModel.images.last())

            viewModel.getSelectedAttachmentsAsync()
            advanceUntilIdle()

            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(expectedAttachments, result)
        }
    }

    @Test
    fun `Given selected files When getting selected attachments async Should emit attachments for upload`() = runTest {
        val expectedAttachments = listOf(
            Attachment(type = "file", upload = mock()),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn listOf(fileAttachment1, fileAttachment2)
            whenever(it.getAttachmentsForUpload(any())) doReturn expectedAttachments
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.attachmentsForUpload.test {
            viewModel.changeAttachmentState(true)
            viewModel.changeAttachmentPickerMode(Files)
            viewModel.changeSelectedAttachments(viewModel.files.first())

            viewModel.getSelectedAttachmentsAsync()
            advanceUntilIdle()

            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(expectedAttachments, result)
        }
    }

    @Test
    fun `Given attachment metadata When getting attachments from metadata async Should emit attachments for upload`() = runTest {
        val metadata = listOf(imageAttachment1, imageAttachment2)
        val expectedAttachments = listOf(
            Attachment(type = "image", upload = mock()),
            Attachment(type = "image", upload = mock()),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getAttachmentsForUpload(metadata)) doReturn expectedAttachments
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.attachmentsForUpload.test {
            viewModel.getAttachmentsFromMetadataAsync(metadata)
            advanceUntilIdle()

            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(expectedAttachments, result)
        }
    }

    companion object {

        private val imageAttachment1 = AttachmentMetaData(
            mimeType = "image/jpeg",
            title = "img_1.jpeg",
            type = "image",
        )
        private val imageAttachment2 = AttachmentMetaData(
            mimeType = "image/png",
            title = "img_2.png",
            type = "image",
        )
        private val fileAttachment1 = AttachmentMetaData(
            mimeType = "application/pdf",
            title = "pdf_1.pdf",
            type = "file",
        )
        private val fileAttachment2 = AttachmentMetaData(
            mimeType = "application/pdf",
            title = "pdf_2.pdf",
            type = "file",
        )
    }
}
