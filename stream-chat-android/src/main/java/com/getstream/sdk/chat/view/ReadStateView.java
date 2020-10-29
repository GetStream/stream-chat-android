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
import com.getstream.sdk.chat.ChatUI;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.style.ChatFonts;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.List;

import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.User;

public class ReadStateView<STYLE extends BaseStyle> extends RelativeLayout {
    STYLE style;
    boolean isIncoming;
    private List<ChannelUserRead> reads;

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
        if (!style.isShowReadState()
                || reads == null
                || reads.isEmpty()) return;


        ChatFonts chatFonts = ChatUI.instance().getFonts();


        User user = reads.get(0).getUser();
        String image = user.getExtraValue("name", "");
        // Avatar
        CircularImageView imageView = new CircularImageView(getContext());

        String initials = LlcMigrationUtils.getInitials(user);

        imageView.setPlaceholder(initials,
                style.getAvatarBackGroundColor(),
                style.readStateText.getColor());

        Typeface typeface = chatFonts.getFont(style.readStateText);

        if (typeface != null) {
            imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                    (style.readStateText.getSize()), typeface);
        } else
            imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                   (style.readStateText.getSize()),
                    style.readStateText.getStyle());

        if (!Utils.isSVGImage(image))
            Glide.with(getContext())
                    .load(image)
                    //TODO: llc check glide
                    //.load(StreamChat.instance().getUploadStorage().signGlideUrl(image))
                    .into(imageView);

        RelativeLayout.LayoutParams avatarParams = new RelativeLayout.LayoutParams(
                (style.getReadStateAvatarWidth()),
                (style.getReadStateAvatarHeight()));

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                (style.getReadStateAvatarHeight()));
        imageView.setId(1);

        if (reads.size() < 2) {
            imageView.setLayoutParams(avatarParams);
            addView(imageView);
            return;
        }
        // Count Text

        TextView textView = new TextView(getContext());
        textView.setText(String.valueOf(reads.size() - 1));

        textView.setTextColor(style.readStateText.getColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.readStateText.getSize());
        chatFonts.setFont(style.readStateText, textView);
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
