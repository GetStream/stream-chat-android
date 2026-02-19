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
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertInstanceOf
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

    private val storageHelper: AttachmentStorageHelper = mock()
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
        assertNull(viewModel.pickerMode)
        assertEquals(2, viewModel.attachments.size)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given files on the file system When showing attachments picker Should show available files`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(
            AttachmentPickerItemState(fileAttachment1),
            AttachmentPickerItemState(fileAttachment2),
        )

        assertTrue(viewModel.isShowingAttachments)
        assertInstanceOf<FilePickerMode>(viewModel.pickerMode)
        assertEquals(2, viewModel.attachments.size)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given images on the file system When selecting an image Should show the selection`() {
        whenever(storageHelper.toAttachments(any())) doReturn listOf(Attachment(type = "image"))
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        assertTrue(viewModel.isShowingAttachments)
        assertNull(viewModel.pickerMode)
        assertEquals(2, viewModel.attachments.size)
        assertEquals(1, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given selected images When deselecting earlier item Should update selection`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        viewModel.changeSelectedAttachments(viewModel.attachments.last())
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        assertFalse(viewModel.attachments.first().isSelected)
        assertTrue(viewModel.attachments.last().isSelected)
    }

    @Test
    fun `Given files When showing and hiding attachments picker Should reset the picker state`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(FilePickerMode())
        viewModel.changeAttachmentState(false)

        assertFalse(viewModel.isShowingAttachments)
        assertNull(viewModel.pickerMode)
        assertEquals(0, viewModel.attachments.size)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `When changing picker mode Should not load attachments`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changePickerMode(FilePickerMode())

        assertInstanceOf<FilePickerMode>(viewModel.pickerMode)
        assertEquals(0, viewModel.attachments.size)
        verify(storageHelper, never()).getFileMetadata()
        verify(storageHelper, never()).getMediaMetadata()
    }

    @Test
    fun `Given hidden attachment picker When create the view model Should not load any files`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        assertFalse(viewModel.isShowingAttachments)
        verify(storageHelper, never()).getFileMetadata()
        verify(storageHelper, never()).getMediaMetadata()
    }

    @Test
    fun `Given selected attachments When getting selected attachments Should map metadata for preview`() {
        val expectedAttachments = listOf(Attachment(type = "image"))
        whenever(storageHelper.toAttachments(any())) doReturn expectedAttachments
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
        whenever(storageHelper.toAttachments(metadata)) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        val result = viewModel.getAttachmentsFromMetaData(metadata)

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
    fun `Given multiple selected attachments When removing first Should unselect it`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        viewModel.changeSelectedAttachments(viewModel.attachments.last())

        viewModel.removeSelectedAttachment(attachmentWithSourceUri(imageUri1))

        assertFalse(viewModel.attachments.first().isSelected)
        assertTrue(viewModel.attachments.last().isSelected)
    }

    @Test
    fun `Given single selection mode When selecting another item Should replace selection`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )

        viewModel.changeSelectedAttachments(viewModel.attachments.first(), allowMultipleSelection = false)
        viewModel.changeSelectedAttachments(viewModel.attachments.last(), allowMultipleSelection = false)

        assertFalse(viewModel.attachments.first().isSelected)
        assertTrue(viewModel.attachments.last().isSelected)
    }

    @Test
    fun `Given single selection mode When clicking selected item Should keep it selected`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))

        viewModel.changeSelectedAttachments(viewModel.attachments.first(), allowMultipleSelection = false)
        viewModel.changeSelectedAttachments(viewModel.attachments.first(), allowMultipleSelection = false)

        assertTrue(viewModel.attachments.first().isSelected)
    }

    @Test
    fun `Given selected images When switching to files tab Should preserve image selections`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(fileAttachment1))

        viewModel.changePickerMode(GalleryPickerMode())
        assertTrue(viewModel.attachments.first().isSelected)
    }

    @Test
    fun `Given selections in both tabs When getting selected attachments Should combine from all tabs`() {
        whenever(storageHelper.toAttachments(any())) doReturn listOf(
            Attachment(type = "image"),
            Attachment(type = "file"),
        )
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(fileAttachment1))
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        val result = viewModel.getSelectedAttachments()
        assertEquals(2, result.size)
    }

    @Test
    fun `Given onAttachmentsLoaded with existing selections Should restore selections`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.onAttachmentsLoaded(
            listOf(AttachmentPickerItemState(imageAttachment1), AttachmentPickerItemState(imageAttachment2)),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.onAttachmentsLoaded(
            listOf(AttachmentPickerItemState(imageAttachment1), AttachmentPickerItemState(imageAttachment2)),
        )

        assertTrue(viewModel.attachments.first().isSelected)
        assertFalse(viewModel.attachments.last().isSelected)
    }

    @Test
    fun `Given selections across tabs When removing from other tab Should deselect it`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(fileAttachment1))

        viewModel.removeSelectedAttachment(attachmentWithSourceUri(imageUri1))

        viewModel.changePickerMode(GalleryPickerMode())
        assertFalse(viewModel.attachments.first().isSelected)
    }

    @Test
    fun `Given selections across tabs When dismissing picker Should clear all state`() {
        whenever(storageHelper.toAttachments(any())) doReturn emptyList()
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(fileAttachment1))
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changeAttachmentState(false)

        assertEquals(0, viewModel.attachments.size)
        viewModel.changePickerMode(FilePickerMode())
        assertEquals(0, viewModel.attachments.size)
        assertEquals(0, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given an image selected in media tab When loading files tab with same URI Should show it selected`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changePickerMode(FilePickerMode())
        viewModel.onAttachmentsLoaded(
            listOf(
                AttachmentPickerItemState(imageAttachment1),
                AttachmentPickerItemState(fileAttachment1),
            ),
        )

        assertTrue(viewModel.attachments.first().isSelected)
        assertFalse(viewModel.attachments.last().isSelected)
    }

    @Test
    fun `Given same file selected via media tab When selecting in media tab Should sync to files tab`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(fileAttachment1),
        )

        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changePickerMode(FilePickerMode())
        assertTrue(viewModel.attachments.first().isSelected)
    }

    @Test
    fun `Given same file selected in both tabs When deselecting in one tab Should deselect in both`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))

        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.changeSelectedAttachments(viewModel.attachments.first())
        assertFalse(viewModel.attachments.first().isSelected)
        viewModel.changePickerMode(FilePickerMode())
        assertFalse(viewModel.attachments.first().isSelected)
    }

    @Test
    fun `Given same file in both tabs When getting selected attachments Should not duplicate`() {
        whenever(storageHelper.toAttachments(any())) doReturn listOf(Attachment(type = "image"))
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))

        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        assertTrue(viewModel.attachments.first().isSelected)
        viewModel.changePickerMode(FilePickerMode())
        assertTrue(viewModel.attachments.first().isSelected)
        assertEquals(1, viewModel.getSelectedAttachments().size)
    }

    @Test
    fun `Given same file in both tabs When removing by URI Should deselect in both tabs`() {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        viewModel.changeAttachmentState(true)
        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))
        viewModel.changePickerMode(FilePickerMode())
        viewModel.attachments = listOf(AttachmentPickerItemState(imageAttachment1))

        viewModel.changePickerMode(GalleryPickerMode())
        viewModel.changeSelectedAttachments(viewModel.attachments.first())

        viewModel.removeSelectedAttachment(attachmentWithSourceUri(imageUri1))

        assertFalse(viewModel.attachments.first().isSelected)
        viewModel.changePickerMode(FilePickerMode())
        assertFalse(viewModel.attachments.first().isSelected)
    }

    @Test
    fun `mergeSelections with empty existing list Should return new items unchanged`() {
        val newItems = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )

        val result = AttachmentsPickerViewModel.mergeSelections(existing = emptyList(), newItems = newItems)

        assertEquals(newItems, result)
    }

    @Test
    fun `mergeSelections with existing selections Should restore matching selections`() {
        val existing = listOf(
            AttachmentPickerItemState(imageAttachment1, isSelected = true),
            AttachmentPickerItemState(imageAttachment2),
        )
        val newItems = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(imageAttachment2),
        )

        val result = AttachmentsPickerViewModel.mergeSelections(existing = existing, newItems = newItems)

        assertTrue(result.first().isSelected)
        assertFalse(result.last().isSelected)
    }

    @Test
    fun `applyCrossTabSelections Should mark items matching other tab as selected`() {
        val items = listOf(
            AttachmentPickerItemState(imageAttachment1),
            AttachmentPickerItemState(fileAttachment1),
        )
        val otherTab = listOf(
            AttachmentPickerItemState(imageAttachment1, isSelected = true),
        )

        val result = AttachmentsPickerViewModel.applyCrossTabSelections(items, otherTab)

        assertTrue(result.first().isSelected)
        assertFalse(result.last().isSelected)
    }

    @Test
    fun `loadAttachments loads file metadata for FilePickerMode`() = runTest {
        val expectedMetadata = listOf(fileAttachment1, fileAttachment2)
        whenever(storageHelper.getFileMetadata()) doReturn expectedMetadata
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)
        viewModel.changePickerMode(FilePickerMode())

        viewModel.loadAttachments()
        advanceUntilIdle()

        assertEquals(expectedMetadata.size, viewModel.attachments.size)
    }

    @Test
    fun `loadAttachments loads media metadata for GalleryPickerMode`() = runTest {
        val expectedMetadata = listOf(imageAttachment1, imageAttachment2)
        whenever(storageHelper.getMediaMetadata()) doReturn expectedMetadata
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)
        viewModel.changePickerMode(GalleryPickerMode())

        viewModel.loadAttachments()
        advanceUntilIdle()

        assertEquals(expectedMetadata.size, viewModel.attachments.size)
    }

    @Test
    fun `resolveAndSubmitUris emits resolved attachments`() = runTest {
        val uris = listOf(imageUri1, imageUri2)
        val metadata = listOf(imageAttachment1, imageAttachment2)
        val expectedAttachments = uris.map(::attachmentWithSourceUri)
        whenever(storageHelper.resolveMetadata(uris)) doReturn metadata
        whenever(storageHelper.toAttachments(metadata)) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        val results = mutableListOf<SubmittedAttachments>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.submittedAttachments.collect(results::add)
        }
        viewModel.resolveAndSubmitUris(uris)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, results.size)
        assertEquals(expectedAttachments, results.first().attachments)
        assertFalse(results.first().hasUnsupportedFiles)
    }

    @Test
    fun `resolveAndSubmitUris sets hasUnsupportedFiles when URIs are filtered`() = runTest {
        val uris = listOf(imageUri1, imageUri2)
        val filteredMetadata = listOf(imageAttachment1)
        val expectedAttachments = listOf(attachmentWithSourceUri(imageUri1))
        whenever(storageHelper.resolveMetadata(uris)) doReturn filteredMetadata
        whenever(storageHelper.toAttachments(filteredMetadata)) doReturn expectedAttachments
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        val results = mutableListOf<SubmittedAttachments>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.submittedAttachments.collect(results::add)
        }
        viewModel.resolveAndSubmitUris(uris)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, results.size)
        assertTrue(results.first().hasUnsupportedFiles)
    }

    @Test
    fun `resolveAndSubmitUris does nothing for empty URIs`() = runTest {
        val viewModel = AttachmentsPickerViewModel(storageHelper, channelState)

        val results = mutableListOf<SubmittedAttachments>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.submittedAttachments.collect(results::add)
        }
        viewModel.resolveAndSubmitUris(emptyList())
        advanceUntilIdle()
        job.cancel()

        assertTrue(results.isEmpty())
    }

    private fun mockChannelState(): ChannelState {
        val channel = randomChannel()
        return mock {
            on { channelData } doReturn MutableStateFlow(channel.toChannelData())
            on { channelConfig } doReturn MutableStateFlow(channel.config)
            on { toChannel() } doReturn channel
        }
    }

    private companion object {

        private val imageUri1 = Uri.parse("content://media/external/images/1")
        private val imageUri2 = Uri.parse("content://media/external/images/2")
        private val fileUri1 = Uri.parse("content://media/external/files/100")
        private val fileUri2 = Uri.parse("content://media/external/files/101")

        private val imageAttachment1 = AttachmentMetaData(
            uri = imageUri1,
            mimeType = "image/jpeg",
            title = "img_1.jpeg",
            type = "image",
        )
        private val imageAttachment2 = AttachmentMetaData(
            uri = imageUri2,
            mimeType = "image/png",
            title = "img_2.png",
            type = "image",
        )
        private val fileAttachment1 = AttachmentMetaData(
            uri = fileUri1,
            mimeType = "application/pdf",
            title = "pdf_1.pdf",
            type = "file",
        )
        private val fileAttachment2 = AttachmentMetaData(
            uri = fileUri2,
            mimeType = "application/pdf",
            title = "pdf_2.pdf",
            type = "file",
        )

        /** Creates an [Attachment] carrying [uri] in [EXTRA_SOURCE_URI], as produced by [AttachmentStorageHelper]. */
        private fun attachmentWithSourceUri(uri: Uri): Attachment =
            Attachment(extraData = mapOf(EXTRA_SOURCE_URI to uri.toString()))
    }
}
