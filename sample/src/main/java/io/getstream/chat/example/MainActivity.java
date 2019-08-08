package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.HashMap;

import io.getstream.chat.example.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    final String USER_ID = "broken-waterfall-5";
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg";

    private ChannelListViewModel viewModel;

    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance();
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Broken waterfall");
        extraData.put("image", "https://bit.ly/2u9Vc0r");
        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN);

        return client;
    }

    protected void queryChannels(Client c) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // initialize a websocket connection to Stream and setup the channel
        Client c = configureStreamClient();


        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // most the business logic of the chat is handled in the view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);

        //query = c.queryChannels().filterMembers(USER_ID)
        //viewModel.setChannelQuery(query)


        binding.channelList.setViewModel(viewModel);

        // set the viewModel data for the layout
        binding.setViewModel(viewModel);

    }
}
