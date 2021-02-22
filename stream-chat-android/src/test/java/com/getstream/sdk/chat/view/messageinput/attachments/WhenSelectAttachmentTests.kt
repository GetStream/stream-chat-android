package com.getstream.sdk.chat.view.messageinput.attachments

import android.net.Uri
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.createAttachment
import com.getstream.sdk.chat.createAttachmentMetaDataWithAttachment
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.test.randomLong
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class WhenSelectAttachmentTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If media attachment size is more than max upload file size Should show message`() {
        val attachment = createAttachmentMetaDataWithAttachment().apply {
            size = AttachmentConstants.MAX_UPLOAD_FILE_SIZE + abs(randomLong())
        }

        sut.selectMediaAttachment(attachment)

        verify(view).showMessage(R.string.stream_large_size_file_error)
    }

    @Test
    fun `Should add media attachment to selected attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment()

        sut.selectMediaAttachment(attachment)

        sut.selectedAttachments.contains(attachment) shouldBeEqualTo true
    }

    @Test
    fun `If media attachment Should show media attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        sut.selectMediaAttachment(attachment)

        verify(view).showMediaAttachments()
    }

    @Test
    fun `Should config send button enable state to message input controller`() {
        sut.selectMediaAttachment(mock())

        verify(messageInputController).configSendButtonEnableState()
    }

    @Test
    fun `If media attachment Should add attachments to selected media attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))

        sut.selectMediaAttachment(attachment)

        verify(selectedMediaAttachmentAdapter).addAttachment(attachment)
    }

    @Test
    fun `If media attachment and total media attachments already shown and contains attachment Should change selection state`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        val sut = Fixture().givenMediaAttachmentsState(listOf(attachment)).please()

        sut.selectMediaAttachment(attachment)

        verify(totalMediaAttachmentAdapter).selectAttachment(attachment)
    }

    @Test
    fun `Should show error message when at least one file attachment size is more than max upload file size`() {
        val attachment = createAttachmentMetaDataWithAttachment().apply {
            size = AttachmentConstants.MAX_UPLOAD_FILE_SIZE + abs(randomLong())
        }

        val sut = Fixture().givenAttachmentsFromUriState(listOf(attachment)).please()
        sut.selectAttachmentsFromUriList(listOf(Uri.EMPTY))

        verify(view).showMessage(R.string.stream_large_size_file_error)
    }

    @Test
    fun `Should clear media selected attachments when selecting attachments from Uri list`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.selectAttachmentsFromUriList(emptyList())

        sut.selectedAttachments.isEmpty() shouldBe true
    }

    @Test
    fun `Should set already selected non media attachments when selecting attachments from Uri list`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.selectAttachmentsFromUriList(emptyList())

        verify(selectedFileAttachmentAdapter).setAttachments(argThat { contains(attachment) })
    }

    @Test
    fun `Should show selected file attachments when selecting attachments from Uri list`() {
        sut.selectAttachmentsFromUriList(emptyList())

        verify(view).showSelectedFileAttachments(selectedFileAttachmentAdapter)
    }

    @Test
    fun `Should clear media adapter when selecting attachments from Uri list`() {
        sut.selectAttachmentsFromUriList(emptyList())

        verify(selectedMediaAttachmentAdapter).clear()
    }

    @Test
    fun `Should convert Uri list to attachments list when selecting attachments from Uri list`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))
        val sut = Fixture().givenAttachmentsFromUriState(listOf(attachment)).please()
        val list = listOf(Uri.EMPTY)
        sut.selectAttachmentsFromUriList(list)

        verify(storageHelper).getAttachmentsFromUriList(any(), eq(list))
    }

    @Test
    fun `Should show file attachment when selecting attachments from Uri list`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))
        val sut = Fixture().givenAttachmentsFromUriState(listOf(attachment)).please()
        sut.selectAttachmentsFromUriList(listOf(Uri.EMPTY))

        verify(view).showFileAttachments()
    }

    @Test
    fun `Should show error message when attachment from camera is too big`() {
        val attachment = createAttachmentMetaDataWithAttachment().apply {
            size = AttachmentConstants.MAX_UPLOAD_FILE_SIZE + abs(randomLong())
        }

        sut.selectAttachmentFromCamera(attachment)

        verify(view).showMessage(R.string.stream_large_size_file_error)
    }

    @Test
    fun `Should add media attachment to selected attachments when selecting attachment from camera`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))

        sut.selectAttachmentFromCamera(attachment)

        sut.selectedAttachments.contains(attachment) shouldBeEqualTo true
    }

    @Test
    fun `Should show media attachments when selecting attachment from camera`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))

        sut.selectAttachmentFromCamera(attachment)

        verify(view).showMediaAttachments()
    }

    private inner class Fixture {

        private val attachmentsController = this@WhenSelectAttachmentTests.sut

        fun givenMediaAttachmentsState(totalMediaAttachments: List<AttachmentMetaData>): Fixture {
            whenever(storageHelper.getMediaAttachments(any())) doReturn totalMediaAttachments
            whenever(permissionHelper.isGrantedStoragePermissions(any())) doReturn true
            attachmentsController.onClickOpenMediaSelectView(mock())
            return this
        }

        fun givenAttachmentsFromUriState(fileAttachments: List<AttachmentMetaData>): Fixture {
            whenever(
                storageHelper.getAttachmentsFromUriList(
                    any(),
                    any()
                )
            ) doReturn fileAttachments
            return this
        }

        fun givenSelectedAttachmentsState(selectedAttachments: Set<AttachmentMetaData>): Fixture {
            attachmentsController.setSelectedAttachments(selectedAttachments)
            return this
        }

        fun please() = attachmentsController
    }
}
