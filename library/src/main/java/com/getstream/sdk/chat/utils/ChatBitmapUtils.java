package com.getstream.sdk.chat.utils;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class ChatBitmapUtils {
    /**
     * Catching OutOfMemoryError and returns null instead of bitmap.
     * Should be used only in exceptional cases when it's not possible to avoid crash.
     */
    @Nullable
    public static Bitmap createSafeBitmap(int width, int height, Bitmap.Config config) {

        try {
            Bitmap result = Bitmap.createBitmap(width, height, config);
            return result;
        } catch (OutOfMemoryError e) {
            // ignore
            return null;
        }
    }

    public static void recycleIfNeeded(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
    }
}
