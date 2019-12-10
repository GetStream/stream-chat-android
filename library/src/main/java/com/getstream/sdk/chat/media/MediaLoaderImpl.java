package com.getstream.sdk.chat.media;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.getstream.sdk.chat.utils.exomedia.ui.widget.VideoView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class MediaLoaderImpl implements MediaLoader {

    private final Context context;

    public MediaLoaderImpl(Context context) {

        this.context = context;
    }

    @Override
    public void loadImageInto(String url, ImageView imageView) {
        loadImageInto(url, imageView, 0);
    }

    @Override
    public void loadImageInto(String url, ImageView imageView, @DrawableRes int placeHolder) {
        Glide.with(context)
                .load(signGlideUrl(url))
                .placeholder(placeHolder)
                .into(imageView);
    }

    @Override
    public void loadFileInto(String url, WebView webView) {
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + signFileUrl(url));
    }

    @Override
    public void setVideoURI(String url, VideoView videoView) {
        videoView.setVideoURI(Uri.parse(signFileUrl(url)));
    }

    @Override
    public String signFileUrl(String url) {
        return url;
    }

    @Override
    @Nullable
    public GlideUrl signGlideUrl(@Nullable String url) {
        if (url == null) return null;
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("X-requested-by", "stream")
                .build());
    }
}
