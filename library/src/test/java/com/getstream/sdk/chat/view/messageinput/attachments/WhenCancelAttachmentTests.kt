package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.createAttachmentMetaDataWithAttachment
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.randomBoolean
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class WhenCancelAttachmentTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If attachments is selected before Should remove it from selected attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture()
            .givenMediaAttachmentsState(listOf(attachment))
            .givenMediaSelectedAttachment(attachment)
            .please()

        sut.cancelAttachment(attachment, mock(), randomBoolean())

        sut.selectedAttachments.contains(attachment) shouldBeEqualTo false
    }

    @Test
    fun `If isMedia and this attachments is selected before Should remove it from selected media attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture()
            .givenMediaAttachmentsState(listOf(attachment))
            .givenMediaSelectedAttachment(attachment)
            .please()

        sut.cancelAttachment(attachment, mock(), true)

        verify(selectedMediaAttachmentAdapter).removeAttachment(attachment)
    }

    @Test
    fun `If isMedia and this attachments is selected before Should unselect attachment to total media adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture()
            .givenMediaAttachmentsState(listOf(attachment))
            .givenMediaSelectedAttachment(attachment)
            .please()

        sut.cancelAttachment(attachment, mock(), true)

        verify(totalMediaAttachmentAdapter).unselectAttachment(attachment)
    }

    @Test
    fun `If is not media and this attachments is selected before Should set attachments list without it to attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture()
            .givenFileAttachmentsState(listOf(attachment))
            .givenFileSelectedAttachment(attachment)
            .please()
        reset(selectedFileAttachmentAdapter)

        sut.cancelAttachment(attachment, mock(), false)

        verify(selectedFileAttachmentAdapter).setAttachments(argThat { !contains(attachment) })
    }

    @Test
    fun `If is not media and this attachments is selected before Should unselect attachment to total file adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment()
        val sut = Fixture()
            .givenFileAttachmentsState(listOf(attachment))
            .givenFileSelectedAttachment(attachment)
            .please()

        sut.cancelAttachment(attachment, mock(), false)

        verify(totalFileAttachmentAdapter).unselectAttachment(attachment)
    }

    private inner class Fixture {
        private val attachmentsController = sut

        fun givenMediaAttachmentsState(totalMediaAttachments: List<AttachmentMetaData>): Fixture {
            When calling storageHelper.getMediaAttachments(any()) doReturn totalMediaAttachments
            When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true
            attachmentsController.onClickOpenMediaSelectView(mock())
            return this
        }

        fun givenMediaSelectedAttachment(selectedAttachment: AttachmentMetaData): Fixture {
            sut.selectAttachment(selectedAttachment, true)
            return this
        }

        fun givenFileAttachmentsState(totalFileAttachments: List<AttachmentMetaData>): Fixture {
            When calling storageHelper.getFileAttachments(any(), any()) doReturn totalFileAttachments
            attachmentsController.onClickOpenFileSelectView(mock(), mock())
            return this
        }

        fun givenFileSelectedAttachment(selectedAttachment: AttachmentMetaData): Fixture {
            sut.selectAttachment(selectedAttachment, false)
            return this
        }

        fun please() = attachmentsController
    }
}
