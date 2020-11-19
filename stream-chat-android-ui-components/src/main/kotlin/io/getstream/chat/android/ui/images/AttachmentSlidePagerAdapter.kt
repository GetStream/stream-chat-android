package io.getstream.chat.android.ui.images

import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class AttachmentSlidePagerAdapter(
    fa: FragmentActivity,
    private val bitmapList: List<Bitmap>
) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = bitmapList.size

    override fun createFragment(position: Int): Fragment {
        return ImageSlidePageFragment().apply {
            this.imageBitmap = bitmapList[position]
        }
    }
}
