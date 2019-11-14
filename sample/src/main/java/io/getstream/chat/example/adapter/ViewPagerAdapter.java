package io.getstream.chat.example.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.getstream.chat.example.view.fragment.ChannelListFragment;
import io.getstream.chat.example.view.fragment.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0)
        return new ChannelListFragment();
        else return new ProfileFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
