package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class ReactionView extends RelativeLayout implements View.OnClickListener {

    final String TAG = ReactionView.class.getSimpleName();

    public ReactionView(Context context) {
        super(context);

    }

    public ReactionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        applyStyle();
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

    }

    public interface OnBackClickListener {
        void onClick(View v);
    }

    private void applyStyle() {
        // Title
    }
}
