package io.getstream.chat.example;

import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import io.getstream.chat.example.adapter.ViewPagerAdapter;
import io.getstream.chat.example.databinding.ActivityMainBinding;

/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    MenuItem prevMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.viewPager.setAdapter(createCardAdapter());
        binding.tabs.setOnNavigationItemSelectedListener(item -> {
            int position = 0;
                        switch (item.getItemId()) {
                            case R.id.action_channel:
                                binding.viewPager.setCurrentItem(0);
                                position = 0;
                                break;
                            case R.id.action_profile:
                                binding.viewPager.setCurrentItem(1);
                                position = 1;
                                break;
                        }

                        if (prevMenuItem != null)
                            prevMenuItem.setChecked(false);
                        else
                            binding.tabs.getMenu().getItem(0).setChecked(false);

                        binding.tabs.getMenu().getItem(position).setChecked(true);
                        prevMenuItem = binding.tabs.getMenu().getItem(position);
                        return false;
                });
    }

    private ViewPagerAdapter createCardAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        return adapter;
    }

}
