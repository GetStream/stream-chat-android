package io.getstream.chat.android.ui.images

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class AttachmentSlidePagerAdapter(
    fa: FragmentActivity,
    private val imageList: List<String>
) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        return ImageSlidePageFragment().apply {
            this.image = imageList[position]
        }
    }
}
