package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.BaseStyle;
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.Collections;
import java.util.List;

public class ReadStateView<STYLE extends BaseStyle> extends RelativeLayout {
    private List<ChannelUserRead> reads;
    STYLE style;

    public ReadStateView(Context context) {
        super(context);

    }

    public ReadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    public void setReads(List<ChannelUserRead> reads, STYLE style) {
        this.reads = reads;
        this.style = style;
        init();
    }

    public void init() {
        removeAllViews();
        setVisibility(style.isShowReadState() ? VISIBLE : GONE);
        if (!style.isShowReadState()) return;
        if (reads == null || reads.isEmpty()) return;

        // Show the icon of the user who was last to read...
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
        User user = reads.get(0).getUser();
        String image = user.getImage();
        CircularImageView imageView = new CircularImageView(getContext());
        imageView.setPlaceholder(user.getInitials(),
                style.getAvatarBackGroundColor(),
                style.getAvatarInitialTextColor());
        imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                (int) (style.getReadStateTextSize()),
                style.getAvatarInitialTextStyle());

        Glide.with(getContext())
                .load(image)
                .into(imageView);

        RelativeLayout.LayoutParams params;
        params = new RelativeLayout.LayoutParams(
                (int) (style.getReadStateAvatarWidth()),
                (int) (style.getReadStateAvatarHeight()));
        imageView.setLayoutParams(params);

        addView(imageView);
    }
}
