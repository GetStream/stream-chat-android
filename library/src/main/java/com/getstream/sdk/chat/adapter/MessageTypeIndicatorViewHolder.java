package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;

public class MessageTypeIndicatorViewHolder extends RecyclerView.ViewHolder {
    // region LifeCycle
    final String TAG = MessageTypeIndicatorViewHolder.class.getSimpleName();
    private LinearLayout ll_typingusers;
    private ImageView iv_typing_indicator;
    private Context context;


    public MessageTypeIndicatorViewHolder(View itemView) {
        super(itemView);
        ll_typingusers = itemView.findViewById(R.id.ll_typing_indicator);
        iv_typing_indicator = itemView.findViewById(R.id.iv_typing_indicator);
    }

    public void bind(Context context ) {
        // set binding
        this.context = context;
        configTypingIndicator();
    }
    // endregion

    // region Config UIs
    private void configTypingIndicator() {

        if (Global.typingUsers.size() > 0) {
            ll_typingusers.setVisibility(View.VISIBLE);
            iv_typing_indicator.setVisibility(View.VISIBLE);
            createTypingUsersView();
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_typing_indicator);
            Glide.with(context).load(R.raw.typing).into(imageViewTarget);
        }
    }
    private void createTypingUsersView() {
        ll_typingusers.removeAllViews();
        Resources resources = context.getResources();
        float marginLeft = resources.getDimension(R.dimen.user_avatar_margin_left);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < Global.typingUsers.size(); i++) {
            View v = vi.inflate(R.layout.view_user_avatar_initials, null);
            User user = Global.typingUsers.get(i);
            TextView textView = v.findViewById(R.id.tv_initials);
            ImageView imageView = v.findViewById(R.id.cv_avatar);
            textView.setText(user.getUserInitials());
            Utils.circleImageLoad(imageView, user.getImage());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i == 0) {
                params.setMargins(0, 0, 0, 0);
            } else {
                params.setMargins(-(int) marginLeft, 0, 0, 0);
            }
            v.setLayoutParams(params);
            ll_typingusers.addView(v);
        }
    }
}
