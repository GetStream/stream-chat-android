package com.getstream.sdk.chat.view.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.UserGroupListAdapter;
import com.getstream.sdk.chat.adapter.UserListItemAdapter;
import com.getstream.sdk.chat.databinding.StreamActivityUsersBinding;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.client.utils.Result;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * An Activity of user list.
 */
public class UsersActivity extends AppCompatActivity {

    private TaggedLogger logger = ChatLogger.Companion.get("UsersActivity");
    boolean isLastPage = false;
    RecyclerView.LayoutManager mLayoutManager;
    boolean isCalling;
    private StreamActivityUsersBinding binding;
    private UserListItemAdapter adapter;
    private UserGroupListAdapter groupListAdapter;
    private List<User> groupUsers;

    // region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.stream_activity_users);
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
        getMenuInflater().inflate(R.menu.stream_new_chat, menu);
        return true;
    }
    // endregion

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

    // region Init
    private void init() {
        configChannelListView();
        getUsers();
    }

    private void configUIs() {
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        binding.rvSelectedUsers.setLayoutManager(mLayoutManager);
        binding.clGroup.setVisibility(View.GONE);
    }

    public void onClickBackFinish(View v) {
        finish();
    }
    // endregion

    // region New Chat

    private void configChannelListView() {
//        adapter = new UserListItemAdapter(this, client.users, (View view) -> {
//            User user = (User) view.getTag();
//            Log.d(TAG, "User Selected: " + user.getName());
//            if (!groupUsers.contains(user)) {
//                groupUsers.add(user);
//            } else {
//                groupUsers.remove(user);
//            }
//            changeGroupUsers(false);
//        });
//        binding.listUsers.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
//            if (binding.clGroup.getVisibility() == View.VISIBLE) return;
//            if (client.isConnected())
//                getChannel(Arrays.asList(client.users.get(i)));
//            else
//                Utils.showMessage(UsersActivity.this, "No internet connection!");
//        });
//
//        binding.listUsers.setAdapter(adapter);
//
//        binding.listUsers.setOnScrollListener(new AbsListView.OnScrollListener() {
//            private int mLastFirstVisibleItem;
//
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem,
//                                 int visibleItemCount, int totalItemCount) {
//                if (mLastFirstVisibleItem < firstVisibleItem) {
//                    Log.d(TAG, "LastVisiblePosition: " + view.getLastVisiblePosition());
//                    if (view.getLastVisiblePosition() == client.users.size() - 1)
//                        getUsers();
//                }
//                if (mLastFirstVisibleItem > firstVisibleItem) {
//                    Log.d(TAG, "SCROLLING UP");
//                }
//                mLastFirstVisibleItem = firstVisibleItem;
//            }
//        });
    }

    /**
     * Start private chat
     */
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

    // endregion

    // region Get Users and Channel

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

    private void getUsers() {
        logger.logI("queryUsers");
        logger.logI("isLastPage: " + isLastPage);
        logger.logI("isCalling: " + isCalling);
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;


        ChatClient.instance().queryUsers(getQueryUserRequest()).enqueue(new Function1<Result<List<User>>, Unit>() {
            @Override
            public Unit invoke(Result<List<User>> listResult) {

                if (listResult.isSuccess()) {

                    List<User> data = listResult.data();

                    binding.setShowMainProgressbar(false);
                    isCalling = false;
                    if (data.isEmpty()) {
                        Utils.showMessage(UsersActivity.this, "There is no any active user(s)!");

                    } else {
                        adapter.notifyDataSetChanged();
                        isLastPage = (data.size() < Constant.USER_LIMIT);
                    }

//                if (client.users.isEmpty()) {
//                    configChannelListView();
//                }

                } else {
                    binding.setShowMainProgressbar(false);
                    isCalling = false;
                    Utils.showMessage(UsersActivity.this, listResult.error().getMessage());
                    logger.logI("Failed Get Channels : " + listResult.error().getMessage());
                }

                return null;
            }
        });

//        StreamChat.getInstance().getUsers(getQueryUserRequest(), new QueryUserListCallback() {
//            @Override
//            public void onSuccess(QueryUserListResponse response) {
//                binding.setShowMainProgressbar(false);
//                isCalling = false;
//                if (response.getUsers().isEmpty()) {
//                    Utils.showMessage(UsersActivity.this, "There is no any active user(s)!");
//                    return;
//                }
//
////                if (client.users.isEmpty()) {
////                    configChannelListView();
////                }
//                adapter.notifyDataSetChanged();
//                isLastPage = (response.getUsers().size() < Constant.USER_LIMIT);
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                binding.setShowMainProgressbar(false);
//                isCalling = false;
//                Utils.showMessage(UsersActivity.this, errMsg);
//                StreamChat.getLogger(). logI(this,"Failed Get Channels : " + errMsg);
//            }
//        });
    }

    private QueryUsersRequest getQueryUserRequest() {
        FilterObject filter = new FilterObject();
        QuerySort sort = new QuerySort().asc("last_active");
        return new QueryUsersRequest(filter, 0, 10, sort, false);
    }

    private void getChannel(List<User> users) {
    }

    /**
     * Start group chat
     */
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

}
