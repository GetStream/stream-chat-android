/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
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

    @Test
    fun `Given images on the file system When showing attachments picker Should show available images`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(imageAttachment1, imageAttachment2)
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper)

        viewModel.changeAttachmentState(true)
        viewModel.loadData()

        viewModel.isShowingAttachments `should be equal to` true
        viewModel.attachmentsPickerMode `should be equal to` Images
        viewModel.images.size `should be equal to` 2
        viewModel.files.size `should be equal to` 0
        viewModel.hasPickedImages `should be equal to` false
        viewModel.hasPickedFiles `should be equal to` false
        viewModel.getSelectedAttachments().size `should be equal to` 0
    }

    @Test
    fun `Given files on the file system When showing attachments picker Should show available files`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn listOf(fileAttachment1, fileAttachment2)
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)

        viewModel.isShowingAttachments `should be equal to` true
        viewModel.attachmentsPickerMode `should be equal to` Files
        viewModel.images.size `should be equal to` 0
        viewModel.files.size `should be equal to` 2
        viewModel.hasPickedImages `should be equal to` false
        viewModel.hasPickedFiles `should be equal to` false
        viewModel.getSelectedAttachments().size `should be equal to` 0
    }

    @Test
    fun `Given images on the file system When showing attachments picker and selecting an image Should show the selection`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(imageAttachment1, imageAttachment2)
            whenever(it.getAttachmentsForUpload(any())) doReturn listOf(Attachment(type = "image"))
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper)

        viewModel.changeAttachmentState(true)
        viewModel.loadData()
        viewModel.changeSelectedAttachments(viewModel.images.first())

        viewModel.isShowingAttachments `should be equal to` true
        viewModel.attachmentsPickerMode `should be equal to` Images
        viewModel.images.size `should be equal to` 2
        viewModel.files.size `should be equal to` 0
        viewModel.hasPickedImages `should be equal to` true
        viewModel.hasPickedFiles `should be equal to` false
        viewModel.getSelectedAttachments().size `should be equal to` 1
    }

    @Test
    fun `Given files on the file system When showing and hiding attachments picker Should reset the picker state`() {
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn listOf(fileAttachment1, fileAttachment2)
        }
        val viewModel = AttachmentsPickerViewModel(storageHelper)

        viewModel.changeAttachmentState(true)
        viewModel.changeAttachmentPickerMode(Files)
        viewModel.changeAttachmentState(false)

        viewModel.isShowingAttachments `should be equal to` false
        viewModel.attachmentsPickerMode `should be equal to` Images
        viewModel.images.size `should be equal to` 0
        viewModel.files.size `should be equal to` 0
        viewModel.hasPickedImages `should be equal to` false
        viewModel.hasPickedFiles `should be equal to` false
        viewModel.getSelectedAttachments().size `should be equal to` 0
    }

    @Test
    fun `Given hidden attachment picker When create the view model Should not load any files from the file system`() {
        val storageHelper: StorageHelperWrapper = mock()
        val viewModel = AttachmentsPickerViewModel(storageHelper)

        viewModel.isShowingAttachments `should be equal to` false
        verify(storageHelper, never()).getFiles()
        verify(storageHelper, never()).getMedia()
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
