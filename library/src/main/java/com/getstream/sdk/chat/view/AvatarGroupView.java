package com.getstream.sdk.chat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;

import java.util.List;

/**
 * Avatar group views renders a list of avatars for a channel
 * There is quite a bit of business logic in here, it's specific to the channel scenario
 * Here's how it should work
 *
 * 1 - If a channel image is available use that
 * 2 - If there are members (not all channels have members) render up to 3 avatars sorted based on last active (if there are more than 3 members, we still render 3). For each member if there is no image fallback to their initial.
 * 3 - If there are no members, and there is no channel image fallback to the channel initial
 *
 */
public class AvatarGroupView extends View {
    Context context;
    Channel channel;
    List<User> otherUsers;

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

    public void setChannelAndOtherUsers(Channel channel, List<User> otherUsers) {
        this.channel = channel;
        this.otherUsers = otherUsers;
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.user_intials_background));
        paint.setStyle(Paint.Style.FILL);
        float radius = getWidth()/2f;
        canvas.drawCircle(getWidth()/2f, getWidth()/2f, radius, paint);
    }
}
