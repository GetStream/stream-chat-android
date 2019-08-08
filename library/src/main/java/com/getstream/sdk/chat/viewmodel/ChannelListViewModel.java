package com.getstream.sdk.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class ChannelListViewModel extends AndroidViewModel {
    private final String TAG = ChannelViewModel2.class.getSimpleName();

    public MutableLiveData<Boolean> loading;
    public MutableLiveData<Boolean> loadingMore;
    public MutableLiveData<Boolean> failed;
    public MutableLiveData<Boolean> endOfPagination;
    public MutableLiveData<Boolean> online;


    public ChannelListViewModel(@NonNull Application application) {
        super(application);
        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        online = new MutableLiveData<>(true);
        endOfPagination = new MutableLiveData<>(false);

    }
}
