package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Utils;

import java.util.List;

public class ReadStateView extends AppCompatImageView {
    private List<ChannelUserRead> reads;
    private Context context;

    public ReadStateView(Context context) {
        super(context);
        this.context = context;
    }

    public ReadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ReadStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void init() {
        if (reads.size() > 0) {
            User u = reads.get(0).getUser();
            String image = u.getImage();
            Utils.circleImageLoad(this, image);
        }


    }

    public void setReads(List<ChannelUserRead> reads) {
        this.reads = reads;
        init();
    }
}
