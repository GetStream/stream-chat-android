package io.getstream.chat.sdk;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.view.fragment.ChannelListFragment;

public class ChannelsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);
        addGetStreamFragment(Global.streamChat);
    }

    private void addGetStreamFragment(@NonNull StreamChat streamChat) {
        ChannelListFragment fragment = new ChannelListFragment();
        fragment.containerResId = R.id.container;
        fragment.streamChat = streamChat;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
