package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.createAttachmentMetaDataWithAttachment
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.randomLong
import com.getstream.sdk.chat.utils.Constant
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class WhenSelectAttachmentTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If attachment size is more than max upload file size Should show message`() {
        val attachment = createAttachmentMetaDataWithAttachment().apply {
            size = Constant.MAX_UPLOAD_FILE_SIZE + abs(randomLong())
        }

        sut.selectAttachment(attachment, mock(), true)

        verify(view).showMessage(R.string.stream_large_size_file_error)
    }

    @Test
    fun `Should add attachment to selected attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment()

        sut.selectAttachment(attachment, mock(), true)

        sut.selectedAttachments.contains(attachment) shouldBeEqualTo true
    }

    @Test
    fun `If isMedia Should show media attachments`() {
        sut.selectAttachment(mock(), mock(), true)

        verify(view).showMediaAttachments()
    }

    @Test
    fun `If not isMedia Should show file attachments`() {
        sut.selectAttachment(mock(), mock(), false)

        verify(view).showFileAttachments()
    }

    @Test
    fun `Should config send button enable state to message input controller`() {
        sut.selectAttachment(mock(), mock(), true)

        verify(messageInputController).configSendButtonEnableState()
    }

    @Test
    fun `If isMedia Should add attachments to selected media attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment()

        sut.selectAttachment(attachment, mock(), true)

        sut.selectedMediaAttachmentAdapter?.selectedAttachments?.contains(attachment) shouldBeEqualTo true
    }

    @Test
    fun `If isMedia and total media attachments already shown and contains attachment Should change selection state`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture().givenMediaSelectMenuState(listOf(attachment)).please()

        try {
            sut.selectAttachment(attachment, mock(), true)
        } catch (e: NullPointerException) {
            // expected because adapter not attached to recycler
        }

        sut.totalMediaAttachmentAdapter?.attachments?.contains(attachment) shouldBeEqualTo true
        sut.totalMediaAttachmentAdapter?.attachments?.first()?.isSelected shouldBeEqualTo true
    }

    @Test
    fun `If is not media Should add attachments to selected file attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment()

        sut.selectAttachment(attachment, mock(), false)

        sut.selectedFileAttachmentAdapter?.attachments?.contains(attachment) shouldBeEqualTo true
    }

    @Test
    fun `If is not media and total file attachments already shown and contains attachment Should change selection state`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture().givenFileSelectMenuState(listOf(attachment)).please()

        try {
            sut.selectAttachment(attachment, mock(), true)
        } catch (e: NullPointerException) {
            // expected because adapter not attached to recycler
        }

        sut.totalMediaAttachmentAdapter?.attachments?.contains(attachment) shouldBeEqualTo true
        sut.totalMediaAttachmentAdapter?.attachments?.first()?.isSelected shouldBeEqualTo true
    }

    private inner class Fixture {

        private val attachmentsController = this@WhenSelectAttachmentTests.sut

        fun givenMediaSelectMenuState(totalMediaAttachments: List<AttachmentMetaData>): Fixture {
            When calling storageHelper.getMediaAttachments(any()) doReturn totalMediaAttachments
            When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true
            attachmentsController.onClickOpenMediaSelectView(mock())
            return this
        }

        fun givenFileSelectMenuState(totalFileAttachments: List<AttachmentMetaData>): Fixture {
            When calling storageHelper.getFileAttachments(any(), any()) doReturn totalFileAttachments
            attachmentsController.onClickOpenFileSelectView(mock(), mock())
            return this
        }

        fun please() = attachmentsController
    }
}
