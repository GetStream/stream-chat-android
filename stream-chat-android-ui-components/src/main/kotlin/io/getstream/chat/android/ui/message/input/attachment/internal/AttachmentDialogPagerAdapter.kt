package io.getstream.chat.android.ui.message.input.attachment.internal

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.getstream.chat.android.ui.message.input.attachment.camera.internal.CameraAttachmentFragment
import io.getstream.chat.android.ui.message.input.attachment.file.internal.FileAttachmentFragment
import io.getstream.chat.android.ui.message.input.attachment.media.internal.MediaAttachmentFragment

internal class AttachmentDialogPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_MEDIA_ATTACHMENT -> MediaAttachmentFragment()
            PAGE_FILE_ATTACHMENT -> FileAttachmentFragment()
            PAGE_CAMERA_ATTACHMENT -> CameraAttachmentFragment()
            else -> throw IllegalArgumentException("Can not create page for position $position")
        }
    }

    override fun getItemCount(): Int = PAGE_COUNT

    companion object {
        const val PAGE_MEDIA_ATTACHMENT: Int = 0
        const val PAGE_FILE_ATTACHMENT: Int = 1
        const val PAGE_CAMERA_ATTACHMENT: Int = 2
        const val PAGE_COUNT: Int = 3
    }
}
