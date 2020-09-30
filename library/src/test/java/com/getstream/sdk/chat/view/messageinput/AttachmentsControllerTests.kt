package com.getstream.sdk.chat.view.messageinput

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AttachmentsControllerTests {

    private val messageInputController: MessageInputController = mock()
    private val view: MessageInputView = mock()

    private lateinit var sut: AttachmentsController

    @BeforeEach
    fun setup() {
        sut = AttachmentsController(messageInputController, view, true)
    }

    @Test
    fun `When click open attachment selection menu Should invoke show attachments menu to view`() {
        When calling view.context doReturn mock()

        sut.onClickOpenAttachmentSelectionMenu()

        verify(view).showAttachmentsMenu()
    }
}