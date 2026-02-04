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

import android.net.Uri
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState.Selection
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class AttachmentsPickerViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val storageHelper: StorageHelperWrapper = mock()
    private val channelState = MutableStateFlow(mockChannelState())

    @Test
    fun `Given images on the file system When showing attachments picker Should show available images`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )

        assertTrue(viewModel.isShowingAttachments)
        assertEquals(Images, viewModel.attachmentsPickerMode)
        assertEquals(2, viewModel.attachments.size)
        assertFalse(viewModel.hasPickedAttachments)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given files on the file system When showing attachments picker Should show available files`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(fileAttachment1),
            AttachmentPickerItemState(fileAttachment2),
        )

        assertTrue(viewModel.isShowingAttachments)
        assertEquals(Files, viewModel.attachmentsPickerMode)
        assertEquals(2, viewModel.attachments.size)
        assertFalse(viewModel.hasPickedAttachments)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given images on the file system When showing attachments picker and selecting an image Should show the selection`() {
        whenever(storageHelper.getAttachmentsForUpload(any())) doReturn listOf(Attachment(type = "image"))
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        assertTrue(viewModel.isShowingAttachments)
        assertEquals(Images, viewModel.attachmentsPickerMode)
        assertEquals(2, viewModel.attachments.size)
        assertTrue(viewModel.hasPickedAttachments)
        assertEquals(1, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given selected images When deselecting earlier item Should update selection order`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        viewModel.changeSelectedAttachments(viewModel.attachments.last())
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        val firstItem = viewModel.attachments.first()
        val lastItem = viewModel.attachments.last()
        assertFalse(firstItem.isSelected)
        assertEquals(Selection.Unselected, firstItem.selection)
        assertTrue(lastItem.isSelected)
        assertEquals(Selection.Selected(position = 1), lastItem.selection)
    }

    @Test
    fun `Given files on the file system When showing and hiding attachments picker Should reset the picker state`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)
        viewModel.changeAttachmentState(false)

        assertFalse(viewModel.isShowingAttachments)
        assertEquals(Images, viewModel.attachmentsPickerMode)
        assertEquals(0, viewModel.attachments.size)
        assertFalse(viewModel.hasPickedAttachments)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `When changing picker mode Should not load attachments`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentPickerMode(Files)

        assertEquals(Files, viewModel.attachmentsPickerMode)
        assertEquals(0, viewModel.attachments.size)
        verify(storageHelper, never()).getFiles()
        verify(storageHelper, never()).getMedia()
    }

    @Test
    fun `Given hidden attachment picker When create the view model Should not load any files from the file system`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        assertFalse(viewModel.isShowingAttachments)
        verify(storageHelper, never()).getFiles()
        verify(storageHelper, never()).getMedia()
    }

    @Test
    fun `Given selected images When getting selected attachments async Should invoke callback with attachments for upload`() = runTest {
        val expectedAttachments = listOf(
            Attachment(type = "image", upload = mock()),
            Attachment(type = "image", upload = mock()),
        )
        whenever(storageHelper.getAttachmentsForUpload(any())) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        viewModel.changeSelectedAttachments(viewModel.attachments.last())

        var result: List<Attachment>? = null
        viewModel.getSelectedAttachmentsAsync { result = it }
        advanceUntilIdle()

        assertEquals(2, result?.size)
        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `Given selected files When getting selected attachments async Should invoke callback with attachments for upload`() = runTest {
        val expectedAttachments = listOf(
            Attachment(type = "file", upload = mock()),
        )
        whenever(storageHelper.getAttachmentsForUpload(any())) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(fileAttachment1),
            AttachmentPickerItemState(fileAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        var result: List<Attachment>? = null
        viewModel.getSelectedAttachmentsAsync { result = it }
        advanceUntilIdle()

        assertEquals(1, result?.size)
        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `Given selected attachments When getting selected attachments Should map metadata for upload`() {
        val expectedAttachments = listOf(Attachment(type = "image", upload = mock()))
        whenever(storageHelper.getAttachmentsForUpload(any())) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        val result = viewModel.getSelectedAttachments()

        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `Given attachment metadata When getting attachments from metadata Should return attachments`() {
        val metadata = listOf(imageAttachment1, imageAttachment2)
        val expectedAttachments = listOf(
            Attachment(type = "image", upload = mock()),
            Attachment(type = "image", upload = mock()),
        )
        whenever(storageHelper.getAttachmentsForUpload(metadata)) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        val result = viewModel.getAttachmentsFromMetaData(metadata)

        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `Given attachment uris When getting attachments from uris Should return attachments`() {
        val uris = listOf(Uri.parse("file://test1.pdf"), Uri.parse("file://test2.pdf"))
        val expectedAttachments = listOf(
            Attachment(type = "file", upload = mock()),
            Attachment(type = "file", upload = mock()),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getAttachmentsFromUris(uris)) doReturn expectedAttachments
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        val result = viewModel.getAttachmentsFromUris(uris)

        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `Given attachment metadata When getting attachments from metadata async Should invoke callback with attachments for upload`() = runTest {
        val metadata = listOf(imageAttachment1, imageAttachment2)
        val expectedAttachments = listOf(
            Attachment(type = "image", upload = mock()),
            Attachment(type = "image", upload = mock()),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getAttachmentsForUpload(metadata)) doReturn expectedAttachments
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        var result: List<Attachment>? = null
        viewModel.getAttachmentsFromMetadataAsync(metadata) { result = it }
        advanceUntilIdle()

        assertEquals(2, result?.size)
        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `Should toggle the attachment state`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.toggleAttachmentState()
        assertTrue(viewModel.isShowingAttachments)

        viewModel.toggleAttachmentState()
        assertFalse(viewModel.isShowingAttachments)
    }

    @Test
    fun `Given multiple selected attachments When removing first Should unselect and update selection order`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        viewModel.changeSelectedAttachments(viewModel.attachments.last())

        viewModel.removeSelectedAttachment(Attachment(name = imageAttachment1.title))

        val firstItem = viewModel.attachments.first()
        val lastItem = viewModel.attachments.last()
        assertFalse(firstItem.isSelected)
        assertEquals(Selection.Unselected, firstItem.selection)
        assertTrue(lastItem.isSelected)
        assertEquals(Selection.Selected(position = 1), lastItem.selection)
    }

    private fun mockChannelState(): ChannelState {
        val channel = randomChannel()
        return mock {
            on { channelData } doReturn MutableStateFlow(channel.toChannelData())
            on { channelConfig } doReturn MutableStateFlow(channel.config)
            on { toChannel() } doReturn channel
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
