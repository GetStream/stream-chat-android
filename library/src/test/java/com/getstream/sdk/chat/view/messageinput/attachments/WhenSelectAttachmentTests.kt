package com.getstream.sdk.chat.view.messageinput.attachments

import android.net.Uri
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.createAttachment
import com.getstream.sdk.chat.createAttachmentMetaDataWithAttachment
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.randomLong
import com.getstream.sdk.chat.utils.Constant
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class WhenSelectAttachmentTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If attachment size is more than max upload file size Should show message`() {
        val attachment = createAttachmentMetaDataWithAttachment().apply {
            size = Constant.MAX_UPLOAD_FILE_SIZE + abs(randomLong())
        }

        sut.selectAttachment(attachment)

        verify(view).showMessage(R.string.stream_large_size_file_error)
    }

    @Test
    fun `Should add attachment to selected attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment()

        sut.selectAttachment(attachment)

        sut.selectedAttachments.contains(attachment) shouldBeEqualTo true
    }

    @Test
    fun `If media attachment Should show media attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        sut.selectAttachment(attachment)

        verify(view).showMediaAttachments()
    }

    @Test
    fun `If non media attachment Should show file attachments`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))

        sut.selectAttachment(attachment)

        verify(view).showFileAttachments()
    }

    @Test
    fun `Should config send button enable state to message input controller`() {
        sut.selectAttachment(mock())

        verify(messageInputController).configSendButtonEnableState()
    }

    @Test
    fun `If media attachment Should add attachments to selected media attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))

        sut.selectAttachment(attachment)

        verify(selectedMediaAttachmentAdapter).addAttachment(attachment)
    }

    @Test
    fun `If media attachment and total media attachments already shown and contains attachment Should change selection state`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        val sut = Fixture().givenMediaAttachmentsState(listOf(attachment)).please()

        sut.selectAttachment(attachment)

        verify(totalMediaAttachmentAdapter).selectAttachment(attachment)
    }

    @Test
    fun `If is not media attachmentShould add attachments to selected file attachments adapter`() {
        val attachment = createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))

        sut.selectAttachment(attachment)

        verify(selectedFileAttachmentAdapter).setAttachments(argThat { contains(attachment) })
    }

    @Test
    fun `Should clear media selected attachments when setting non media adapter`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.setSelectedAttachmentAdapter(null, false)

        sut.selectedAttachments.isEmpty() shouldBe true
    }

    @Test
    fun `Should not clear selected attachments when setting adapter of the same type`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.setSelectedAttachmentAdapter(null, true)

        sut.selectedAttachments.isEmpty() shouldBe false
    }

    @Test
    fun `Should set already selected non media attachments when setting non media adapter`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.setSelectedAttachmentAdapter(null, false)

        verify(selectedFileAttachmentAdapter).setAttachments(argThat { contains(attachment) })
    }

    @Test
    fun `Should show selected file attachments when setting non media adapter`() {
        sut.setSelectedAttachmentAdapter(null, false)

        verify(view).showSelectedFileAttachments(selectedFileAttachmentAdapter)
    }

    @Test
    fun `Should clear media adapter when setting non media adapter`() {
        sut.setSelectedAttachmentAdapter(null, false)

        verify(selectedMediaAttachmentAdapter).clear()
    }

    @Test
    fun `Should clear non media selected attachments when setting media adapter`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.setSelectedAttachmentAdapter(null, true)

        sut.selectedAttachments.isEmpty() shouldBe true
    }

    @Test
    fun `Should set already selected media attachments when setting media adapter`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_image))
        val sut = Fixture().givenSelectedAttachmentsState(setOf(attachment)).please()

        sut.setSelectedAttachmentAdapter(null, true)

        verify(selectedMediaAttachmentAdapter).setAttachments(argThat { contains(attachment) })
    }

    @Test
    fun `Should show selected media attachments when setting media adapter`() {
        sut.setSelectedAttachmentAdapter(null, true)

        verify(view).showSelectedMediaAttachments(selectedMediaAttachmentAdapter)
    }

    @Test
    fun `Should clear files adapter when setting media adapter`() {
        sut.setSelectedAttachmentAdapter(null, true)

        verify(selectedFileAttachmentAdapter).clear()
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
    fun `Should csomething when selecting attachments from Uri list`() {
        val attachment =
            createAttachmentMetaDataWithAttachment(attachment = createAttachment(type = ModelType.attach_file))
        val sut = Fixture().givenAttachmentsFromUriState(listOf(attachment)).please()
        val list = listOf(Uri.parse(""))
        sut.selectAttachmentsFromUriList(list)

        verify(storageHelper).getAttachmentsFromUriList(any(), eq(list))
    }

    private inner class Fixture {

        private val attachmentsController = this@WhenSelectAttachmentTests.sut

        fun givenMediaAttachmentsState(totalMediaAttachments: List<AttachmentMetaData>): Fixture {
            When calling storageHelper.getMediaAttachments(any()) doReturn totalMediaAttachments
            When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn true
            attachmentsController.onClickOpenMediaSelectView(mock())
            return this
        }

        fun givenAttachmentsFromUriState(fileAttachments: List<AttachmentMetaData>): Fixture {
            When calling storageHelper.getAttachmentsFromUriList(
                any(),
                any()
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
