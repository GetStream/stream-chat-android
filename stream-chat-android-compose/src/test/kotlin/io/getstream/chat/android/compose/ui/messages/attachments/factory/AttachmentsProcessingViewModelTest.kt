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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.net.Uri
import androidx.lifecycle.ViewModel
import app.cash.turbine.test
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class AttachmentsProcessingViewModelTest {

    @Test
    fun `Given URIs When processing attachments Should emit result with processed metadata`() = runTest {
        val uri1 = mock<Uri>()
        val uri2 = mock<Uri>()
        val uris = listOf(uri1, uri2)
        val expectedMetadata = listOf(
            AttachmentMetaData(
                type = "image",
                mimeType = "image/jpeg",
                title = "photo.jpg",
            ),
            AttachmentMetaData(
                type = "image",
                mimeType = "image/png",
                title = "screenshot.png",
            ),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getAttachmentsMetadataFromUris(uris)) doReturn expectedMetadata
        }
        val viewModel = AttachmentsProcessingViewModel(storageHelper)

        viewModel.attachmentsMetadataFromUris.test {
            viewModel.getAttachmentsMetadataFromUrisAsync(uris)
            advanceUntilIdle()

            val result = awaitItem()
            assertEquals(uris, result.uris)
            assertEquals(expectedMetadata, result.attachmentsMetadata)
            assertEquals(2, result.attachmentsMetadata.size)
        }
    }

    @Test
    fun `Given files When getting files async Should emit files metadata`() = runTest {
        val expectedFilesMetadata = listOf(
            AttachmentMetaData(
                type = "file",
                mimeType = "application/pdf",
                title = "document.pdf",
            ),
            AttachmentMetaData(
                type = "file",
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                title = "report.docx",
            ),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getFiles()) doReturn expectedFilesMetadata
        }
        val viewModel = AttachmentsProcessingViewModel(storageHelper)

        viewModel.filesMetadata.test {
            viewModel.getFilesAsync()
            advanceUntilIdle()

            val result = awaitItem()
            assertEquals(expectedFilesMetadata, result)
            assertEquals(2, result.size)
        }
    }

    @Test
    fun `Given media When getting media async Should emit media metadata`() = runTest {
        val expectedMediaMetadata = listOf(
            AttachmentMetaData(
                type = "image",
                mimeType = "image/jpeg",
                title = "photo1.jpg",
            ),
            AttachmentMetaData(
                type = "video",
                mimeType = "video/mp4",
                title = "video1.mp4",
            ),
            AttachmentMetaData(
                type = "image",
                mimeType = "image/png",
                title = "screenshot.png",
            ),
        )
        val storageHelper: StorageHelperWrapper = mock {
            whenever(it.getMedia()) doReturn expectedMediaMetadata
        }
        val viewModel = AttachmentsProcessingViewModel(storageHelper)

        viewModel.mediaMetadata.test {
            viewModel.getMediaAsync()
            advanceUntilIdle()

            val result = awaitItem()
            assertEquals(expectedMediaMetadata, result)
            assertEquals(3, result.size)
        }
    }
}

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class AttachmentsProcessingViewModelFactoryTest {

    @Test
    fun `create should return correct AttachmentsProcessingViewModel instance`() {
        val storageHelper: StorageHelperWrapper = mock()
        val factory = AttachmentsProcessingViewModelFactory(storageHelper)

        val viewModel = factory.create(AttachmentsProcessingViewModel::class.java)

        assertInstanceOf(AttachmentsProcessingViewModel::class.java, viewModel)
    }

    @Test
    fun `create should throw IllegalArgumentException for unsupported ViewModel class`() {
        val storageHelper: StorageHelperWrapper = mock()
        val factory = AttachmentsProcessingViewModelFactory(storageHelper)

        val exception = assertThrows<IllegalArgumentException> {
            factory.create(ViewModel::class.java)
        }

        assertEquals(
            "AttachmentsProcessingViewModelFactory can only create instances of AttachmentsProcessingViewModel",
            exception.message,
        )
    }
}
