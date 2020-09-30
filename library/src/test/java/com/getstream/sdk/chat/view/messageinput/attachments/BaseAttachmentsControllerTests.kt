package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.utils.PermissionHelper
import com.getstream.sdk.chat.utils.StorageHelper
import com.getstream.sdk.chat.view.messageinput.AttachmentsController
import com.getstream.sdk.chat.view.messageinput.MessageInputController
import com.getstream.sdk.chat.view.messageinput.MessageInputView
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.junit.jupiter.api.BeforeEach

internal open class BaseAttachmentsControllerTests {

    protected lateinit var messageInputController: MessageInputController
    protected lateinit var permissionHelper: PermissionHelper
    protected lateinit var storageHelper: StorageHelper
    protected lateinit var view: MessageInputView

    protected lateinit var sut: AttachmentsController

    @BeforeEach
    open fun setup() {
        messageInputController = mock()
        permissionHelper = mock()
        storageHelper = mock()
        view = mock()
        When calling view.context doReturn mock()
        sut = createSut()
    }

    protected fun createSut(showOpenAttachmentsMenuConfig: Boolean = true) =
        AttachmentsController(
            messageInputController,
            permissionHelper,
            storageHelper,
            view,
            showOpenAttachmentsMenuConfig
        )
}
