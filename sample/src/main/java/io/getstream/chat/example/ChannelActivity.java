package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel2;

import io.getstream.chat.example.databinding.ActivityChannelBinding;

public class ChannelActivity extends AppCompatActivity {

    private ChannelViewModel2 viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        String channelType = intent.getStringExtra(MainActivity.EXTRA_CHANNEL_TYPE);
        String channelID = intent.getStringExtra(MainActivity.EXTRA_CHANNEL_ID);
        Client client = StreamChat.getInstance();
        Channel channel = client.channel(channelType, channelID);

        // we're using data binding in this example
        ActivityChannelBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_channel);

        // most the business logic of the chat is handled in the view model
        viewModel = ViewModelProviders.of(this).get(ChannelViewModel2.class);
        viewModel.setChannel(channel);

        // connect the view model
        binding.chtHeader.setViewModel(viewModel);
        binding.messageInput.setViewModel(viewModel);
        binding.mlvMessageList.setViewModel(viewModel);


        // set the viewModel data for the layout
        binding.setViewModel(viewModel);

    }
}