package com.getstream.sdk.chat.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;

import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Global;

import java.util.List;

public class ChatActivityViewModel extends ViewModel {

    private ChannelResponse channelResponse;

    public ChatActivityViewModel(ChannelResponse channelResponse) {
        this.channelResponse = channelResponse;
    }

    public ChannelResponse getChannelResponse() {
        return channelResponse;
    }

    private MutableLiveData<List<Message>> channelMessages = new MutableLiveData<>();

    public MutableLiveData<List<Message>> getChannelMessages() {
        return channelMessages;
    }

    public void setChannelMessages(List<Message> channelMessages) {
        this.channelMessages.setValue(channelMessages);
    }

    public boolean isOnline() {
        try {
            if (Global.getOpponentUser(channelResponse) == null)
                return false;

            return Global.getOpponentUser(channelResponse).getOnline();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private ObservableField<Integer> replyCount = new ObservableField<>();

    public ObservableField<Integer> getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount.set(replyCount);
    }
// endregion
}
