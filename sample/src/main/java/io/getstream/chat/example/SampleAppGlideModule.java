package io.getstream.chat.example;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class SampleAppGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setLogLevel(Log.VERBOSE);
    }
}
