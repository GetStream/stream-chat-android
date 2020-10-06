package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.createAttachmentMetaDataWithAttachment
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class WhenOnClickOpenMediaSelectViewTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If storage permission is not granted Should check storage permission to permission helper`() {
        When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn false

        sut.onClickOpenMediaSelectView(mock())

        verify(permissionHelper).checkStoragePermissions(any(), any(), any())
    }

    @Test
    fun `If storage permission is granted Should show loading state`() {
        When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true

        sut.onClickOpenMediaSelectView(mock())

        verify(view).showLoadingTotalAttachments(true)
    }

    @Test
    fun `If storage permission is granted and total media attachments are empty Should hide attachments menu and show empty message`() {
        When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true
        When calling storageHelper.getMediaAttachments(any()) doReturn emptyList()

        sut.onClickOpenMediaSelectView(mock())

        verify(view).hideAttachmentsMenu()
        verify(view).showMessage(R.string.stream_no_media_error)
    }

    @Test
    fun `If storage permission is granted and total media attachments are not empty Should show total media attachments And empty media selected attachments`() {
        When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true
        When calling storageHelper.getMediaAttachments(any()) doReturn listOf(
            createAttachmentMetaDataWithAttachment(),
            createAttachmentMetaDataWithAttachment()
        )

        sut.onClickOpenMediaSelectView(mock())

        verify(totalMediaAttachmentAdapter).setAttachments(argThat { size == 2 })
        verify(view).showTotalMediaAttachments(totalMediaAttachmentAdapter)
        verify(view).showSelectedMediaAttachments(selectedMediaAttachmentAdapter)
    }

    @Test
    fun `If storage permission is granted and total media attachments are not empty Should hide loading`() {
        When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true
        When calling storageHelper.getMediaAttachments(any()) doReturn listOf(
            createAttachmentMetaDataWithAttachment(),
            createAttachmentMetaDataWithAttachment()
        )

        sut.onClickOpenMediaSelectView(mock())

        verify(view).showLoadingTotalAttachments(false)
    }
}
