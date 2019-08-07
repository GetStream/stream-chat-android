package io.getstream.chat.example;

import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.component.Component;

import com.getstream.sdk.chat.enums.ReadIndicator;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.enums.Token;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.view.fragment.ChannelFragment;
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

import com.getstream.sdk.chat.enums.FilterObject;

import static com.getstream.sdk.chat.enums.Filters.eq;
import static com.getstream.sdk.chat.enums.Filters.in;
import static com.getstream.sdk.chat.enums.Filters.or;

public class MainActivity extends AppCompatActivity {

    final String USER_NAME = "Broken waterfall";
    final String USER_ID = "broken-waterfall-5";
    final String USER_IMAGE = "https://bit.ly/2u9Vc0r";
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg";

    boolean isSingleConversation = true;

    // region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("niceName", "Test Nicename");
        extraData.put("name", USER_NAME);
        extraData.put("image", USER_IMAGE);

        User user = new User(USER_ID, extraData);

        Client client = StreamChat.getInstance();

        try {
            setStreamChatUser(client, user, Token.HARDCODED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isSingleConversation)
            singleConversation();
        else
            multiConversation(client);

    }

    // endregion

    // region SetStreamChat

    private void setStreamChatUser(Client client, User user, Token token) throws Exception {
        switch (token) {
            case SERVERSIDE:
                client.setUser(user, new TokenProvider() {
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
                client.setUser(user, USER_TOKEN);
                break;
            case GUEST:
            case DEVELOPMENT:
                client.setUser(user, token);
                break;
            default:
                break;
        }
    }
    // endregion

    // region Customization
    private void customChannelPreview(Component component) {
        component.channel.setShowReadIndicator(true);
        component.channel.setReadIndicatorType(ReadIndicator.UNREAD_COUNT);

//        component.channel.setChannelItemLayoutId(R.layout.list_item_custom_channel);
//        component.channel.setChannelItemViewHolderName(CustomChannelListItemViewHolder.class.getName());
    }

    private void customMessageItemView(Component component) {
//        component.message.setMessageItemLayoutId(R.layout.list_item_custom_message);
//        component.message.setMessageItemViewHolderName(CustomisedMsgListItemViewHolder.class.getName());
    }

    private void customFilterChannelOption(Component component) {
//        FilterObject filter = or(in("members", USER_ID), eq("type", "messaging"));
        FilterObject filter = eq("type", ModelType.channel_messaging);
        component.channel.filter(filter);
    }

    private void customFilterUserOption(Component component) {
        FilterObject filter = or(in("id", "testuser2", "testuser3"), eq("gender", "male"));
        component.user.filter(filter);
    }

    private void customOrderChannelOption(Component component) {
        Map<String, Object> sortOption = new HashMap<>();
        sortOption.put("field", "last_message_at");
        sortOption.put("direction", -1);
        component.channel.setSortOptions(sortOption);
    }

    private void customOrderUserOption(Component component) {
        Map<String, Object> sortOption = new HashMap<>();
        sortOption.put("field", "last_active");
        sortOption.put("direction", -1);
        component.user.setSortOptions(sortOption);
    }

    // endregion


    // region Open Stream Chat

    public void multiConversation(Client client) {

        // Customize Commponets and Filter&Order feature
        Component component = new Component();

        // Custom Style
        customChannelPreview(component);

        // Custom Channel Filter
        customFilterChannelOption(component);

        // Custom User Filder
        customFilterUserOption(component);

        // Custom Channel Order
        customOrderChannelOption(component);

        // Custom User Order
        customOrderUserOption(component);

        // Custom MessageItemView
        customMessageItemView(component);

        component.channel.setShowSearchBar(true);

        client.setComponent(component);

        ChannelListFragment fragment = new ChannelListFragment();
        fragment.client = client;
        fragment.containerResId = R.id.container;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void singleConversation() {
        HashMap<String, Object> extraData = new HashMap<>();
        List<String> members = new ArrayList<>();
        members.add(USER_ID);
        members.add("spring-unit-2");
        extraData.put("members", members);
        extraData.put("group", "sports");

        ChannelFragment fragment = new ChannelFragment();
        fragment.channelType = "message";
        fragment.channelID = "general";
        fragment.channelExtraData = extraData;
        fragment.singleConversation = true;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    // endregion
}
