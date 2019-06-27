package com.getstream.sdk.chat.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.text.TextUtils;
import android.view.View;

import com.getstream.sdk.chat.databinding.ListItemChannelBinding;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

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
    public String channelName() {
        if (!TextUtils.isEmpty(channelResponse.getChannel().getName())) {
            return channelResponse.getChannel().getName();
        } else {
            User opponent = Global.getOpponentUser(channelResponse);
            if (opponent != null) {
                return opponent.getName();
            }
        }
        return null;
    }
    public boolean isVisibleLastActive() {
        User opponent = Global.getOpponentUser(channelResponse);
        if (opponent != null) {
            if (TextUtils.isEmpty(Global.differentTime(opponent.getLast_active())))
                return false;
            else {

                return true;
            }
        }
        return false;
    }
    public String lastActive(){
        // Last Active
        User opponent = Global.getOpponentUser(channelResponse);
        if (opponent != null) {
            if (TextUtils.isEmpty(Global.differentTime(opponent.getLast_active())))
                return null;
            else {

                return Global.differentTime(opponent.getLast_active());
            }
        }
        return null;
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
