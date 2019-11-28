package com.getstream.sdk.chat.style;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.getstream.sdk.chat.StreamChat;

import androidx.annotation.Nullable;

public class TextStyle {

    public int fontResource = -1;
    public String fontAssetsPath = null;
    public int style = -1;
    public int size = -1;
    public int color = 0;
    public int hintColor = 0;

    @Nullable
    public Typeface getFont() {
        FontsManager fontsManager = StreamChat.getFontsManager();
        return fontsManager.getFont(this);
    }

    public void apply(TextView textView) {
        FontsManager fontsManager = StreamChat.getFontsManager();

        if (size != -1)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

        if (color != 0)
            textView.setTextColor(color);

        if (hintColor != 0)
            textView.setHintTextColor(hintColor);

        fontsManager.setFont(this, textView);
    }

    public boolean hasFont() {
        return fontAssetsPath != null || fontResource != -1;
    }

    public static class Builder {

        private final TypedArray array;
        private final TextStyle result;

        public Builder(TypedArray array) {
            this.array = array;
            result = new TextStyle();
        }

        public Builder size(int ref) {
            return size(ref, -1);
        }

        public Builder size(int ref, int defValue) {
            result.size = array.getDimensionPixelSize(ref, defValue);
            return this;
        }

        public Builder font(int assetsPath, int resId) {
            result.fontAssetsPath = array.getString(assetsPath);
            result.fontResource = array.getResourceId(resId, -1);
            return this;
        }

        public Builder color(int ref, int defValue) {
            result.color = array.getColor(ref, defValue);
            return this;
        }

        public Builder hintColor(int ref, int defValue) {
            result.hintColor = array.getColor(ref, defValue);
            return this;
        }

        public Builder style(int ref, int defValue) {
            result.style = array.getInt(ref, defValue);
            return this;
        }

        public TextStyle build() {
            return result;
        }
    }
}
