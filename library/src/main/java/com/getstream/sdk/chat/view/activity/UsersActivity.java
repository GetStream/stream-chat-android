package com.getstream.sdk.chat.view.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.UserGroupListAdapter;
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
    private UserGroupListAdapter groupListAdapter;
    private List<User> users = new ArrayList<>();
    private List<User> groupUsers = new ArrayList<>();
    boolean isLastPage = false;

    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_users);

        setSupportActionBar(binding.header);
        init();
        configUIs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int i = menuItem.getItemId();
        if (i == R.id.menu_chat) {
            createNewChat();
            return true;
        } else if (i == R.id.menu_group_chat) {
            inputGroupName();
            return true;
        }
        return false;
    }

    private void init() {
        configChannelListView();
        getUsers();
    }

    private void configUIs() {
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvSelectedUsers.setLayoutManager(mLayoutManager);
    }

    private void createNewChat() {
        adapter.groupChatMode = false;
        adapter.notifyDataSetChanged();
        binding.llGroup.setVisibility(View.GONE);
    }

    private void createNewGroupChat() {
        adapter.groupChatMode = true;
        adapter.notifyDataSetChanged();
        groupUsers.clear();
        binding.llGroup.setVisibility(View.VISIBLE);
        groupListAdapter = new UserGroupListAdapter(this, groupUsers, (View view) -> {
            User user = (User) view.getTag();
            groupUsers.remove(user);
            groupListAdapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
        });
        binding.rvSelectedUsers.setAdapter(groupListAdapter);
    }

    private void inputGroupName() {
        final EditText inputName = new EditText(this);
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        inputName.setHint("Group name");
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("New Group Chat")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setView(inputName);
        alertDialog.setOnShowListener((DialogInterface dialog) -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener((View view) -> {
                String groupName = inputName.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    inputName.setError("Invalid Name!");
                    return;
                }
                binding.tvGroupName.setText(groupName);
                createNewGroupChat();
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    public void onClickBackFinish(View v) {
        finish();
    }

    private void configChannelListView() {
        adapter = new UserListItemAdapter(this, users, groupUsers, (CompoundButton buttonView, boolean isChecked) -> {
            User user = (User) buttonView.getTag();
            Log.d(TAG, "User Selected: " + user.getName());
            if (isChecked) {
                groupUsers.add(user);
            } else {
                groupUsers.remove(user);
            }
            groupListAdapter.notifyDataSetChanged();
        });
        binding.listUsers.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            if (binding.llGroup.getVisibility() == View.VISIBLE) return;

            if (!Global.noConnection)
                getChannel(users.get(i));
            else
                Utils.showMessage(UsersActivity.this, "No internet connection!");
        });
        binding.listUsers.setAdapter(adapter);

        binding.listUsers.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.d(TAG, "LastVisiblePosition: " + view.getLastVisiblePosition());
                    if (view.getLastVisiblePosition() == users.size() - 1)
                        getUsers();
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.d(TAG, "SCROLLING UP");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    boolean isCalling;

    private void getUsers() {
        Log.d(TAG, "getUsers");
        Log.d(TAG, "isLastPage: " + isLastPage);
        Log.d(TAG, "isCalling: " + isCalling);
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;
        RestController.GetUsersCallback callback = (GetUsersResponse response) -> {
            binding.setShowMainProgressbar(false);
            isCalling = false;
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
                if (!response.getUsers().get(i).isMe())
                    users.add(response.getUsers().get(i));

            adapter.notifyDataSetChanged();
            isLastPage = (response.getUsers().size() < Constant.USER_LIMIT);
        };
        Global.mRestController.getUsers(getPayload(), callback, (String errMsg, int errCode) -> {
            binding.setShowMainProgressbar(false);
            isCalling = false;
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

        Channel channel = new Channel(ModelType.channel_messaging, channelId, null, null);

        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();
        data.put("name", channel.getName());
        data.put("image", channel.getImageURL());
        data.put("members", Arrays.asList(Global.streamChat.getUser().getId(), user.getId()));

//        if (Component.Channel.invitation) {
//            data.put("invites", Arrays.asList(user.getId()));
//        }
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
//        filter_conditions.put("presence",false);
        Map<String, Object> sort = new HashMap<>();
        sort.put("last_active", -1);
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
