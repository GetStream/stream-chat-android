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

package io.getstream.chat.android.compose.viewmodel

import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class AttachmentsPickerViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `Given images on the file system When showing attachments picker Should show available images`() {
        val storageHelperWrapper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(
                AttachmentMetaData(
                    mimeType = "image/jpeg",
                    title = "img_1.jpeg",
                    type = "image"
                ),
                AttachmentMetaData(
                    mimeType = "image/png",
                    title = "img_2.png",
                    type = "image"
                )
            )
        }
        val viewModel = AttachmentsPickerViewModel(storageHelperWrapper)

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
        val storageHelperWrapper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn listOf(
                AttachmentMetaData(
                    mimeType = "application/pdf",
                    title = "pdf_1.pdf",
                    type = "file"
                ),
                AttachmentMetaData(
                    mimeType = "application/pdf",
                    title = "pdf_2.pdf",
                    type = "file"
                )
            )
        }
        val viewModel = AttachmentsPickerViewModel(storageHelperWrapper)

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
    fun `Given images on the file system When showing attachments picker and selecting an image Should show the selected image`() {
        val attachmentMetaData = AttachmentMetaData(
            mimeType = "image/jpeg",
            title = "img_1.jpeg",
            type = "image"
        )
        val storageHelperWrapper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn listOf(attachmentMetaData)
            whenever(it.getAttachmentsForUpload(any())) doReturn listOf(Attachment(type = "image"))
        }
        val viewModel = AttachmentsPickerViewModel(storageHelperWrapper)

        viewModel.changeAttachmentState(true)
        viewModel.loadData()
        viewModel.changeSelectedAttachments(AttachmentPickerItemState(attachmentMetaData, isSelected = false))

        viewModel.isShowingAttachments `should be equal to` true
        viewModel.attachmentsPickerMode `should be equal to` Images
        viewModel.images.size `should be equal to` 1
        viewModel.files.size `should be equal to` 0
        viewModel.hasPickedImages `should be equal to` true
        viewModel.hasPickedFiles `should be equal to` false
        viewModel.getSelectedAttachments().size `should be equal to` 1
    }
}
