package com.getstream.sdk.chat.media;

import android.webkit.WebView;
import android.widget.ImageView;

import com.bumptech.glide.load.model.GlideUrl;
import com.getstream.sdk.chat.utils.exomedia.ui.widget.VideoView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * Encapsulates images/videos caching and urls signing.
 * Not completed, shouldn't expose signFileUrl and signGlideUrl
 */
public interface MediaLoader {
    void loadImageInto(String url, ImageView imageView);
    void loadImageInto(String url, ImageView imageView, @DrawableRes int placeHolder);
    void loadFileInto( String url, WebView webView);
    void setVideoURI(String url, VideoView videoView);

    String signFileUrl(String url);

    GlideUrl signGlideUrl(@Nullable String url);
}
