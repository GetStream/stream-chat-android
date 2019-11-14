package io.getstream.chat.example;

import android.os.Bundle;

import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.getstream.chat.example.adapter.ViewPagerAdapter;
import io.getstream.chat.example.databinding.ActivityMainBinding;


/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.viewPager.setAdapter(createCardAdapter());
        new TabLayoutMediator(binding.tabs, binding.viewPager,(@NonNull TabLayout.Tab tab, int position)-> {
            tab.setIcon(getTabIcon(position));
            tab.setText(getTabTitle(position));
                }).attach();
    }

    private ViewPagerAdapter createCardAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        return adapter;
    }

    private @DrawableRes int getTabIcon(int position){
        if (position == 0)
            return R.drawable.ic_image;
        else
            return R.drawable.ic_file;
    }

    private String getTabTitle(int position){
        if (position == 0)
            return getString(R.string.tab_channel);
        else
            return getString(R.string.tab_profile);
    }

    // endregion
}
