package com.getstream.sdk.chat;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

public class MarkDown {

    private static MarkDown instance;
    private Markwon markwon;

    public MarkDown(Context context) {
        markwon = Markwon.builder(context)
                .usePlugin(CorePlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(StrikethroughPlugin.create())
                .build();
    }

    public void setMarkDown(@NonNull TextView textView, @NonNull String text){
        markwon.setMarkdown(textView, text);
    }

    public static MarkDown getInstance(Context context) {
        if (instance == null)
            instance = new MarkDown(context);
        return instance;
    }
}
