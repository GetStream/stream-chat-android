package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.Collections;
import java.util.List;

public class ReadStateView extends RelativeLayout {
    private List<ChannelUserRead> reads;
    private ReadStateStyle style;

    public ReadStateView(Context context) {
        super(context);

    }

    public ReadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        applyStyle();
    }

    private void applyStyle() {

    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        style = new ReadStateStyle(context, attrs);
    }

    public void setReads(List<ChannelUserRead> reads) {
        this.reads = reads;
        init();
    }

    public void init() {
        removeAllViews();
        if (reads == null || reads.isEmpty()) return;

        // Show the icon of the user who was last to read...
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
        User u = reads.get(0).getUser();
        String image = u.getImage();
//        Utils.circleImageLoad(this, image);
        AvatarGroupView<ReadStateStyle> avatarGroupView = new AvatarGroupView<>(getContext());
        avatarGroupView.setUser(u, style);
    }
}
