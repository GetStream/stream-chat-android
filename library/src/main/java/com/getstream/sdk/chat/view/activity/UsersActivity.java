package com.getstream.sdk.chat.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.UserListItemAdapter;
import com.getstream.sdk.chat.databinding.ActivityUsersBinding;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetUsersResponse;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = UsersActivity.class.getSimpleName();
    private ActivityUsersBinding binding;
    private UserListItemAdapter adapter;
    private List<User> users = new ArrayList<>();
    boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_users);
        init();
    }

    private void init() {
        configChannelListView();
        getUsers();
    }

    private void configChannelListView() {
        adapter = new UserListItemAdapter(this, users);
        binding.listUsers.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            if (!Global.noConnection)
                getChannel(users.get(i));
            else
                Utils.showMessage(UsersActivity.this, "No internet connection!");
        });
        binding.listUsers.setAdapter(adapter);
    }

    private void getUsers() {
        if (isLastPage) return;
        binding.setShowMainProgressbar(true);
        RestController.GetUsersCallback callback = (GetUsersResponse response) -> {
            binding.setShowMainProgressbar(false);

            if (response.getUsers().isEmpty()) {
                Utils.showMessage(this, "There is no any active user(s)!");
                return;
            }

            if (users == null) users = new ArrayList<>();
            boolean isReconnecting = false;
            if (users.isEmpty()) {
                configChannelListView();
                isReconnecting = true;
            }

            for (int i = 0; i < response.getUsers().size(); i++)
                users.add(response.getUsers().get(i));

            adapter.notifyDataSetChanged();
            isLastPage = (response.getUsers().size() < Constant.USER_LIMIT);
        };
        Global.mRestController.getUsers(getPayload(), callback, (String errMsg, int errCode) -> {
            binding.setShowMainProgressbar(false);

            Utils.showMessage(this, errMsg);
            Log.d(TAG, "Failed Get Channels : " + errMsg);
        });
    }

    private void getChannel(User user) {

        if (Global.getPrivateChannel(user) != null) {
            navigateChatActivity(Global.getPrivateChannel(user));
            return;
        }

        binding.setShowMainProgressbar(true);

        String channelId = Global.streamChat.getUser().getId() + "-" + user.getId();
        String channelName = Constant.CHANNEL_NAME_DEFAULT;
        String channelImage = Constant.CHANNEL_IMAGE_DEFAULT;

        Channel channel = new Channel(ModelType.channel_messaging, channelId, channelName, channelImage);

        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();
        data.put("name", channel.getName());
        data.put("image", channel.getImageURL());
        data.put("members", Arrays.asList(Global.streamChat.getUser().getId(), user.getId()));
        data.put("watch", true);
        data.put("state", true);
        Log.d(TAG, "Channel Connecting...");

        ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

        RestController.ChannelDetailCallback callback = (ChannelResponse response) -> {
            if (!response.getMessages().isEmpty())
                Global.setStartDay(response.getMessages(), null);
            Global.addChannelResponse(response);
            navigateChatActivity(response);
        };
        Global.mRestController.channelDetailWithID(channel.getId(), request, callback, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed Connect Channel : " + errMsg);
        });
    }

    private JSONObject getPayload() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> filter_conditions = new HashMap<>();

        Map<String, Object> sort = new HashMap<>();
        sort.put("created_at", -1);
        payload.put("filter_conditions", filter_conditions);
        payload.put("sort", Collections.singletonList(sort));
        if (users.size() > 0)
            payload.put("offset", users.size());
        payload.put("limit", Constant.USER_LIMIT);

        JSONObject json;
        json = new JSONObject(payload);
        return json;
    }

    private void navigateChatActivity(ChannelResponse response) {
        Global.channelResponse = response;
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", true);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
