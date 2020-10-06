package com.getstream.sdk.chat.view.messageinput.attachments

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.junit.jupiter.api.Test

internal class WhenOnCameraClickTests : BaseAttachmentsControllerTests() {

    @Test
    fun `If camera permission is granted Should show camera options`() {
        When calling permissionHelper.isGrantedCameraPermissions(any()) doReturn true

        sut.onCameraClick()

        verify(view).showCameraOptions()
    }

    @Test
    fun `If camera permission is not granted Should check camera permissions to permission helper`() {
        When calling permissionHelper.isGrantedCameraPermissions(any()) doReturn false

        sut.onCameraClick()

        verify(permissionHelper).checkCameraPermissions(any(), any(), any())
    }
}
