package com.getstream.sdk.chat.view.messageinput.attachments

import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test

internal class WhenOnClickCloseAttachmentSelectionMenuTests : BaseAttachmentsControllerTests() {

    @Test
    fun `Should hide attachments menu`() {
        sut.onClickCloseAttachmentSelectionMenu()

        verify(view).hideAttachmentsMenu()
    }

    @Test
    fun `If show open attachments menu config is True Should show open attachments menu button`() {
        sut = createSut(true)

        sut.onClickCloseAttachmentSelectionMenu()

        verify(view).showOpenAttachmentsMenuButton(true)
    }

    @Test
    fun `If show open attachments menu config is False Should never show open attachments menu button`() {
        sut = createSut(false)

        sut.onClickCloseAttachmentSelectionMenu()

        verify(view, never()).showOpenAttachmentsMenuButton(true)
    }
}
