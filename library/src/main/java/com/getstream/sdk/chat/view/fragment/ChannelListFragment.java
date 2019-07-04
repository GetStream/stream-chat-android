package com.getstream.sdk.chat.view.fragment;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.databinding.FragmentChannelListBinding;
import com.getstream.sdk.chat.function.EventFunction;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.channel.Event;
import com.getstream.sdk.chat.rest.Parser;
import com.getstream.sdk.chat.rest.apimodel.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.response.AddDevicesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetChannelsResponse;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.utils.ConnectionChecker;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.activity.ChatActivity;
import com.getstream.sdk.chat.view.activity.UsersActivity;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelListFragment extends Fragment implements WSResponseHandler {

    final String TAG = ChannelListFragment.class.getSimpleName();

    private ChannelListViewModel mViewModel;
    private FragmentChannelListBinding binding;
    public WebSocketService webSocketService;
    private ChannelListItemAdapter adapter;

    public int containerResId;
    public StreamChat streamChat;

    EventFunction eventFunction = new EventFunction();
    boolean isLastPage = false;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public static ChannelListFragment newInstance() {
        return new ChannelListFragment();
    }

    // region LifeCycle
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentChannelListBinding.inflate(inflater, container, false);
        mViewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        binding.setViewModel(mViewModel);

        init();
        configUIs();
        setStreamChat();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data); comment this unless you want to pass your result to the activity.
        if (requestCode == Constant.USERSLISTACTIVITY_REQUEST) {
            try {
                boolean result = data.getBooleanExtra("result", false);
                if (result) {
                    navigationChannelDetail(Global.channelResponse);
                }
            } catch (Exception e) {
            }
        }
    }
    //endregion

    // region Private Functions
    private void init() {
        webSocketService = new WebSocketService();
        webSocketService.setWSResponseHandler(this);
        Fresco.initialize(getContext());
        connectionCheck();
        permissionCheck();
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
//        ConnectionChecker.startConnectionCheckRepeatingTask(getContext());
    }

    private void configUIs() {
        FrameLayout frameLayout = getActivity().findViewById(this.containerResId);
        frameLayout.setFitsSystemWindows(true);
        binding.setShowMainProgressbar(true);
        configChannelListView();
        binding.listChannels.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.d(TAG, "LastVisiblePosition: " + view.getLastVisiblePosition());
                    if (view.getLastVisiblePosition() == Global.channels.size() - 1)
                        getChannels();
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.d(TAG, "SCROLLING UP");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

        binding.tvSend.setOnClickListener((View view) -> navigateUserList());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                adapter.filter = binding.etSearch.getText().toString();
                adapter.notifyDataSetChanged();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
    }

    public void setStreamChat() {
        binding.setShowMainProgressbar(!Global.noConnection);
        if (Global.noConnection) {
            Utils.showMessage(getContext(), "No internet connection!");
            return;
        }
        streamChat.wsConnection = webSocketService;
        streamChat.setupWebSocket();
    }

    private void setAfterFirstConnection(Event event) {
        // Initialize Channels
        Global.channels = new ArrayList<>();
        // Set Current User
        if (event.getMe() != null)
            Global.streamChat.setUser(event.getMe());
        // Set New Connection ID
        String connectionId = event.getConnection_id();
        Global.streamChat.setClientID(connectionId);
        Log.d(TAG, "Client ID : " + connectionId);

        initLoadingChannels();

        if (streamChat.getChannel() != null) {
            // If default Channel exist
            getChannel(streamChat.getChannel(), true);
        } else {
            getChannels();
        }
        // get and save Device Token
        getDeviceToken();
    }

    private void initLoadingChannels() {
        isCalling = false;
        isLastPage = false;
    }

    boolean isCalling;

    private void getChannels() {
        Log.d(TAG, "getChannels...");
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;
        Global.mRestController.getChannels(getPayload(), this::progressNewChannels
                , (String errMsg, int errCode) -> {
                    binding.setShowMainProgressbar(false);
                    isCalling = false;

                    Utils.showMessage(getContext(), errMsg);
                    Log.d(TAG, "Failed Get Channels : " + errMsg);
                });
    }

    private void progressNewChannels(GetChannelsResponse response) {
        binding.setShowMainProgressbar(false);
        isCalling = false;
        if (response.getChannels().isEmpty()) {
            Utils.showMessage(getContext(), "There is no any active Channel(s)!");
            return;
        }

        if (Global.channels == null) Global.channels = new ArrayList<>();
        boolean isReconnecting = false;
        if (Global.channels.isEmpty()) {
            configChannelListView();
            binding.setNoConnection(false);
            isReconnecting = true;
        }

        for (int i = 0; i < response.getChannels().size(); i++)
            Global.channels.add(response.getChannels().get(i));

        if (isReconnecting) this.eventFunction.handleReconnect(Global.noConnection);

        adapter.notifyDataSetChanged();
        isLastPage = (response.getChannels().size() < Constant.CHANNEL_LIMIT);
    }

    private void getChannel(Channel channel, boolean goChat) {
        binding.setShowMainProgressbar(true);
        channel.setType(ModelType.channel_messaging);
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();
        Log.d(TAG, "Channel Connecting...");

        ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

        Global.mRestController.channelDetailWithID(channel.getId(), request, (ChannelResponse response) -> {
            binding.setShowMainProgressbar(false);
            if (!response.getMessages().isEmpty())
                Global.setStartDay(response.getMessages(), null);
            Global.addChannelResponse(response);
            if (goChat) {
                navigationChannelDetail(response);
            } else {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
            Gson gson = new Gson();
            Log.d(TAG, "Channel Response: " + gson.toJson(response));

        }, (String errMsg, int errCode) -> {
            binding.setShowMainProgressbar(false);
            Log.d(TAG, "Failed Connect Channel : " + errMsg);
        });
    }

    private JSONObject getPayload() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> filter_conditions = new HashMap<>();
        Map<String, List<String>> filterOption = new HashMap<>();

        filterOption.put("$in", Arrays.asList(Global.streamChat.getUser().getId()));
        filter_conditions.put("type", "messaging");
        filter_conditions.put("members", filterOption);

        Map<String, Object> sort = new HashMap<>();
        sort.put("field", "last_message_at");
        sort.put("direction", -1);

        payload.put("filter_conditions", filter_conditions);
        payload.put("sort", Collections.singletonList(sort));

        payload.put("message_limit", Constant.CHANNEL_MESSAGE_LIMIT);
        if (Global.channels.size() > 0)
            payload.put("offset", Global.channels.size());
        payload.put("limit", Constant.CHANNEL_LIMIT);
        payload.put("presence", false);
        payload.put("state", true);
        payload.put("subscribe", true);
        payload.put("watch", true);

        JSONObject json;
        json = new JSONObject(payload);
        return json;
    }

    private void configChannelListView() {
        adapter = new ChannelListItemAdapter(getContext(), Global.channels, (View view) -> {
            String channelId = view.getTag().toString();
            ChannelResponse response = Global.getChannelResponseById(channelId);
            if (Global.channels.isEmpty())
                Utils.showMessage(getContext(), "No internet connection!");
            else if (response != null)
                navigationChannelDetail(response);

        }, (View view) -> {
            String channelId = view.getTag().toString();
            final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Do you want to delete this channel?")
                    .setMessage("If you delete this channel, will delete all chat history for this channel!")
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            alertDialog.setOnShowListener((DialogInterface dialog) -> {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener((View v) -> {
                    Log.d(TAG, "Deleting Channel ID: " + channelId);
                    ChannelResponse response_ = Global.getChannelResponseById(channelId);
                    Global.mRestController.deleteChannel(channelId, (ChannelResponse response) -> {
                        Utils.showMessage(getContext(), "Deleted successfully!");
                        Global.channels.remove(response_);
                        adapter.notifyDataSetChanged();
                    }, (String errMsg, int errCode) -> {
                        Log.d(TAG, "Failed Deleting: " + errMsg);
                        Utils.showMessage(getContext(), errMsg);
                    });
                    alertDialog.dismiss();
                });

            });
            alertDialog.show();
            return true;
        });
        binding.listChannels.setAdapter(adapter);
    }

    private void navigationChannelDetail(ChannelResponse response) {
        binding.setShowMainProgressbar(false);
        Global.setStartDay(response.getMessages(), null);
        Log.d(TAG, "Channel ID:" + response.getChannel());
        Global.eventFunction = eventFunction;
        Global.channelResponse = response;
        Intent intent = new Intent(getContext(), ChatActivity.class);
        getActivity().startActivity(intent);
    }

    private void navigateUserList() {
        Intent intent = new Intent(getContext(), UsersActivity.class);
        startActivityForResult(intent, Constant.USERSLISTACTIVITY_REQUEST);
    }

    private void getDeviceToken() {
        String token = pref.getString("Token", null);
        if (token != null) {
            Log.d(TAG, "device Token: " + token);
            return;
        }
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener((@NonNull Task<InstanceIdResult> task) -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getActivity(), "getInstanceId failed:" + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "getInstanceId failed", task.getException());
                        return;
                    }
                    String token_ = task.getResult().getToken();
                    Log.d(TAG, "device Token: " + token_);
                    // Save to Server
                    addDevice(token_);
                    // Save to Local
                    editor.putString("Token", token_);
                    editor.commit();
                });
    }

    private void addDevice(@NonNull String deviceId) {
        AddDeviceRequest request = new AddDeviceRequest(deviceId);
        Global.mRestController.addDevice(request, (AddDevicesResponse response) -> {
            Log.d(TAG, "ADDED Device!");
        }, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed ADD Device! " + errMsg);
        });
    }
    //endregion

    // region Listners
    @Override
    public void handleWSResponse(Object response) {
        Global.noConnection = false;
        if (response.getClass().equals(String.class)) {
            // Checking No connection
            JSONObject json = null;
            try {
                json = new JSONObject(response.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (json == null) return;

            Event event = Parser.parseEvent(json);
            if (!event.getType().equals(Event.health_check))
                Log.d(TAG, "Connection Response : " + json);

            if (!TextUtils.isEmpty(event.getConnection_id()) && TextUtils.isEmpty(Global.streamChat.getClientID())) {
                setAfterFirstConnection(event);
                return;
            }
            eventFunction.handleReceiveEvent(event);
            if (event.getType().equals(Event.notification_added_to_channel)) {
                Channel channel_ = event.getChannel();
                getChannel(channel_, false);
            }
            switch (event.getType()) {
                case Event.message_new:
                case Event.message_read:
                case Event.channel_deleted:
                case Event.channel_updated:
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    break;
            }
        }
    }

    @Override
    public void onFailed(String errMsg, int errCode) {
        Global.noConnection = true;
        Global.streamChat.setClientID(null);
        binding.setNoConnection(true);
        eventFunction.handleReconnect(Global.noConnection);
        binding.setShowMainProgressbar(false);
    }

    //endregion

    // region Permission
    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            int hasStoragePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int hasCameraPermission = getActivity().checkSelfPermission(Manifest.permission.CAMERA);


            List<String> permissions = new ArrayList<>();
            if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]),
                        Constant.PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            // TODO: 7/12/2016 DO NOTHING
        }
    }
    // endregion

    // region Connection Check
    private void connectionCheck() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Global.noConnection = !(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        Log.d(TAG, "Connection: " + !Global.noConnection);
    }
    // endregion
}
