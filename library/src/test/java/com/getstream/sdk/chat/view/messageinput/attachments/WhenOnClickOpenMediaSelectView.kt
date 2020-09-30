package com.getstream.sdk.chat.view.messageinput.attachments

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.junit.jupiter.api.Test

internal class WhenOnClickOpenMediaSelectView : BaseAttachmentsControllerTests() {

    @Test
    fun `If storage permission is not granted Should check storage permission to permission helper`() {
        When calling permissionHelper.isGrantedStoragePermissions(any()) doReturn false

        sut.onClickOpenMediaSelectView(mock())

        verify(permissionHelper).checkStoragePermissions(any(), any(), any())
    }
}
