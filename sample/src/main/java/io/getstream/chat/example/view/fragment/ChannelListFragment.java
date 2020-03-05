package io.getstream.chat.example.view.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.ChannelQueryRequest;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.example.BaseApplication;
import io.getstream.chat.example.ChannelMoreActionDialog;
import io.getstream.chat.example.HomeActivity;
import io.getstream.chat.example.R;
import io.getstream.chat.example.databinding.FragmentChannelListBinding;
import io.getstream.chat.example.navigation.ChannelDestination;
import io.getstream.chat.example.navigation.SearchDestination;
import io.getstream.chat.example.utils.AppConfig;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static java.util.UUID.randomUUID;

public class ChannelListFragment extends Fragment {

    private static final String TAG = ChannelListFragment.class.getSimpleName();

    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    //private final Boolean offlineEnabled = false;
    private ChannelListViewModel viewModel;
    private ChatClient client;

    // establish a websocket connection to stream
    private void configureStreamClient() {
        client = StreamChat.getInstance();

        AppConfig appConfig = ((BaseApplication) getContext().getApplicationContext()).getAppConfig();

        if (appConfig.getCurrentUser() == null) {
            StreamChat.getLogger().logE(this, "Current user is null");
            return;
        }

        String USER_ID = appConfig.getCurrentUser().getId();
        String USER_TOKEN = appConfig.getCurrentUser().getToken();
        String USER_NAME = appConfig.getCurrentUser().getName();
        String USER_IMAGE = appConfig.getCurrentUser().getImage();

        Crashlytics.setUserIdentifier(USER_ID);

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", USER_NAME);
        extraData.put("image", USER_IMAGE);

        User user = new User(USER_ID);
        user.setExtraData(extraData);

        client.setUser(user, USER_TOKEN);

        // Set custom delay in 5 min
        //client.setWebSocketDisconnectDelay(1000 * 60 * 5);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel = ViewModelProviders.of(this).get(randomUUID().toString(), ChannelListViewModel.class);
        FilterObject filter = Filters.INSTANCE.eq("type", "messaging");
        viewModel.setChannelFilter(filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // setup the client
        configureStreamClient();
        // example for how to observe the unread counts
        StreamChat.getTotalUnreadMessages().observe(this, (Number count) -> {
            Log.i(TAG, String.format("Total unread message count is now %d", count));
        });
        StreamChat.getUnreadChannels().observe(this, (Number count) -> {
            Log.i(TAG, String.format("There are %d channels with unread messages", count));
        });

        // we're using data binding in this example
        FragmentChannelListBinding binding = FragmentChannelListBinding.inflate(inflater, container, false);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // most the business logic for chat is handled in the ChannelListViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // just get all channels
        FilterObject filter = Filters.INSTANCE.eq("type", "messaging");

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

        // set the viewModel data for the fragment_channel_list.xml layout
        binding.setViewModel(viewModel);

        binding.channelList.setViewModel(viewModel, this);

        // set your markdown
//        MarkdownImpl.setMarkdownListener((TextView textView, String message)-> {
//            // TODO: use your Markdown library or the extended Markwon.
//        });

        // setup an onclick listener to capture clicks to the user profile or channel

        binding.channelList.setOnChannelClickListener(channel -> {
            // open the channel activity
            StreamChat.getNavigator().navigate(new ChannelDestination(channel.getType(), channel.getId(), getContext()));
        });

        binding.channelList.setOnLongClickListener(this::showMoreActionDialog);
        binding.channelList.setOnUserClickListener(user -> {
            // open your user profile
        });

        binding.ivAdd.setOnClickListener(view -> createNewChannelDialog());

        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_hidden_channel:
                showHiddenChannels();
                return true;
            case R.id.action_search_messages_channel:
                openSearchActivity();
                return true;
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HomeActivity homeActivity = (HomeActivity) getActivity();
        Toolbar toolbar = getView().findViewById(R.id.toolbar);
        homeActivity.setSupportActionBar(toolbar);
    }

    private void createNewChannelDialog() {
        final EditText inputName = new EditText(getContext());
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
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

    private void createNewChannel(String name) {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", name);

        List<String> members = new ArrayList<>();

        User currentUser = client.getCurrentUser();

        members.add(currentUser.getId());
        extraData.put("members", members);

        String channelId = name.replaceAll(" ", "-").toLowerCase();

        viewModel.setLoading();

        ChannelQueryRequest request = new ChannelQueryRequest().withData(extraData);

        client.queryChannel(ModelType.channel_messaging, channelId, request).enqueue(new Function1<Result<io.getstream.chat.android.client.models.Channel>, Unit>() {
            @Override
            public Unit invoke(Result<io.getstream.chat.android.client.models.Channel> channelResult) {

                if (channelResult.isSuccess()) {

                    io.getstream.chat.android.client.models.Channel channel = channelResult.data();

                    StreamChat.getNavigator().navigate(new ChannelDestination(channel.getType(), channel.getId(), getContext()));
                    viewModel.addChannels(Arrays.asList(channel));
                    viewModel.setLoadingDone();
                } else {
                    viewModel.setLoadingDone();
                    Toast.makeText(getContext(), channelResult.error().getMessage(), Toast.LENGTH_SHORT).show();
                }

                return null;
            }
        });

//
//        Channel channel = new Channel(client, ModelType.channel_messaging, channelId, extraData);
//
//        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(10).withWatch();
//
//        viewModel.setLoading();
//        channel.query(request, new QueryChannelCallback() {
//            @Override
//            public void onSuccess(ChannelState response) {
//                StreamChat.getNavigator().navigate(new ChannelDestination(channel.getType(), channel.getId(), getContext()));
//                viewModel.addChannels(Arrays.asList(channel.getChannelState()));
//                viewModel.setLoadingDone();
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                viewModel.setLoadingDone();
//                Toast.makeText(getContext(), errMsg, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void showMoreActionDialog(Channel channel) {
        new ChannelMoreActionDialog(getContext())
                .setChannelListViewModel(viewModel)
                .setChannel(channel)
                .show();
    }
    // endregion

    private void showHiddenChannels() {
        Utils.showMessage(getContext(), StreamChat.getStrings().get(R.string.show_hidden_channel));
        FilterObject filter = Filters.INSTANCE.eq("type", "messaging").put("hidden", true);
        viewModel.setChannelFilter(filter);
        viewModel.queryChannels();
    }

    private void openSearchActivity() {
        StreamChat.getNavigator().navigate(new SearchDestination(null, getContext()));
    }
}
