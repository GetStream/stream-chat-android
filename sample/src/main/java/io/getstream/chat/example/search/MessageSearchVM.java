package io.getstream.chat.example.search;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.getstream.sdk.chat.StreamChat;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.getstream.chat.android.client.api.models.ChannelQueryRequest;
import io.getstream.chat.android.client.api.models.SearchMessagesRequest;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.example.BaseApplication;
import io.getstream.chat.example.utils.AppConfig;
import io.getstream.chat.example.utils.SingleLiveEvent;
import io.getstream.chat.example.utils.UserConfig;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MessageSearchVM extends AndroidViewModel {

    private static final String TAG = MessageSearchVM.class.getSimpleName();
    private static final int QUERY_LIMIT = 50;

    public MutableLiveData<Boolean> isLoading;
    public MutableLiveData<Boolean> isEmpty;
    public MutableLiveData<String> searchQuery;
    MutableLiveData<List<Message>> searchResult = new MutableLiveData<>();
    MutableLiveData<Channel> channelResult = new MutableLiveData<>();
    SingleLiveEvent<String> onError;

    private Context context;
    private AppConfig appConfig;
    private String cid;

    public MessageSearchVM(@NonNull Application application) {
        super(application);

        context = application;
        appConfig = ((BaseApplication) context.getApplicationContext()).getAppConfig();

        initLiveData();
    }

    void search() {


        UserConfig userConf = appConfig.getCurrentUser();
        if (userConf != null && searchQuery.getValue() != null) {
            loadQueryData(0, QUERY_LIMIT, userConf);
        } else {
            Log.e(TAG, "Require field is null. user = " + userConf + ", search = " + searchQuery.getValue());
        }
    }

    void setCid(String cid) {
        this.cid = cid;
    }

    void loadChannel() {
        String type = cid.split(":")[0];
        String id = cid.split(":")[1];
        StreamChat.getInstance().queryChannel(type, id, new ChannelQueryRequest()).enqueue(result -> {
            if (result.isSuccess()) {
                channelResult.setValue(result.data());
            } else {
                onError.setValue(result.error().getMessage());
            }
            return null;
        });
    }

    //TODO Implement this
    // We can't load more, because we have no metadata in search result
    void loadMore(int offset) {
        UserConfig userConf = appConfig.getCurrentUser();
        if (userConf != null && searchQuery.getValue() != null) {
            loadQueryData(offset, QUERY_LIMIT, userConf);
        } else {
            Log.e(TAG, "Require field is null. user = " + userConf + ", search = " + searchQuery.getValue());
        }
    }

    private void initLiveData() {
        isLoading = new MutableLiveData<>(false);
        isEmpty = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>();
        onError = new SingleLiveEvent<>();
    }

    private void loadQueryData(int offset, int limit, UserConfig userConf) {
        String query = searchQuery.getValue();
        if (query != null && !query.isEmpty()) {
            ArrayList<String> searchUsersList = new ArrayList<>();
            searchUsersList.add(userConf.getId());
            FilterObject filter;

            if (cid != null) {
                filter = Filters.INSTANCE.in("cid", cid);
            } else {
                filter = Filters.INSTANCE.in("members", searchUsersList);
            }

            SearchMessagesRequest searchRequest = new SearchMessagesRequest(query, offset, limit, filter);

            isLoading.setValue(true);

            StreamChat.getInstance().searchMessages(searchRequest).enqueue(new Function1<Result<List<Message>>, Unit>() {
                @Override
                public Unit invoke(Result<List<Message>> result) {

                    if (result.isSuccess()) {
                        onSearchMessagesLoaded(result.data());
                    } else {
                        onError.setValue(result.error().getMessage());
                        isLoading.setValue(false);
                    }

                    return null;
                }
            });
        } else {
            Log.e(TAG, "Search query is null or empty. searchQuery = " + searchQuery);
        }
    }

    private void onSearchMessagesLoaded(List<Message> messages) {
        searchResult.setValue(messages);
        isLoading.setValue(false);
        isEmpty.setValue(messages.isEmpty());
    }
}
