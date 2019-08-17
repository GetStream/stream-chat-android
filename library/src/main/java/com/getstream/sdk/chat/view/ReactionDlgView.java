package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;

public class ReactionDlgView extends RelativeLayout implements View.OnClickListener {

    final String TAG = ReactionDlgView.class.getSimpleName();
    MessageListViewStyle style;

    public ReactionDlgView(Context context) {
        super(context);
        initView();

    }

    public ReactionDlgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ReactionDlgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void setMessagewithStyle(Channel channel, Message message, View.OnClickListener clickListener, MessageListViewStyle style) {
        this.style = style;
        init(channel, message, clickListener);
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.dialog_reaction, null);
        addView(view);
    }

    private void init(Channel channel, Message message, View.OnClickListener clickListener) {
        RecyclerView rv_reaction = this.findViewById(R.id.rv_reaction);
        ImageView iv_bg = this.findViewById(R.id.iv_bg);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(channel, message, style.isShowUsersReactionDlg(), style, clickListener);
        rv_reaction.setAdapter(reactionAdapter);
        iv_bg.setBackground(style.getReactionDlgBgDrawable());
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

    }

    public interface OnBackClickListener {
        void onClick(View v);
    }


}
