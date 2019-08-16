
package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

/**
 * Style for MessageInputStyle customization by xml attributes
 */
class ReadStateStyle extends BaseStyle {

    private boolean showReadState;

    private float readStateAvatarWidth;
    private float readStateAvatarHeight;

    private int readStateTextSize;
    private int readStateTextColor;
    private int readStateTextStyle;

    public ReadStateStyle(Context context, AttributeSet attrs) {
        setContext(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageInputView);
        // Attachment Button
        showReadState = a.getBoolean(R.styleable.MessageInputView_showAttachmentButton, true);

        readStateAvatarWidth = a.getDimension(R.styleable.ChannelListView_channelAvatarWidth, getDimension(R.dimen.read_state_avatar_width));
        readStateAvatarHeight = a.getDimension(R.styleable.ChannelListView_channelAvatarHeight, getDimension(R.dimen.read_state_avatar_height));

        readStateTextSize = a.getDimensionPixelSize(R.styleable.MessageInputView_inputTextSize, getDimension(R.dimen.input_text_size));
        readStateTextColor = a.getColor(R.styleable.MessageInputView_inputTextColor, getColor(R.color.black));
        readStateTextStyle = a.getInt(R.styleable.MessageInputView_inputTextStyle, Typeface.NORMAL);

        a.recycle();
    }



}
