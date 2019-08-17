package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class ReactionDlgView extends RelativeLayout implements View.OnClickListener {

    final String TAG = ReactionDlgView.class.getSimpleName();
    MessageListViewStyle style;
    public ReactionDlgView(Context context) {
        super(context);

    }

    public ReactionDlgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        applyStyle();
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes

    }

    public void setStyle(MessageListViewStyle style){
        this.style = style;
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
