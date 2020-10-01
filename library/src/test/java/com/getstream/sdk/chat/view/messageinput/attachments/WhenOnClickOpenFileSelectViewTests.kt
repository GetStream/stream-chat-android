package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.createAttachmentMetaDataWithAttachment
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.junit.jupiter.api.Test

internal class WhenOnClickOpenFileSelectViewTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If file uri is null Should show message`() {
        sut.onClickOpenFileSelectView(mock(), null)

        verify(view).showMessage(R.string.stream_permissions_storage_message)
    }

    @Test
    fun `If file uri is not null Should show loading total attachments`() {
        sut.onClickOpenFileSelectView(mock(), mock())

        verify(view).showLoadingTotalAttachments(true)
    }

    @Test
    fun `If file uri is not null and file attachments are empty Should show message and hide attachments menu`() {
        When calling storageHelper.getFileAttachments(any(), any()) doReturn emptyList()
        sut.onClickOpenFileSelectView(mock(), mock())

        verify(view).showMessage(R.string.stream_no_media_error)
        verify(view).hideAttachmentsMenu()
    }

    @Test
    fun `If file uri is not null and file attachments are non empty Should show file total attachments and empty selected attachments`() {
        When calling storageHelper.getFileAttachments(any(), any()) doReturn listOf(
            createAttachmentMetaDataWithAttachment(), createAttachmentMetaDataWithAttachment()
        )

        sut.onClickOpenFileSelectView(mock(), mock())

        verify(view).showTotalFileAttachments(argThat { itemCount == 2 })
        verify(view).showSelectedFileAttachments(argThat { count == 0 })
    }

    @Test
    fun `If file uri is not null and file attachments are non empty Should hide loading total attachments`() {
        When calling storageHelper.getFileAttachments(any(), any()) doReturn listOf(
            createAttachmentMetaDataWithAttachment(), createAttachmentMetaDataWithAttachment()
        )

        sut.onClickOpenFileSelectView(mock(), mock())

        verify(view).showLoadingTotalAttachments(false)
    }
}
