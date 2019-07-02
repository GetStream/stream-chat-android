package com.getstream.sdk.chat.view.fragment;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;

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

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

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
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.utils.ConnectionChecker;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.activity.ChatActivity;
import com.getstream.sdk.chat.view.activity.UsersActivity;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;
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
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
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
    public void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data); comment this unless you want to pass your result to the activity.
        if (requestCode == Constant.USERSLISTACTIVITY_REQUEST) {
            try {
                boolean result = data.getBooleanExtra("result", false);
                if (result) {
                    navigationChannel(Global.channelResponse);
                }
            } catch (Exception e) {
            }
        }
    }
    //endregion

    // region Private Functions
    void init() {
        webSocketService = new WebSocketService();
        webSocketService.setWSResponseHandler(this);
        Fresco.initialize(getContext());
        connectionCheck();
        permissionCheck();
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
//        ConnectionChecker.startConnectionCheckRepeatingTask(getContext());
    }

    void configUIs() {
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
        binding.clHeader.setOnClickListener((View view) -> navigateUserList());
        binding.etSearch.setOnClickListener((View view) -> navigateUserList());
        binding.tvSend.setOnClickListener((View view) -> navigateUserList());
    }

    private void navigateUserList() {
        Intent intent = new Intent(getContext(), UsersActivity.class);
        startActivityForResult(intent, Constant.USERSLISTACTIVITY_REQUEST);
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

    private void initLoadingChannel() {
        isCalling = false;
        isLastPage = false;
    }

    boolean isCalling;

    private void getChannels() {
        Log.d(TAG, "getChannels...");
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;
        RestController.GetChannelsCallback callback = (GetChannelsResponse response) -> {
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
        };
        Global.mRestController.getChannels(getPayload(), callback, (String errMsg, int errCode) -> {
            binding.setShowMainProgressbar(false);
            isCalling = false;

            Utils.showMessage(getContext(), errMsg);
            Log.d(TAG, "Failed Get Channels : " + errMsg);
        });
    }

    private void getChannel(Channel channel) {
        binding.setShowMainProgressbar(true);
        channel.setType(ModelType.channel_messaging);
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();
        data.put("name", channel.getName());
        data.put("image", channel.getImageURL());
        data.put("members", Arrays.asList(Global.streamChat.getUser().getId()));
        data.put("watch", true);
        data.put("state", true);
        Log.d(TAG, "Channel Connecting...");

        ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

        RestController.ChannelDetailCallback callback = (ChannelResponse response) -> {
            if (!response.getMessages().isEmpty())
                Global.setStartDay(response.getMessages(), null);
            Global.addChannelResponse(response);
            Gson gson = new Gson();
            Log.d(TAG,"Channel Response: "+ gson.toJson(response));
            navigationChannel(response);
        };
        Global.mRestController.channelDetailWithID(channel.getId(), request, callback, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed Connect Channel : " + errMsg);
        });
    }

    private JSONObject getPayload() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> filter_conditions = new HashMap<>();
        Map<String, Object> user_details = new HashMap<>();
        Map<String, List<String>> filterOption = new HashMap<>();

        filterOption.put("$in", Arrays.asList(Global.streamChat.getUser().getId()));
        filter_conditions.put("type", "messaging");
        filter_conditions.put("members", filterOption);

        user_details.put("id", Global.streamChat.getUser().getId());
        user_details.put("name", Global.streamChat.getUser().getName());
        user_details.put("image", Global.streamChat.getUser().getImage());

        Map<String, Object> sort = new HashMap<>();
        sort.put("field", "last_message_at");
        sort.put("direction", -1);

        payload.put("filter_conditions", filter_conditions);

        payload.put("sort", Collections.singletonList(sort));
        payload.put("user_details", user_details);
        payload.put("message_limit", Constant.CHANNEL_MESSAGE_LIMIT);
        if (Global.channels.size() > 0)
            payload.put("offset", Global.channels.size());
        payload.put("limit", Constant.CHANNEL_LIMIT);
        payload.put("presence", false);
        payload.put("state", true);
//        payload.put("subscribe", true);
        payload.put("watch", true);

        JSONObject json;
        json = new JSONObject(payload);
        return json;
    }

    private void configChannelListView() {
        adapter = new ChannelListItemAdapter(getContext(), Global.channels, (View view) -> {
            int position = (Integer) view.getTag();
            Log.d(TAG, "onItemClick : " + position);
            if (!binding.getNoConnection())
                navigationChannel(Global.channels.get(position));
            else
                Utils.showMessage(getContext(), "No internet connection!");
        });
        binding.listChannels.setAdapter(adapter);
    }

    void addDevice() {
        Log.d(TAG, "DeviceId:" + Global.deviceId);
        if (TextUtils.isEmpty(Global.deviceId)) return;
        AddDeviceRequest request = new AddDeviceRequest();
        Global.mRestController.addDevice(request, (AddDevicesResponse response) -> {
            Log.d(TAG, "ADDED Device:");
        }, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed ADD Device:" + errMsg);
        });
    }
    //endregion

    // region Listners
    @Override
    public void handleWSResponse(Object response) {
        if (response.getClass().equals(String.class)) {

            // Checking No connection
            if (Global.noConnection) {
                binding.setNoConnection(true);
                eventFunction.handleReconnect(Global.noConnection);
                binding.setShowMainProgressbar(false);
                return;
            }

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
                String connectionId = event.getConnection_id();
                if (event.getMe() != null) Global.streamChat.setUser(event.getMe());
                Global.streamChat.setClientID(connectionId);
                Log.d(TAG, "Client ID : " + connectionId);
                initLoadingChannel();
                addDevice();
                if (streamChat.getChannel() == null){
                    getChannels();
                }else{
                    getChannel(streamChat.getChannel());
                }

                return;
            }

            eventFunction.handleReceiveEvent(event);

            switch (event.getType()) {
                case Event.message_new:
                case Event.message_read:
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    break;
            }
        }
    }

    @Override
    public void onFailed(String errMsg, int errCode) {

    }

    void navigationChannel(ChannelResponse response) {
        Global.setStartDay(response.getMessages(), null);
        Log.d(TAG,"Channel ID:" + response.getChannel());
        Global.eventFunction = eventFunction;
        Global.channelResponse = response;
        Intent intent = new Intent(getContext(), ChatActivity.class);
        getActivity().startActivity(intent);
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
