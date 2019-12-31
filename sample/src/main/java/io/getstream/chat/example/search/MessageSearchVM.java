package io.getstream.chat.example.search;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.Filters;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.SearchMessagesCallback;
import com.getstream.sdk.chat.rest.request.SearchMessagesRequest;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.rest.response.SearchMessagesResponse;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.example.utils.AppDataConfig;
import io.getstream.chat.example.utils.SingleLiveEvent;
import io.getstream.chat.example.utils.UserConfig;

public class MessageSearchVM extends AndroidViewModel {

    private static final String TAG = MessageSearchVM.class.getSimpleName();
    private static final int QUERY_LIMIT = 50;

    public MutableLiveData<Boolean> isLoading;
    public MutableLiveData<Boolean> isEmpty;
    public MutableLiveData<String> searchQuery;
    MutableLiveData<List<MessageResponse>> searchResult;
    SingleLiveEvent<String> onError;

    private Client client;

    private Context context;
    private String cid;

    public MessageSearchVM(@NonNull Application application) {
        super(application);

        context = application;

        initLiveData();
        initComponents();
    }

    void search() {
        UserConfig userConf = AppDataConfig.getCurrentUser();
        if (userConf != null && searchQuery.getValue() != null) {
            loadQueryData(0, QUERY_LIMIT, userConf);
        } else {
            Log.e(TAG, "Require field is null. user = " + userConf + ", search = " + searchQuery.getValue());
        }
    }

    void setCid(String cid) {
        this.cid = cid;
    }

    //TODO Implement this
    // We can't load more, because we have no metadata in search result
    void loadMore(int offset) {
        UserConfig userConf = AppDataConfig.getCurrentUser();
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
        searchResult = new MutableLiveData<>();
        onError = new SingleLiveEvent<>();
    }

    private void initComponents() {
        client = StreamChat.getInstance(context);
    }

    private void loadQueryData(int offset, int limit, UserConfig userConf) {
        String query = searchQuery.getValue();
        if (query != null && !query.isEmpty()) {
            ArrayList<String> searchUsersList = new ArrayList<>();
            searchUsersList.add(userConf.getId());
            FilterObject filter;
            if (cid != null) {
                filter = Filters.in("cid", cid);
            } else {
                filter = Filters.in("members", searchUsersList);
            }

            SearchMessagesRequest searchRequest = new SearchMessagesRequest(filter, query)
                    .withLimit(limit)
                    .withOffset(offset);

            isLoading.setValue(true);
            client.searchMessages(searchRequest, new SearchMessagesCallback() {
                @Override
                public void onSuccess(SearchMessagesResponse response) {
                    Log.d(TAG, response.toString());
                    onSearchMessagesLoaded(response);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    Log.e(TAG, errMsg);
                    onError.setValue(errMsg);
                    isLoading.setValue(false);
                }
            });
        } else {
            Log.e(TAG, "Search query is null or empty. searchQuery = " + searchQuery);
        }
    }

    private void onSearchMessagesLoaded(SearchMessagesResponse response) {
        List<MessageResponse> result = response.getResults();
        searchResult.setValue(result);
        isLoading.setValue(false);
        isEmpty.setValue(result.isEmpty());
    }
}
