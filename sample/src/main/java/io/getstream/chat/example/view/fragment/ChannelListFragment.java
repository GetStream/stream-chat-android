package io.getstream.chat.example.view.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.getstream.chat.example.ChannelActivity;
import io.getstream.chat.example.databinding.FragmentChannelListBinding;
import io.getstream.chat.example.utils.AppDataStorage;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.eq;
import static java.util.UUID.randomUUID;

public class ChannelListFragment extends Fragment {

    private static final String TAG = ChannelListFragment.class.getSimpleName();

    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    final Boolean offlineEnabled = false;
    private FragmentChannelListBinding binding;
    private ChannelListViewModel viewModel;

    // establish a websocket connection to stream
    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance(getContext());

        String USER_ID = AppDataStorage.getCurrentUser().getId();
        String USER_TOKEN = AppDataStorage.getCurrentUser().getToken();
        String USER_NAME = AppDataStorage.getCurrentUser().getName();
        String USER_IMAGE = AppDataStorage.getCurrentUser().getImage();

        Crashlytics.setUserIdentifier(USER_ID);
        if (offlineEnabled) {
            client.enableOfflineStorage();
        }
        Crashlytics.setBool("offlineEnabled", offlineEnabled);

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", USER_NAME);
        extraData.put("image", USER_IMAGE);

        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN, new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                Log.i(TAG, String.format("Connection established for user %s", user.getName()));
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, String.format("Failed to establish websocket connection. Code %d message %s", errCode, errMsg));
            }
        });
        return client;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // setup the client
        Client client = configureStreamClient();
        // example for how to observe the unread counts
        StreamChat.getTotalUnreadMessages().observe(this, (Number count) -> {
            Log.i(TAG, String.format("Total unread message count is now %d", count));
        });
        StreamChat.getUnreadChannels().observe(this, (Number count) -> {
            Log.i(TAG, String.format("There are %d channels with unread messages", count));
        });

        // we're using data binding in this example
        binding = FragmentChannelListBinding.inflate(inflater, container, false);

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

        binding.channelList.setOnChannelClickListener(channel -> {
            // open the channel activity
            Intent intent = new Intent(getContext(), ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
            intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
            startActivity(intent);
        });
        binding.channelList.setOnUserClickListener(user -> {
            // open your user profile
        });
        binding.ivAdd.setOnClickListener(view -> showCreateNewChannelDialog());

        return binding.getRoot();
    }

    // region create new channel
    private void showCreateNewChannelDialog() {
        final EditText inputName = new EditText(getContext());
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        inputName.setHint("Type a channel name");
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    private void createNewChannel(String channelName) {
        Client client = configureStreamClient();

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
                Intent intent = new Intent(getContext(), ChannelActivity.class);
                intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
                intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
                startActivity(intent);
                viewModel.addChannels(Arrays.asList(channel.getChannelState()));
                viewModel.setLoadingDone();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                viewModel.setLoadingDone();
                Toast.makeText(getContext(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // endregion


    @Override
    public void onResume(){
        super.onResume();
        viewModel = ViewModelProviders.of(this).get(randomUUID().toString(), ChannelListViewModel.class);
        FilterObject filter = and(eq("type", "messaging"));
        viewModel.setChannelFilter(filter);
    }
}
