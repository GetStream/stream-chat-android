package io.getstream.chat.android.ui.gallery

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class AttachmentGalleryPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val imageList: List<String>,
    private val imageClickListener: () -> Unit,
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        return AttachmentGalleryPageFragment.create(getItem(position), imageClickListener)
    }

    fun getItem(position: Int): String = imageList[position]
}
