package com.getstream.sdk.chat.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.style.FontsManager;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class AvatarGroupView<STYLE extends BaseStyle> extends RelativeLayout {

    private static final String TAG = AvatarGroupView.class.getSimpleName();
    Context context;
    Channel channel;
    STYLE style;
    List<User> lastActiveUsers;
    User user;
    float factor = 1.7f;

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
        if (params != null) {
            params.width = style.getAvatarWidth();
            params.height = style.getAvatarHeight();
            this.setLayoutParams(params);
        }

        removeAllViews();
        if (user != null) {
            configSingleAvatar(user.getImage(), user.getInitials());
        } else if (!TextUtils.isEmpty(channel.getImage())) {
            configSingleAvatar(channel.getImage(), channel.getInitials());
        } else {
            configUserAvatars();
        }
    }

    private void configUserAvatars() {

        if (lastActiveUsers != null && !lastActiveUsers.isEmpty()) {
            for (int i = 0; i < Math.min(lastActiveUsers.size(), 3); i++) {
                User user_ = lastActiveUsers.get(i);
                if (lastActiveUsers.size() == 1) {
                    configSingleAvatar(user_.getImage(), user_.getInitials());
                } else {
                    CircularImageView imageView = new CircularImageView(context);
                    configAvatarView(imageView, user_.getImage(), user_.getInitials(), factor);
                    imageView.setBorderWidth(TypedValue.COMPLEX_UNIT_PX,
                            style.getAvatarBorderWidth());

                    RelativeLayout.LayoutParams params;
                    params = new RelativeLayout.LayoutParams(
                            (int) (style.getAvatarWidth() / factor),
                            (int) (style.getAvatarHeight() / factor));

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
            configSingleAvatar(channel.getImage(), channel.getInitials());
        }
    }

    private void configSingleAvatar(String image, String initial) {
        CircularImageView imageView = new CircularImageView(context);
        configAvatarView(imageView, image, initial, 1);
        RelativeLayout.LayoutParams params;
        params = new RelativeLayout.LayoutParams(
                (style.getAvatarWidth()),
                (style.getAvatarHeight()));
        imageView.setLayoutParams(params);
        this.addView(imageView);
    }

    private void configAvatarView(CircularImageView imageView, String image, String initial, float factor) {
        imageView.setBorderColor(style.getAvatarBorderColor());
        imageView.setPlaceholder(initial,
                style.getAvatarBackGroundColor(),
                style.avatarInitialText.color);

        if (!Utils.isSVGImage(image))
            Glide.with(context)
                    .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(image))
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);

        FontsManager fontsManager = StreamChat.getFontsManager();
        fontsManager.setFont(style.avatarInitialText, imageView, factor);
    }
}
