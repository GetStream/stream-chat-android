package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import io.getstream.chat.example.databinding.ActivityChannelBinding;

/**
 * Show the messages for a channel
 */
public class ChannelActivity extends AppCompatActivity {

    private ChannelViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // receive the intent and create a channel object
        Intent intent = getIntent();
        String channelType = intent.getStringExtra(MainActivity.EXTRA_CHANNEL_TYPE);
        String channelID = intent.getStringExtra(MainActivity.EXTRA_CHANNEL_ID);
        Client client = StreamChat.getInstance();
        Channel channel = client.channel(channelType, channelID);

        // we're using data binding in this example
        ActivityChannelBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_channel);
        // most the business logic of the chat is handled in the ChannelViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelViewModel.class);
        viewModel.setChannel(channel);

        // connect the view model
        binding.channelHeader.setViewModel(viewModel);
        binding.messageInput.setViewModel(viewModel);
        binding.messageList.setViewModel(viewModel);

        // set the viewModel data for the activity_channel.xml layout
        binding.setViewModel(viewModel);

    }
}