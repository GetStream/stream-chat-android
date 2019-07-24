package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.getstream.sdk.chat.model.Message;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;

import java.util.List;

public abstract class BaseChannelListItemViewHolder extends View{
    public BaseChannelListItemViewHolder(Context context) {
        super(context);
    }

    //    public BaseChannelListItemViewHolder(int resId){
//        super();
//    }
    public abstract void bind(Context context, List<ChannelResponse> channels, View.OnClickListener clickListener, View.OnLongClickListener longClickListener);
}
