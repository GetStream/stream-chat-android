package com.getstream.sdk.chat.style;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.TextView;

import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.FontRes;
import androidx.core.content.res.ResourcesCompat;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;

public class ChatFontsImpl implements ChatFonts {

    private final ChatStyle style;
    private final Context context;
    private SparseArray<Typeface> resourceMap = new SparseArray<>();
    private Map<String, Typeface> pathMap = new HashMap<>();
    private final TaggedLogger logger = ChatLogger.Companion.get(ChatFonts.class.getSimpleName());

    public ChatFontsImpl(ChatStyle style, Context context) {
        this.style = style;
        this.context = context;
    }

    @SuppressLint("WrongConstant")
    public void setFont(TextStyle textStyle, TextView textView) {

        Typeface font = textStyle.getFont();

        if (font != null) {
            textView.setTypeface(font, textStyle.style);
        } else {
            setDefaultFont(textView, textStyle.style);
        }
    }

    @Override
    public void setFont(TextStyle textStyle, CircularImageView imageView, float factor) {

        Typeface font = textStyle.getFont();

        if (font != null) {
            imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (textStyle.size / factor), font);
        } else {

            if (style.hasDefaultFont()) {
                TextStyle defaultTextStyle = style.getDefaultTextStyle();
                imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (textStyle.size / factor), getFont(defaultTextStyle));
            } else {
                imageView.setPlaceholderTextSize(TypedValue.COMPLEX_UNIT_PX,
                        (int) (textStyle.size / factor),
                        textStyle.style);
            }
        }
    }

    @Override
    public Typeface getFont(TextStyle textStyle) {
        if (textStyle.fontResource != -1) {
            return getFont(textStyle.fontResource);
        } else if (textStyle.fontAssetsPath != null && !"".equals(textStyle.fontAssetsPath)) {
            return getFont(textStyle.fontAssetsPath);
        } else {
            return null;
        }
    }

    private Typeface getFont(String fontPath) {
        Typeface result;

        if (pathMap.containsKey(fontPath)) {
            result = pathMap.get(fontPath);
        } else {
            result = safeLoadTypeface(fontPath);
            pathMap.put(fontPath, result);
        }

        return result;
    }

    private Typeface getFont(@FontRes int fontRes) {

        Typeface result;

        if (resourceMap.get(fontRes) != null) {
            result = resourceMap.get(fontRes);
        } else {
            result = safeLoadTypeface(fontRes);
            resourceMap.put(fontRes, result);
        }

        return result;
    }

    private void setDefaultFont(TextView textView, int textStyle) {

        if (style.hasDefaultFont()) {
            textView.setTypeface(getFont(style.getDefaultTextStyle()), textStyle);
        } else {
            textView.setTypeface(Typeface.DEFAULT, textStyle);
        }
    }

    private Typeface safeLoadTypeface(int fontRes) {
        try {
            return ResourcesCompat.getFont(context, fontRes);
        } catch (Throwable t) {
            logger.logE(t);
            return null;
        }
    }

    private Typeface safeLoadTypeface(String fontPath) {
        try {
            return Typeface.createFromAsset(context.getAssets(), fontPath);
        } catch (Throwable t) {
            logger.logE(t);
            return null;
        }
    }
}
