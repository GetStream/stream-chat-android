package com.getstream.sdk.chat.view.messageinput.attachments

import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageHelper
import com.getstream.sdk.chat.view.messageinput.AttachmentsController
import com.getstream.sdk.chat.view.messageinput.MessageInputController
import com.getstream.sdk.chat.view.messageinput.MessageInputView
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension

internal open class BaseAttachmentsControllerTests {

    protected lateinit var messageInputController: MessageInputController
    protected lateinit var permissionHelper: PermissionChecker
    protected lateinit var storageHelper: StorageHelper
    protected lateinit var view: MessageInputView
    protected lateinit var totalMediaAttachmentAdapter: MediaAttachmentAdapter
    protected lateinit var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter
    protected lateinit var selectedFileAttachmentAdapter: FileAttachmentSelectedAdapter

    protected lateinit var sut: AttachmentsController

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @ExperimentalCoroutinesApi
    @BeforeEach
    open fun setup() {
        messageInputController = mock()
        permissionHelper = mock()
        storageHelper = mock()
        view = mock()
        totalMediaAttachmentAdapter = mock()
        selectedMediaAttachmentAdapter = mock()
        selectedFileAttachmentAdapter = mock()
        whenever(view.context) doReturn mock()
        sut = createSut()
    }

    protected fun createSut(showOpenAttachmentsMenuConfig: Boolean = true) =
        AttachmentsController(
            messageInputController,
            permissionHelper,
            storageHelper,
            view,
            totalMediaAttachmentAdapter,
            selectedMediaAttachmentAdapter,
            selectedFileAttachmentAdapter,
            showOpenAttachmentsMenuConfig
        )
}
