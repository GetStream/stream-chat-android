package io.getstream.chat.sdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.getstream.sdk.chat.Component;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.model.FilterOption;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.enums.Token;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.view.activity.ChatActivity;
import com.getstream.sdk.chat.view.fragment.ChannelListFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private StreamChat streamChat;

    private final String API_KEY = "cranwty2xrfm";

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    FrameLayout getstream_fragment;
    LinearLayout ll_buttons;

    // region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    public void onBackPressed() {
        if (getstream_fragment.getVisibility() == View.VISIBLE) {
            getstream_fragment.setVisibility(View.GONE);
            ll_buttons.setVisibility(View.VISIBLE);
            return;
        }

        super.onBackPressed();
    }
    // endregion

    // region SetStreamChat
    void init() {
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        getstream_fragment = findViewById(R.id.container);
        ll_buttons = findViewById(R.id.ll_buttons);
        configUser();
    }

    void configUser() {
        String userId = pref.getString("userId", null);
        String userName = pref.getString("userName", null);
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(userName)) {
            // Set Stream Client
            try {
                setStream(userId, userName);
            }catch (Exception e){

            }

        } else {
            // Set User Info
            inputUserInfo();
        }
    }

    // Open Dialog to input user info
    private void inputUserInfo() {
        LinearLayout lila1 = new LinearLayout(this);
        lila1.setOrientation(LinearLayout.VERTICAL);
        final EditText inputName = new EditText(this);
        final EditText inputId = new EditText(this);

        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        inputId.setInputType(InputType.TYPE_CLASS_TEXT);
        inputName.setHint("Full Name");
        inputId.setHint("User ID");

        lila1.addView(inputName);
        lila1.addView(inputId);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Please input your info")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setView(lila1);
        alertDialog.setOnShowListener((DialogInterface dialog) -> {

            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener((View view) -> {
                String userId = inputId.getText().toString();
                String userName = inputName.getText().toString();

                if (TextUtils.isEmpty(userName)) {
                    inputName.setError("Invalid Name!");
                    return;
                }
                if (TextUtils.isEmpty(userId)) {
                    inputId.setError("Invalid ID!");
                    return;
                }

                // save user info to local
                editor.putString("userName", userName);
                editor.putString("userId", userId);
                editor.commit();
                // set stream client
                try {
                    setStream(userId, userName);
                }catch (Exception e){

                }

                alertDialog.dismiss();
            });

        });
        alertDialog.show();
    }

    private void setStream(String USER_ID, String USER_NAME) throws Exception{
        streamChat = new StreamChat(API_KEY);

        /**
         * Add additional fields - you can add additional info of user
         * @param {HashMap} additionalFields User Additional fields
         */
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("niceName", "Test Nicename");
        additionalFields.put("age", 29);
        additionalFields.put("sex", "male");

        // User Constructor
        User user = new User(USER_ID, USER_NAME, additionalFields);

        /**
         * Set StreamChat User
         * @param token : Token type, i.e SERVERSIDE, DEVELOPMENT, HARDCODED and GUEST
         * @return
         */
        setStreamChatUser(streamChat, user, Token.DEVELOPMENT);
    }

    private void setStreamChatUser(StreamChat streamChat, User user, Token token) throws Exception {
        switch (token) {
            case SERVERSIDE:
                streamChat.setUser(user, new TokenProvider() {
                    @Override
                    public void onResult(TokenListener listener) {
                        Request request = new Request.Builder()
                                .url(String.format("https://my.backend.com/path/to/session/?user_id=%s", user.getId()))
                                .build();
                        OkHttpClient client = new OkHttpClient();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            public void onResponse(Call call, Response response) throws IOException {
                                String responseData = response.body().string();
                                try {
                                    JSONObject jsonObject = new JSONObject(responseData);
                                    String token = jsonObject.getString("user_token");
                                    listener.onResult(token);
                                } catch (JSONException e) {

                                }
                            }

                            public void onFailure(Call call, IOException e) {

                            }
                        });
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
                break;
            case HARDCODED:
                token.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9tbWFzbyJ9.XGkxJKi33fHr3cHyLFc6HRnbPgLuwNHuETWQ2MWzz5c");
                streamChat.setUser(user, Token.DEVELOPMENT);
                break;
            case GUEST:
            case DEVELOPMENT:
                streamChat.setUser(user, token);
                break;
            default:
                break;
        }
    }

    private void customChannelPreview(Component component) {
        /**
         * Show/Hide Last Message Indicator
         * @param {boolean}showReadIndicator : default value true.
         * @return
         **/
        component.channel.setShowReadIndicator(true);
    }

    private void customFilterChannelOption(Component component, String userId) {
        /**
         * Filter Channel by userIds of member
         * @param userIds : userIds Array that users in member in channel
         **/
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);

        Map<String, Object> filterValue = new HashMap<>();
        filterValue.put("$in", userIds);
        FilterOption filterOption = new FilterOption("members", filterValue);

        List<FilterOption> filterOptions = new ArrayList<>();
        filterOptions.add(filterOption);
        component.channel.setFilterOptions(filterOptions);
    }

    private void customFilterUserOption(Component component) {
        /**
         * Filter Users by users Id and gender
         * @param filterOption1 : filter users by users Ids
         * @param filterOption2 : filter users by gender
         * @param filterOptions : array of filteroptions
         **/
        List<String> userIds = new ArrayList<>();
        userIds.add("testuser7");
        userIds.add("testuser9");
        userIds.add("testuser10");

        Map<String, Object> filterValue = new HashMap<>();
        filterValue.put("$in", userIds);
        FilterOption filterOption1 = new FilterOption("id", filterValue);
        FilterOption filterOption2 = new FilterOption("sex", "male");
        List<FilterOption> filterOptions = new ArrayList<>();
        filterOptions.add(filterOption1);
        filterOptions.add(filterOption2);

        component.user.setFilterOptions(filterOptions);
        component.user.setQuery("$or"); // query can be replaced by $and, $nor
    }

    private void customOrderChannelOption(Component component) {
        /**
         * Order Channels by created Date date
         * @param created_at : Channel created date
         **/
        Map<String, Object> sortOption = new HashMap<>();
        sortOption.put("field", "created_at");
        sortOption.put("direction", -1);
        component.channel.setSortOptions(sortOption);
    }

    private void customOrderUserOption(Component component) {
        /**
         * Order Users by last active date
         * @param last_active : User Last Active default : -1
         **/
        Map<String, Object> sortOption = new HashMap<>();
        sortOption.put("field", "last_active");
        sortOption.put("direction", 1);
        component.user.setSortOptions(sortOption);
    }
    // endregion

    // region Open Stream Chat

    public void onClickSingleConversation(View v) {
        final EditText inputId = new EditText(this);
        inputId.setInputType(InputType.TYPE_CLASS_TEXT);
        inputId.setHint("Channel id");

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Please input channel id")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setView(inputId);
        alertDialog.setOnShowListener((DialogInterface dialog) -> {

            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener((View view) -> {
                String channelId = inputId.getText().toString();

                if (TextUtils.isEmpty(channelId)) {
                    inputId.setError("Invalid id!");
                    return;
                }

                singleConversation(channelId);
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    // Add Multi Conversation View
    public void onClickMultipleConversation(View v) {

        ll_buttons.setVisibility(View.GONE);
        getstream_fragment.setVisibility(View.VISIBLE);

        // Customize Commponets and Filter&Order feature
        Component component = new Component();

        // Custom Style
        customChannelPreview(component);

        // Custom Channel Filter
        String userId = pref.getString("userId", null);
        customFilterChannelOption(component, userId);

        // Custom User Filder
        customFilterUserOption(component);

        // Custom Channel Order
        customOrderChannelOption(component);

        // Custom User Order
        customOrderUserOption(component);

        ChannelListFragment fragment = new ChannelListFragment();
        fragment.containerResId = R.id.container;
        fragment.streamChat = streamChat;
        fragment.component = component;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // navigation Single Conversation view directly
    private void singleConversation(String channelId) {
        /**
         * Add additional fields - you can add additional info of Channel
         * @param {HashMap} additionalFields Channel Additional fields
         * @return
         */
        Map<String, Object> additionalFields = new HashMap<>();
        List<String>members = new ArrayList<>();
        String userId = pref.getString("userId", null);
        members.add(userId);
        members.add("testuser2");
        additionalFields.put("members",members);
        additionalFields.put("group","sports");

        Channel channel = new Channel("message", channelId, null,null, additionalFields);

        // Setting Channel
        streamChat.setChannel(channel);
        // Start ChatActivity
        Intent i = new Intent(this, ChatActivity.class);
        startActivity(i);
    }
    // endregion

}
