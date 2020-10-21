package com.getstream.sdk.chat;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

class ChatMarkdownImpl implements ChatMarkdown {
    
//    private static MarkdownHandler markdownHandler;
//
//    public static MarkdownHandler getMarkdownHandler() {
//        return markdownHandler;
//    }
//
//    public static void setMarkdownHandler(MarkdownHandler markdownHandler) {
//        MarkdownImpl.markdownHandler = markdownHandler;
//    }
//
//    public interface MarkdownHandler {
//        void setText(TextView textView, String text);
//    }

    private Markwon markwon;

    public ChatMarkdownImpl(Context context) {
        markwon = Markwon.builder(context)
                .usePlugin(CorePlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .build();
    }

    public void setText(@NonNull TextView textView, @NonNull String text) {
        markwon.setMarkdown(textView, text);
    }
}
