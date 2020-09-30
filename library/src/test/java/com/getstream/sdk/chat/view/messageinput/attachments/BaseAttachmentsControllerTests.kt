package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.utils.PermissionHelper
import com.getstream.sdk.chat.view.messageinput.AttachmentsController
import com.getstream.sdk.chat.view.messageinput.MessageInputController
import com.getstream.sdk.chat.view.messageinput.MessageInputView
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.BeforeEach

internal open class BaseAttachmentsControllerTests {

    protected lateinit var messageInputController: MessageInputController
    protected lateinit var permissionHelper: PermissionHelper
    protected lateinit var view: MessageInputView

    protected lateinit var sut: AttachmentsController

    @BeforeEach
    fun setup() {
        messageInputController = mock()
        permissionHelper = mock()
        view = mock()
        sut = AttachmentsController(messageInputController, permissionHelper, view, true)
    }
}