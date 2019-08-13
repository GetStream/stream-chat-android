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

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.BaseStyle;
import com.getstream.sdk.chat.utils.Utils;

import java.util.List;

/**
 * Avatar group views renders a list of avatars for a channel
 * There is quite a bit of business logic in here, it's specific to the channel scenario
 * Here's how it should work
 * <p>
 * 1 - If a channel image is available use that
 * 2 - If there are members (not all channels have members) render up to 3 avatars sorted based on last active (if there are more than 3 members, we still render 3). For each member if there is no image fallback to their initial.
 * 3 - If there are no members, and there is no channel image fallback to the channel initial
 */
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
            for (int i = 0; i < lastActiveUsers.size(); i++) {
                User user = lastActiveUsers.get(i);
                View v = inflater.inflate(R.layout.view_user_avatar_initials, null);

                TextView textView = v.findViewById(R.id.tv_initials);
                ImageView imageView = v.findViewById(R.id.cv_avatar);

                textView.setText(user.getUserInitials());
                Utils.circleImageLoad(imageView, user.getImage());

                if (lastActiveUsers.size() == 1) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            (int) style.getAvatarWidth(),
                            (int) style.getAvatarHeight());
                    v.setLayoutParams(params);

                    setTextViewStyle(textView, style, 1.0f);

                } else if (lastActiveUsers.size() == 2) {
                    double width = style.getAvatarWidth() / factor;
                    double height = style.getAvatarHeight() / factor;
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width), (int) (height));
                    switch (i) {
                        case 0:
                            params.addRule(RelativeLayout.ALIGN_PARENT_START);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            break;
                        default:
                            params.addRule(RelativeLayout.ALIGN_PARENT_END);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            break;
                    }
                    v.setLayoutParams(params);

                    setTextViewStyle(textView, style, (float) factor);

                } else {

                    double width = style.getAvatarWidth() / factor;
                    double height = style.getAvatarHeight() / factor;
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width), (int) (height));
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
                    v.setLayoutParams(params);
                    setTextViewStyle(textView, style, (float) factor);
                }
                this.addView(v);
            }
        } else {
            View v = inflater.inflate(R.layout.view_user_avatar_initials, null);
            v.setLayoutParams(new RelativeLayout.LayoutParams(
                    (int) style.getAvatarWidth(),
                    (int) style.getAvatarHeight()));
            TextView textView = v.findViewById(R.id.tv_initials);
            textView.setText(channel.getInitials());
            setTextViewStyle(textView, style, 1.0f);
            this.addView(v);
        }
    }

    private void setTextViewStyle(TextView textView, STYLE style, float factor) {
        textView.setTextColor(style.getInitialsTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getInitialsTextSize() / factor);
        textView.setTypeface(textView.getTypeface(), style.getInitialsTextStyle());
    }
}
