package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.Filters;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.SearchMessagesCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.SearchMessagesRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.SearchMessagesResponse;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.getstream.chat.example.databinding.ActivityMainBinding;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.eq;


/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    final Boolean offlineEnabled = false;
    private Client client;

    private ChannelListViewModel viewModel;

    // establish a websocket connection to stream
    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance(getApplication());

        Crashlytics.setUserIdentifier(BuildConfig.USER_ID);
        if (offlineEnabled) {
            client.enableOfflineStorage();
        }
        Crashlytics.setBool("offlineEnabled", offlineEnabled);

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", BuildConfig.USER_NAME);
        extraData.put("image", BuildConfig.USER_IMAGE);

        // set sample of user
        User user = new User(BuildConfig.USER_ID, extraData);
        client.setUser(user, BuildConfig.USER_TOKEN, new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                Log.i(TAG, String.format("Connection established for user %s", user.getName()));

                searchMessage();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, String.format("Failed to establish websocket connection. Code %d message %s", errCode, errMsg));
            }
        });
        return client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);

        // setup the client
        client = configureStreamClient();
        // example for how to observe the unread counts
        StreamChat.getTotalUnreadMessages().observe(this, (Number count) -> {
            Log.i(TAG, String.format("Total unread message count is now %d", count));
        });
        StreamChat.getUnreadChannels().observe(this, (Number count) -> {
            Log.i(TAG, String.format("There are %d channels with unread messages", count));
        });

        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // most the business logic for chat is handled in the ChannelListViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // just get all channels
        FilterObject filter = and(eq("type", "messaging"));

        // ChannelViewHolderFactory factory = new ChannelViewHolderFactory();
        //binding.channelList.setViewHolderFactory(factory);
        viewModel.setChannelFilter(filter);


        // Example on how to ignore some events handled by the VM
        //    viewModel.setEventInterceptor((event, channel) -> {
        //        if (event.getType() == EventType.NOTIFICATION_MESSAGE_NEW && event.getMessage() != null) {
        //            return client.getUser().hasMuted(event.getMessage().getUser());
        //        }
        //        return false;
        //    });

        // set the viewModel data for the activity_main.xml layout
        binding.setViewModel(viewModel);

        binding.channelList.setViewModel(viewModel, this);

        // set your markdown
//        MarkdownImpl.setMarkdownListener((TextView textView, String message)-> {
//            // TODO: use your Markdown library or the extended Markwon.
//        });

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
        binding.ivAdd.setOnClickListener(this::createNewChannelDialog);

        initToolbar(binding);
    }

    private void searchMessage() {
        ArrayList<String> searchUsersList = new ArrayList<>();
        searchUsersList.add(BuildConfig.USER_ID);
        FilterObject filter = Filters.in("members", searchUsersList);
        SearchMessagesRequest searchRequest = new SearchMessagesRequest(filter, "hi")
                .withLimit(10)
                .withOffset(0);

        client.searchMessages(searchRequest, new SearchMessagesCallback() {
            @Override
            public void onSuccess(SearchMessagesResponse response) {
                Log.d(TAG, response.toString());
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, errMsg);
            }
        });
    }

    void createNewChannelDialog(View view) {
        final EditText inputName = new EditText(this);
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        inputName.setHint("Type a channel name");
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Create a Channel")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        alertDialog.setView(inputName);
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String channelName = inputName.getText().toString();
                if (TextUtils.isEmpty(channelName)) {
                    inputName.setError("Invalid Name!");
                    return;
                }
                createNewChannel(channelName);
                //switchUser("broken-waterfall-5", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg");
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    void switchUser(String userId, String token) {
        Client client = StreamChat.getInstance(getApplication());
        client.disconnect();

        User user = new User(userId);
        client.setUser(user, token);

        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);

        client.onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                viewModel.reload();
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        });
    }

    void createNewChannel(String channelName) {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", channelName);

        List<String> members = new ArrayList<>();
        members.add(client.getUser().getId());
        extraData.put("members", members);

        String channelId = channelName.replaceAll(" ", "-").toLowerCase();

        Channel channel = new Channel(client, ModelType.channel_messaging, channelId, extraData);

        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(10).withWatch();

        viewModel.setLoading();
        channel.query(request, new QueryChannelCallback() {
            @Override
            public void onSuccess(ChannelState response) {
                Intent intent = new Intent(MainActivity.this, ChannelActivity.class);
                intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
                intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
                startActivity(intent);
                viewModel.addChannels(Arrays.asList(channel.getChannelState()));
                viewModel.setLoadingDone();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                viewModel.setLoadingDone();
                Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initToolbar(ActivityMainBinding binding) {
        binding.toolbar.setTitle("Stream Chat");
        binding.toolbar.setSubtitle("sdk:" + BuildConfig.SDK_VERSION + " / " + BuildConfig.VERSION_NAME + " / " + BuildConfig.APPLICATION_ID);
    }
}
