package io.getstream.chat.android.ui.attachments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.getstream.chat.android.ui.attachments.camera.CameraAttachmentFragment
import io.getstream.chat.android.ui.attachments.file.FileAttachmentFragment
import io.getstream.chat.android.ui.attachments.media.MediaAttachmentFragment
import java.lang.IllegalArgumentException

public class AttachmentDialogPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_MEDIA_ATTACHMENT -> MediaAttachmentFragment()
            PAGE_FILE_ATTACHMENT -> FileAttachmentFragment()
            PAGE_CAMERA_ATTACHMENT -> CameraAttachmentFragment()
            else -> throw IllegalArgumentException("Can not create page for position $position")
        }
    }

    override fun getItemCount(): Int = PAGE_COUNT

    public companion object {
        public const val PAGE_MEDIA_ATTACHMENT: Int = 0
        public const val PAGE_FILE_ATTACHMENT: Int = 1
        public const val PAGE_CAMERA_ATTACHMENT: Int = 2
        public const val PAGE_COUNT: Int = 3
    }
}
