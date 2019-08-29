package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ChannelViewHolderFactory;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.HashMap;

import io.getstream.chat.example.databinding.ActivityMainBinding;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.eq;
import static com.getstream.sdk.chat.enums.Filters.in;


/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    final String USER_ID = "bender";
    // User token is typically provided by your server when the user authenticates
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ";
    private ChannelListViewModel viewModel;

    // establish a websocket connection to stream
    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance(getApplication());

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Bender");
//        extraData.put("image", "https://imgix.ranker.com/user_node_img/50060/1001188616/original/bender-turns-into-a-criminal-in-the-first-episode-photo-u1?w=650&q=50&fm=pjpg&fit=crop&crop=faces");
        extraData.put("image", "https://bit.ly/321RmWb");
        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN);
        return client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);

        // setup the client
        Client client = configureStreamClient();

        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // most the business logic for chat is handled in the ChannelListViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // set the viewModel data for the activity_main.xml layout
        binding.setViewModel(viewModel);
//        viewModel.setChannelsPageSize(0);

        //ChannelListItemAdapter adapter = new ChannelListItemAdapter(this);
        //adapter.setCustomViewHolder(ChannelListItemViewHolder.class);
        binding.channelList.setViewModel(viewModel, this);
        // query all channels where the current user is a member
        // FilterObject filter = in("members", USER_ID);
//        FilterObject filter = and(eq("name", "general"),in("type", "messaging"));
        ChannelViewHolderFactory factory = new ChannelViewHolderFactory();
//        FilterObject filter = and(in("members", USER_ID), in("type", "messaging"));
        FilterObject filter = eq("type", "messaging");
//        FilterObject filter = and(in("type", "messaging"), eq("example", 1));
        //binding.channelList.setViewHolderFactory(factory);
        viewModel.setChannelFilter(filter);
//        viewModel.setChannelSort(new QuerySort().desc("updated_at"));
        // setup an onclick listener to capture clicks to the user profile or channel
        MainActivity parent = this;
        binding.channelList.setOnChannelClickListener(channel -> {
            // open the channel activity
            Intent intent = new Intent(parent, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
            intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
            startActivity(intent);
        });
        binding.channelList.setOnUserClickListener(user -> {
            // open your user profile
        });

    }
}
