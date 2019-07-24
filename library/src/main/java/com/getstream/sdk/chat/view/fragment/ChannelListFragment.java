package com.getstream.sdk.chat.view.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.getstream.sdk.chat.component.Component;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.databinding.FragmentChannelListBinding;
import com.getstream.sdk.chat.function.EventFunction;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.component.FilterOption;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.enums.FilterQuery;
import com.getstream.sdk.chat.rest.Parser;
import com.getstream.sdk.chat.rest.apimodel.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.response.AddDevicesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetChannelsResponse;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.PermissionChecker;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Fragment for Channels preview.
 */
public class ChannelListFragment extends Fragment implements WSResponseHandler {

    final String TAG = ChannelListFragment.class.getSimpleName();

    private ChannelListViewModel mViewModel;
    private FragmentChannelListBinding binding;
    private ChannelListItemAdapter adapter;

    public Component component;
    public int containerResId;
    public StreamChat streamChat;

    private boolean isLastPage = false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

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
        getChannels();
        PermissionChecker.permissionCheck(getActivity(), this);
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
    public void onDestroy(){
        super.onDestroy();
        Global.webSocketService.removeWSResponseHandler(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data); comment this unless you want to pass your result to the activity.
        if (requestCode == Constant.USERSLISTACTIVITY_REQUEST) {
            try {
                boolean result = data.getBooleanExtra("result", false);
                if (result) {
                    String channelId = data.getStringExtra(Constant.TAG_CHANNEL_RESPONSE_ID);
                    navigationChannelDetail(Global.getChannelResponseById(channelId));
                }
            } catch (Exception e) {
            }
        }
    }

