package com.getstream.sdk.chat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.BaseStyle;
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.Collections;
import java.util.List;

public class ReadStateView<STYLE extends BaseStyle> extends RelativeLayout {
    private List<ChannelUserRead> reads;
    STYLE style;
    boolean isIncoming;

    public ReadStateView(Context context) {
        super(context);

    }

    public ReadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setReads(List<ChannelUserRead> reads, boolean isIncoming, STYLE style) {
        this.reads = reads;
        this.style = style;
        this.isIncoming = isIncoming;
        init();
    }

    @SuppressLint("ResourceType")
    private void init() {
        removeAllViews();

        if (!style.isShowReadState()) return;
        if (reads == null || reads.isEmpty()) return;

        // Show the icon of the user who was last to read...
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> o2.getLastRead().compareTo(o1.getLastRead()));



        User user = reads.get(0).getUser();
        String image = user.getImage();
        // Avatar
        CircularImageView imageView = new CircularImageView(getContext());

        imageView.setPlaceholder(user.getInitials(),
                style.getAvatarBackGroundColor(),
                style.getAvatarInitialTextColor());
        imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                (style.getReadStateTextSize()),
                style.getAvatarInitialTextStyle());
        Glide.with(getContext())
                .load(image)
                .into(imageView);

        RelativeLayout.LayoutParams avatarParams = new RelativeLayout.LayoutParams(
                (style.getReadStateAvatarWidth()),
                (style.getReadStateAvatarHeight()));

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                (style.getReadStateAvatarHeight()));
        imageView.setId(1);

        if (reads.size()<2){
            imageView.setLayoutParams(avatarParams);
            addView(imageView);
            return;
        }
        // Count Text
        TextView textView = new TextView(getContext());
        textView.setText(String.valueOf(reads.size()));
        textView.setTextColor(style.getReadStateTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getReadStateTextSize());
        textView.setTypeface(Typeface.DEFAULT, style.getReadStateTextStyle());
        textView.setGravity(Gravity.CENTER);
        textView.setId(2);

        if (isIncoming) {
            textParams.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
            textParams.setMarginStart(getContext().getResources().getDimensionPixelOffset(R.dimen.stream_composer_stroke_width));
        } else {
            avatarParams.addRule(RelativeLayout.RIGHT_OF, textView.getId());
            avatarParams.setMarginStart(getContext().getResources().getDimensionPixelOffset(R.dimen.stream_composer_stroke_width));
        }
        imageView.setLayoutParams(avatarParams);
        textView.setLayoutParams(textParams);
        addView(textView);
        addView(imageView);
    }
}
