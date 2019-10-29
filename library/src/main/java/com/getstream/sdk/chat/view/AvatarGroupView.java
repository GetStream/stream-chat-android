package com.getstream.sdk.chat.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.List;

public class AvatarGroupView<STYLE extends BaseStyle> extends RelativeLayout {

    private static final String TAG = AvatarGroupView.class.getSimpleName();
    Context context;
    Channel channel;
    STYLE style;
    List<User> lastActiveUsers;
    User user;
    double factor = 1.7;

    public AvatarGroupView(Context context) {
        super(context);
        this.context = context;
    }

    public AvatarGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public AvatarGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public AvatarGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public void setChannelAndLastActiveUsers(Channel channel, List<User> lastActiveUsers, @NonNull STYLE style) {
        this.channel = channel;
        this.lastActiveUsers = lastActiveUsers;
        this.user = null;
        this.style = style;
        configUIs();
    }

    public void setUser(User user, @NonNull STYLE style) {
        this.user = user;
        this.style = style;
        this.channel = null;
        this.lastActiveUsers = null;
        configUIs();
    }
    private void configUIs() {

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) this.getLayoutParams();
        if (params != null){
            params.width = style.getAvatarWidth();
            params.height = style.getAvatarHeight();
            this.setLayoutParams(params);
        }

        removeAllViews();
        if (user != null) {
            configAvatar(user.getImage(), user.getInitials());
        } else if (!TextUtils.isEmpty(channel.getImage())) {
            configAvatar(channel.getImage(), channel.getInitials());
        } else {
            configUserAvatars();
        }
    }

    private void configUserAvatars() {
        double factor_;
        if (lastActiveUsers != null && !lastActiveUsers.isEmpty()) {
            for (int i = 0; i < Math.min(lastActiveUsers.size(), 3); i++) {
                User user_ = lastActiveUsers.get(i);
                if (lastActiveUsers.size() == 1) {
                    configAvatar(user_.getImage(), user_.getInitials());
                } else {
                    CircularImageView imageView = new CircularImageView(context);

                    imageView.setBorderColor(style.getAvatarBorderColor());
                    imageView.setPlaceholder(user_.getInitials(),
                            style.getAvatarBackGroundColor(),
                            style.getAvatarInitialTextColor());

                    if (!Utils.isSVGImage(user_.getImage()))
                        Glide.with(context)
                                .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(user_.getImage()))
                                .apply(RequestOptions.circleCropTransform())
                                .into(imageView);

                    RelativeLayout.LayoutParams params;
                    factor_ = factor;
                    imageView.setBorderWidth(TypedValue.COMPLEX_UNIT_PX,
                            style.getAvatarBorderWidth());
                    imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                            (int) (style.getAvatarInitialTextSize() / factor_),
                            style.getAvatarInitialTextStyle());
                    params = new RelativeLayout.LayoutParams(
                            (int) (style.getAvatarWidth() / factor_),
                            (int) (style.getAvatarHeight() / factor_));

                    if (lastActiveUsers.size() == 2) {
                        if (i == 0) {
                            params.addRule(RelativeLayout.ALIGN_PARENT_START);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        } else {
                            params.addRule(RelativeLayout.ALIGN_PARENT_END);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            params.setMarginEnd(20);
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
                    imageView.setLayoutParams(params);
                    this.addView(imageView);
                }
            }
        } else {
            configAvatar(channel.getImage(), channel.getInitials());
        }
    }

    private void configAvatar(String image, String initial) {
        CircularImageView imageView = new CircularImageView(context);
        imageView.setBorderColor(style.getAvatarBorderColor());
        imageView.setPlaceholder(initial,
                style.getAvatarBackGroundColor(),
                style.getAvatarInitialTextColor());

        if (!Utils.isSVGImage(image))
            Glide.with(context)
                    .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(image))
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);

        RelativeLayout.LayoutParams params;
        imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                (style.getAvatarInitialTextSize()),
                style.getAvatarInitialTextStyle());
        params = new RelativeLayout.LayoutParams(
                (style.getAvatarWidth()),
                (style.getAvatarHeight()));
        imageView.setLayoutParams(params);
        this.addView(imageView);
    }

}