    private Activity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Log.d(TAG, "onAttach");
            activity = (Activity) context;
        }
    }

    //endregion

    // region Private Functions
    private void init() {
        Global.webSocketService.setWSResponseHandler(this);
        if (Global.eventFunction == null) Global.eventFunction = new EventFunction();
        try {
            Fresco.initialize(getContext());
        } catch (Exception e) {
        }
        if (component == null) component = new Component();
        Global.component = component;
        connectionCheck();
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        Global.channels = new ArrayList<>();

//        ConnectionChecker.startConnectionCheckRepeatingTask(getContext());
    }

    private void configUIs() {
        // Fits SystemWindows
        try {
            FrameLayout frameLayout = getActivity().findViewById(this.containerResId);
            frameLayout.setFitsSystemWindows(true);
        }catch (Exception e){
            e.printStackTrace();
        }

        // hides Action Bar
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.clHeader.setVisibility(component.channelPreview.isShowSearchBar() ? View.VISIBLE : View.GONE);
        binding.listChannels.setVisibility(View.VISIBLE);
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
        getChannels();

        // get and save Device TokenService
        try {
            getDeviceToken();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Failed adding device token");
        }
    }

    private void initLoadingChannels() {
        isCalling = false;
        isLastPage = false;
    }

    boolean isCalling;

    /**
     * Getting channels
     */
    public void getChannels() {
        if (TextUtils.isEmpty(Global.streamChat.getClientID())) return;
        Log.d(TAG, "getChannels...");
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;
        Global.mRestController.getChannels(getPayload(), this::progressNewChannels
                , (String errMsg, int errCode) -> {
                    binding.setShowMainProgressbar(false);
                    isCalling = false;

//                    Utils.showMessage(getContext(), errMsg);
                    Log.d(TAG, "Failed Get Channels : " + errMsg);
                });
    }

    private void progressNewChannels(GetChannelsResponse response) {
        binding.setShowMainProgressbar(false);
        isCalling = false;
        if (response.getChannels().isEmpty()) {
            if (Global.channels == null || Global.channels.isEmpty())
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

//        if (isReconnecting) Global.eventFunction.handleReconnect(Global.noConnection);

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

        // Filter option
        filter_conditions.put("type", "messaging");

        if (component.channel.getFilterOptions() != null && component.channel.getFilterOptions().size() == 1) {
            FilterOption filterOption = component.channel.getFilterOptions().get(0);

            // Convert from FilterQuery to String
            Map<FilterQuery, Object> mapFilterValue = (Map<FilterQuery, Object>) filterOption.getValue();

            Map<String, Object> mapFilterValue_ = new HashMap<>();
            Set keys = mapFilterValue.keySet();
            for (Object key : keys) {
                mapFilterValue_.put(((FilterQuery) key).get(), mapFilterValue.get(key));
            }

            filter_conditions.put(filterOption.getKey(), mapFilterValue_);
        } else {
            if (component.channel.getQuery() != null) {

                List<Map<String, Object>> filterOptions = new ArrayList<>();

                for (FilterOption filterOption : component.channel.getFilterOptions()) {
                    // Convert from FilterQuery to String
                    if (filterOption.getValue().getClass().equals(String.class)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(filterOption.getKey(), filterOption.getValue());
                        filterOptions.add(map);
                    } else {
                        Map<FilterQuery, Object> mapFilterValue = (Map<FilterQuery, Object>) filterOption.getValue();
                        Map<String, Object> mapFilterValue_ = new HashMap<>();
                        Set keys = mapFilterValue.keySet();
                        for (Object key : keys) {
                            mapFilterValue_.put(((FilterQuery) key).get(), mapFilterValue.get(key));
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put(filterOption.getKey(), mapFilterValue_);
                        filterOptions.add(map);
                    }
                }
                filter_conditions.put(component.channel.getQuery().get(), filterOptions);
            } else {
//                Utils.showMessage(getActivity(), "You must set filter query!");
            }
        }


        // Sort Option
        if (component.channel.getSortOptions() != null) {
            payload.put("sort", Collections.singletonList(component.channel.getSortOptions()));
        } else {
            Map<String, Object> sort = new HashMap<>();
            sort.put("field", "last_message_at");
            sort.put("direction", -1);
            payload.put("sort", Collections.singletonList(sort));
        }

        payload.put("filter_conditions", filter_conditions);

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
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constant.TAG_CHANNEL_RESPONSE_ID, response.getChannel().getId());
        getActivity().startActivity(intent);
        Log.d(TAG, "Channel ID:" + response.getChannel().getId());
        Log.d(TAG, "Event Function:" + Global.eventFunction);
    }

    private void navigateUserList() {
        Intent intent = new Intent(getContext(), UsersActivity.class);
        startActivityForResult(intent, Constant.USERSLISTACTIVITY_REQUEST);
    }

    private void getDeviceToken() {
        String token = pref.getString("TokenService", null);
        if (token != null) {
            Log.d(TAG, "device TokenService: " + token);
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
                    Log.d(TAG, "device TokenService: " + token_);
                    // Save to Server
                    addDevice(token_);
                    // Save to Local
                    editor.putString("TokenService", token_);
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

    /**
     * Handle server response
     *
     * @param response Server response
     */
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
            Global.eventFunction.handleReceiveEvent(event);

            if (event.getType().equals(Event.notification_added_to_channel)) {
                Channel channel_ = event.getChannel();
                getChannel(channel_, false);
            }
            switch (event.getType()) {
                case Event.message_new:
                case Event.message_read:
                case Event.channel_deleted:
                case Event.channel_updated:
                    Log.d(TAG, event.getType());
                    if (activity != null) {
                        Log.d(TAG, event.getType());
                        activity.runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                    break;
            }
        }
    }

    /**
     * Handle server response failures.
     *
     * @param errMsg  Error message
     * @param errCode Error code
     */
    @Override
    public void onFailed(String errMsg, int errCode) {
        Global.noConnection = true;
        Global.streamChat.setClientID(null);
        binding.setNoConnection(true);
//        Global.eventFunction.handleReconnect(Global.noConnection);
        binding.setShowMainProgressbar(false);
    }

    //endregion

    // region Permission

    /**
     * Permission check
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int grantResult : grantResults)
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            if (!granted) PermissionChecker.showRationalDialog(getContext(), this);
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
