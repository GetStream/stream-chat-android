package io.getstream.videosample.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(
  fragmentManager: FragmentManager,
  lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        return SimpleItemsFragment.newInstance()
    }
}
