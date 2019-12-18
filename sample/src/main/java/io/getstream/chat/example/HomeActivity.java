package io.getstream.chat.example;

import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import io.getstream.chat.example.adapter.ViewPagerAdapter;
import io.getstream.chat.example.databinding.ActivityHomeBinding;

/**
 * This activity shows a list of channels
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    MenuItem prevMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.viewPager.setAdapter(createCardAdapter());
        binding.tabs.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_channel:
                    binding.viewPager.setCurrentItem(0);
                    break;
                case R.id.action_profile:
                    binding.viewPager.setCurrentItem(1);
                    break;
            }

            return false;
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);
                else
                    binding.tabs.getMenu().getItem(0).setChecked(false);

                binding.tabs.getMenu().getItem(position).setChecked(true);
                prevMenuItem = binding.tabs.getMenu().getItem(position);
            }
        });

        initToolbar(binding);
    }

    private ViewPagerAdapter createCardAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        return adapter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_hidden_channel, menu);
        return true;
    }

    private void initToolbar(ActivityHomeBinding binding) {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle("Stream Chat");
        binding.toolbar.setSubtitle("sdk:" + BuildConfig.SDK_VERSION + " / " + BuildConfig.VERSION_NAME + " / " + BuildConfig.APPLICATION_ID);
    }
}