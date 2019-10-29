package com.getstream.sdk.chat;

import android.widget.TextView;

public class MarkdownImpl extends MarkDown {
    
    private static MarkdownListener markdownListener;


    public static MarkdownListener getMarkdownListener() {
        return markdownListener;
    }

    public static void setMarkdownListener(MarkdownListener markdownListener) {
        MarkdownImpl.markdownListener = markdownListener;
    }

    public interface MarkdownListener {
        void setText(TextView textView, String text);
    }
}
