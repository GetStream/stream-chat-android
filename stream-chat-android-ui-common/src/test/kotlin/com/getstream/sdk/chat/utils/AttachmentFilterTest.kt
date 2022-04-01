package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.FileUploadConfig
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AttachmentFilterTest {

    private val chatClient: ChatClient = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `Given empty upload config When filtering attachments Should return all attachments`() {
        val fileUploadConfig = FileUploadConfig(
            allowedFileExtensions = emptyList(),
            allowedMimeTypes = emptyList(),
            blockedFileExtensions = emptyList(),
            blockedMimeTypes = emptyList()
        )
        whenever(chatClient.getAppSettings().app.fileUploadConfig) doReturn fileUploadConfig
        val attachmentFilter = AttachmentFilter(chatClient)

        val filteredAttachments = attachmentFilter.filterFileAttachments(attachments)

        filteredAttachments.size `should be equal to` 2
    }

    @Test
    fun `Given upload config with allowed lists matching attachments When filtering attachments Should return all attachments`() {
        val fileUploadConfig = FileUploadConfig(
            allowedFileExtensions = listOf(".jpg"),
            allowedMimeTypes = listOf("image/png"),
            blockedFileExtensions = emptyList(),
            blockedMimeTypes = emptyList()
        )
        whenever(chatClient.getAppSettings().app.fileUploadConfig) doReturn fileUploadConfig
        val attachmentFilter = AttachmentFilter(chatClient)

        val filteredAttachments = attachmentFilter.filterFileAttachments(attachments)

        filteredAttachments.size `should be equal to` 2
    }

    @Test
    fun `Given upload config with allowed lists not matching attachments When filtering attachments Should return no attachments`() {
        val fileUploadConfig = FileUploadConfig(
            allowedFileExtensions = listOf(".doc"),
            allowedMimeTypes = listOf("video/mp4"),
            blockedFileExtensions = emptyList(),
            blockedMimeTypes = emptyList()
        )
        whenever(chatClient.getAppSettings().app.fileUploadConfig) doReturn fileUploadConfig
        val attachmentFilter = AttachmentFilter(chatClient)

        val filteredAttachments = attachmentFilter.filterFileAttachments(attachments)

        filteredAttachments.size `should be equal to` 0
    }

    @Test
    fun `Given upload config with block lists matching attachments When filtering attachments Should return no attachments`() {
        val fileUploadConfig = FileUploadConfig(
            allowedFileExtensions = emptyList(),
            allowedMimeTypes = emptyList(),
            blockedFileExtensions = listOf(".png"),
            blockedMimeTypes = listOf("image/jpeg")
        )
        whenever(chatClient.getAppSettings().app.fileUploadConfig) doReturn fileUploadConfig
        val attachmentFilter = AttachmentFilter(chatClient)

        val filteredAttachments = attachmentFilter.filterFileAttachments(attachments)

        filteredAttachments.size `should be equal to` 0
    }

    private companion object {
        private val attachments = listOf(
            AttachmentMetaData(
                mimeType = "image/jpeg",
                title = "IMG_123.jpg"
            ),
            AttachmentMetaData(
                mimeType = "image/png",
                title = "IMG_123.png"
            )
        )
    }
}
