package com.getstream.sdk.chat.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.BaseStyle;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.List;

public class AvatarGroupView<STYLE extends BaseStyle> extends RelativeLayout {
    Context context;
    Channel channel;
    STYLE style;
    LayoutInflater inflater;
    List<User> lastActiveUsers;

    public AvatarGroupView(Context context) {
        super(context);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AvatarGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AvatarGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AvatarGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setChannelAndLastActiveUsers(Channel channel, List<User> lastActiveUsers, @NonNull STYLE style) {
        this.channel = channel;
        this.lastActiveUsers = lastActiveUsers;
        this.style = style;
        configUIs();
    }

    private void configUIs() {

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) this.getLayoutParams();
        params.width = (int) style.getAvatarWidth();
        params.height = (int) style.getAvatarHeight();
        this.setLayoutParams(params);

        this.removeAllViews();
        if (!TextUtils.isEmpty(channel.getImage())) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(
                    (int) style.getAvatarWidth(),
                    (int) style.getAvatarHeight()));
            Utils.circleImageLoad(imageView, channel.getImage());
            this.addView(imageView);
        } else {
            configUserAvatars();
        }
    }

    double factor = 1.7;

    private void configUserAvatars() {
        if (lastActiveUsers != null && !lastActiveUsers.isEmpty()) {
            double factor_;
            for (int i = 0; i < (lastActiveUsers.size() < 4 ? lastActiveUsers.size() : 3); i++) {
                User user = lastActiveUsers.get(i);

                CircularImageView imageView = new CircularImageView(context);

                imageView.setBorderColor(style.getAvatarBorderColor());
                imageView.setPlaceholder(user.getInitials(),
                        style.getAvatarBackGroundColor(),
                        style.getAvatarInitialTextColor());
                Glide.with(context)
                        .load(user.getImage())
                        .asBitmap()
                        .into(imageView);

                RelativeLayout.LayoutParams params;

                if (lastActiveUsers.size() == 1) {
                    factor_ = 1.0;
                    imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                            (int) (style.getAvatarInitialTextSize() / factor_),
                            style.getAvatarInitialTextStyle());
                    params = new RelativeLayout.LayoutParams(
                            (int) (style.getAvatarWidth() / factor_),
                            (int) (style.getAvatarHeight() / factor_));

                } else {
                    factor_ = factor;
                    imageView.setBorderWidth(TypedValue.COMPLEX_UNIT_PX,
                            (int) style.getAvatarBorderWidth());
                    imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                            (int) (style.getAvatarInitialTextSize() / factor_),
                            style.getAvatarInitialTextStyle());
                    params = new RelativeLayout.LayoutParams(
                            (int) (style.getAvatarWidth() / factor_),
                            (int) (style.getAvatarHeight() / factor_));

                    if (lastActiveUsers.size() == 2) {
                        switch (i) {
                            case 0:
                                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                break;
                            default:
                                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                params.setMarginEnd(20);
                                break;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                break;
                            case 1:
                                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                break;
                            default:
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                                break;
                        }
                    }
                }
                imageView.setLayoutParams(params);
                this.addView(imageView);
            }
        } else {
            View v = inflater.inflate(R.layout.view_user_avatar_initials, null);
            v.setLayoutParams(new RelativeLayout.LayoutParams(
                    (int) style.getAvatarWidth(),
                    (int) style.getAvatarHeight()));
            TextView textView = v.findViewById(R.id.tv_initials);
            textView.setText(channel.getInitials());
            applyTextViewStyle(textView, style, 1.0f);
            this.addView(v);
        }
    }

    private void applyTextViewStyle(TextView textView, STYLE style, double factor) {
        textView.setTextColor(style.getAvatarInitialTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (style.getAvatarInitialTextSize() / factor));
        textView.setTypeface(textView.getTypeface(), style.getAvatarInitialTextStyle());
    }

}
