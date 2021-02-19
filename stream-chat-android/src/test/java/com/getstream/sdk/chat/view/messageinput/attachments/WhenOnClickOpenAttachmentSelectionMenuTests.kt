package com.getstream.sdk.chat.view.messageinput.attachments

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test

internal class WhenOnClickOpenAttachmentSelectionMenuTests : BaseAttachmentsControllerTests() {
    @Test
    fun `Should invoke show attachments menu to view`() {
        sut.onClickOpenAttachmentSelectionMenu()

        verify(view).showAttachmentsMenu()
    }

    @Test
    fun `If camera permission is granted Should hide permissions`() {
        whenever(permissionHelper.isGrantedCameraPermissions(any())) doReturn true

        sut.onClickOpenAttachmentSelectionMenu()

        verify(view).showCameraPermissions(false)
        verify(view).showMediaPermissions(false)
    }

    @Test
    fun `If storage permission is granted and camera permission is not Should show camera permissions and hide media`() {
        whenever(permissionHelper.isGrantedCameraPermissions(any())) doReturn false
        whenever(permissionHelper.isGrantedStoragePermissions(any())) doReturn true

        sut.onClickOpenAttachmentSelectionMenu()

        verify(view).showCameraPermissions(true)
        verify(view).showMediaPermissions(false)
    }

    @Test
    fun `If neither storage permission nor camera permission is granted Should show all permissions`() {
        whenever(view.context) doReturn mock()
        whenever(permissionHelper.isGrantedCameraPermissions(any())) doReturn false
        whenever(permissionHelper.isGrantedStoragePermissions(any())) doReturn false

        sut.onClickOpenAttachmentSelectionMenu()

        verify(view).showCameraPermissions(true)
        verify(view).showMediaPermissions(true)
    }
}
