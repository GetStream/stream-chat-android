package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.List;

public class ReadStateView extends AppCompatImageView {
    private List<ChannelUserRead> reads;

    public ReadStateView(Context context) {
        super(context);
    }

    public ReadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReadStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setReads(List<ChannelUserRead> reads) {
        this.reads = reads;
    }
}
