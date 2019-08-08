package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel2;

import java.util.HashMap;

import io.getstream.chat.example.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    final String USER_ID = "broken-waterfall-5";
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg";

    private ChannelViewModel2 viewModel;

    protected Channel configureStreamChannel() {
        Client client = StreamChat.getInstance();
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Broken waterfall");
        extraData.put("image", "https://bit.ly/2u9Vc0r");
        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN);
        Channel channel = client.channel("message", "general");
        return channel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize a websocket connection to Stream and setup the channel
        Channel channel = configureStreamChannel();

        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // most the business logic of the chat is handled in the view model
        viewModel = ViewModelProviders.of(this).get(ChannelViewModel2.class);
        viewModel.setChannel(channel);
        // TODO: do we pass the view model, do we get the viewModel...
        // Or do we pass the channel object.... interesting...
        binding.messageInput.setViewModel(viewModel);
        binding.mlvMessageList.setViewModel(viewModel);





        // set the viewModel data for the layout
        binding.setViewModel(viewModel);

    }
}
