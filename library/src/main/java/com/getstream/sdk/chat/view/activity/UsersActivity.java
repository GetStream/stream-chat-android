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
import android.widget.EditText;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.UserGroupListAdapter;
import com.getstream.sdk.chat.adapter.UserListItemAdapter;
import com.getstream.sdk.chat.databinding.ActivityUsersBinding;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * An Activity of user list.
 */
public class UsersActivity extends AppCompatActivity {

    private static final String TAG = UsersActivity.class.getSimpleName();

    private StreamChat client;

    private ActivityUsersBinding binding;
    private UserListItemAdapter adapter;
    private UserGroupListAdapter groupListAdapter;
    private List<User> groupUsers;
    boolean isLastPage = false;

    RecyclerView.LayoutManager mLayoutManager;

    // region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_users);
        try {
            setSupportActionBar(binding.header);
        } catch (Exception e) {
        }

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
    // endregion

    // region Init
    private void init() {
        configChannelListView();
        getUsers();
    }

    private void configUIs() {
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvSelectedUsers.setLayoutManager(mLayoutManager);
        binding.clGroup.setVisibility(View.GONE);
    }

    public void onClickBackFinish(View v) {
        finish();
    }

    private void configChannelListView() {
        adapter = new UserListItemAdapter(this, client.users, (View view) -> {
            User user = (User) view.getTag();
            Log.d(TAG, "User Selected: " + user.getName());
            if (!groupUsers.contains(user)) {
                groupUsers.add(user);
            } else {
                groupUsers.remove(user);
            }
            changeGroupUsers(false);
        });
        binding.listUsers.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            if (binding.clGroup.getVisibility() == View.VISIBLE) return;
            if (!Global.noConnection)
                getChannel(Arrays.asList(client.users.get(i)));
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
                    if (view.getLastVisiblePosition() == client.users.size() - 1)
                        getUsers();
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.d(TAG, "SCROLLING UP");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
    }
    // endregion

    // region New Chat
    /**
     * Start private chat
     * */
    public void createNewChat() {
        adapter.groupChatMode = false;
        adapter.notifyDataSetChanged();
        binding.clGroup.setVisibility(View.GONE);
    }

    private void createNewGroupChat() {
        adapter.groupChatMode = true;
        adapter.notifyDataSetChanged();

        groupUsers = new ArrayList<>();
        binding.clGroup.setVisibility(View.VISIBLE);
        binding.tvDone.setVisibility(View.GONE);
        groupListAdapter = new UserGroupListAdapter(this, groupUsers, (View view) -> {
            User user = (User) view.getTag();
            groupUsers.remove(user);
            changeGroupUsers(true);
        });
        binding.rvSelectedUsers.setAdapter(groupListAdapter);
    }

    private void changeGroupUsers(boolean fromGroupView) {
        adapter.selectUsers = new ArrayList<>(groupUsers);
        groupListAdapter.notifyDataSetChanged();
        if (fromGroupView)
            adapter.notifyDataSetChanged();
        binding.tvDone.setVisibility(groupUsers.isEmpty() ? View.GONE : View.VISIBLE);
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

    // endregion

    // region Get Users and Channel

    boolean isCalling;

    private void getUsers() {
        Log.d(TAG, "queryUsers");
        Log.d(TAG, "isLastPage: " + isLastPage);
        Log.d(TAG, "isCalling: " + isCalling);
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;

        if (client == null)
            client = Global.client;
        client.queryUsers(new QueryUserListCallback() {
            @Override
            public void onSuccess(QueryUserListResponse response) {
                binding.setShowMainProgressbar(false);
                isCalling = false;
                if (response.getUsers().isEmpty()) {
                    Utils.showMessage(UsersActivity.this, "There is no any active user(s)!");
                    return;
                }

                if (client.users.isEmpty()) {
                    configChannelListView();
                }
                adapter.notifyDataSetChanged();
                isLastPage = (response.getUsers().size() < Constant.USER_LIMIT);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                binding.setShowMainProgressbar(false);
                isCalling = false;
                Utils.showMessage(UsersActivity.this, errMsg);
                Log.d(TAG, "Failed Get Channels : " + errMsg);
            }
        });
    }

    private void getChannel(List<User> users) {
//        boolean isPrivateChannel = users.size() == 1;
//        if (isPrivateChannel && Global.getPrivateChannel(users.get(0)) != null) {
//            navigateChatActivity(Global.getPrivateChannel(users.get(0)));
//            return;
//        }
//
//        binding.setShowMainProgressbar(true);
//        HashMap<String, Object> extraData = new HashMap<>();
//        extraData.put("members", users);
//
//        String channelId;
//        if (isPrivateChannel){
//            channelId = Global.client.user.getId() + "-" + users.get(0).getId();
//        }else{
//            String memberIds = "";
//            for (User user : users) {
//                memberIds += user.getId() + "-";
//            }
//            channelId = memberIds + getRandomHexString();
//        }
//
//        Channel channel = new Channel(ModelType.channel_messaging, channelId, extraData);
//
//        Map<String, Object> messages = new HashMap<>();
//        messages.put("limit", Constant.DEFAULT_LIMIT);
//        Map<String, Object> data = new HashMap<>();
//        data.put("name", channel.getName());
//        data.put("image", channel.getImage());
//
//        List<String> members = new ArrayList<>();
//        members.add(Global.client.user.getId());
//        for (User user : users) {
//            members.add(user.getId());
//        }
//        data.put("members", members);
//        data.put("group","sports");
//
//        Log.d(TAG, "Channel Connecting...");
//        QueryChannelRequest request = new QueryChannelRequest(messages, data, true, true);
//        Global.mRestController.channelDetailWithID(channel.getId(), request, (ChannelResponse response) -> {
//            if (!response.getMessages().isEmpty())
//                Global.setStartDay(response.getMessages(), null);
//            Global.addChannelResponse(response);
//            navigateChatActivity(response);
//            binding.setShowMainProgressbar(false);
//        }, (String errMsg, int errCode) -> {
//            binding.setShowMainProgressbar(false);
//            Utils.showMessage(this, errMsg);
//        });
    }

    /**
     * Start group chat
     * */
    public void onClickCreateGroupChat(View view) {
        getChannel(groupUsers);
    }

    private String getRandomHexString() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < 10) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 10);
    }
    // endregion

    private void navigateChatActivity(ChannelResponse response) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", true);
        returnIntent.putExtra(Constant.TAG_CHANNEL_RESPONSE_ID, response.getChannel().getId());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
