package com.getstream.sdk.chat.viewmodel;

import android.text.TextUtils;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.Global;

import java.util.List;

public class ChannelViewModelOld extends ViewModel {
    // TODO: Implement the ViewModel
    private ChannelState channelState;

    public ChannelViewModelOld(ChannelState channelState) {
        this.channelState = channelState;
    }

    public ChannelState getChannelState() {
        return channelState;
    }

    private MutableLiveData<List<Message>> channelMessages = new MutableLiveData<>();

    public MutableLiveData<List<Message>> getChannelMessages() {
        return channelMessages;
    }

    public void setChannelMessages(List<Message> channelMessages) {
        this.channelMessages.setValue(channelMessages);
    }

    public boolean isOnline() {
//        if (channelState == null) return false;
//        try {
//            if (Global.getOpponentUser(channelState) == null)
//                return false;
//
//            return Global.getOpponentUser(channelState).getOnline();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return false;
    }
    public String channelName() {
//        if (channelState == null) return null;
//        if (!TextUtils.isEmpty(channelState.getChannel().getName())) {
//            return channelState.getChannel().getName();
//        } else {
//            User opponent = Global.getOpponentUser(channelState);
//            if (opponent != null) {
//                return opponent.getName();
//            }
//        }
        return null;
    }
    public boolean isVisibleLastActive() {
//        if (channelState == null) return false;
//        User opponent = Global.getOpponentUser(channelState);
//        if (opponent != null) {
//            if (TextUtils.isEmpty(Message.differentTime(opponent.getLast_active())))
//                return false;
//            else {
//
//                return true;
//            }
//        }
        return false;
    }
    public String lastActive(){
//        if (channelState == null) return null;
//        // Last Active
//        User opponent = Global.getOpponentUser(channelState);
//        if (opponent != null) {
//            if (TextUtils.isEmpty(Message.differentTime(opponent.getLast_active())))
//                return null;
//            else {
//
//                return Message.differentTime(opponent.getLast_active());
//            }
//        }
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
