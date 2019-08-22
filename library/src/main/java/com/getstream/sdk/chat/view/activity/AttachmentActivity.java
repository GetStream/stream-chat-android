package com.getstream.sdk.chat.view.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;
import com.pierfrancescosoffritti.youtubeplayer.player.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView;

import java.util.List;

/**
 * An Activity showing attachments such as websites, youtube and giphy.
 */
public class AttachmentActivity extends AppCompatActivity {

    private final String TAG = AttachmentActivity.class.getSimpleName();
    WebView webView;
    YouTubePlayerView youtube_player_view;
    ImageView iv_image;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);

        youtube_player_view = findViewById(R.id.youtube_player_view);
        webView = findViewById(R.id.webView);
        iv_image = findViewById(R.id.iv_image);
        progressBar = findViewById(R.id.progressBar);

        configUIs();

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String url = intent.getStringExtra("url");
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Something error!", Toast.LENGTH_SHORT);
            return;
        }
        showAttachment(type, url);
    }


    private void configUIs() {
        iv_image.setVisibility(View.GONE);
        youtube_player_view.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        // WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new AppWebViewClients());
    }

    private void showAttachment(final String type, final String url) {
        switch (type) {
            case ModelType.attach_video:
                if(url.toLowerCase().equals("youtube")){
                    playYoutube(url);
                }else{
                    loadUrlToWeb(url);
                }
                break;
            case ModelType.attach_giphy:
                showGiphy(url);
                break;
            case ModelType.attach_image:
                break;
            case ModelType.attach_link:
            case ModelType.attach_product:
                loadUrlToWeb(url);
                break;
            case ModelType.attach_file:
                break;
            default:
                break;
        }
    }

    /**
     * Show web view with url
     *
     * @param url web url
     */
    public void loadUrlToWeb(String url) {
        iv_image.setVisibility(View.GONE);
        youtube_player_view.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }


    /**
     * Play youtube with url
     *
     * @param url youtube url
     */
    public void playYoutube(String url) {
        iv_image.setVisibility(View.GONE);
        youtube_player_view.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        String[] array = url.split("v=");
        final String videoId = array[1];
        youtube_player_view.initialize((final YouTubePlayer initializedYouTubePlayer) -> {
            initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady() {
                    initializedYouTubePlayer.loadVideo(videoId, 0);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }, true);
    }

    /**
     * Play giphy with url
     *
     * @param url giphy url
     */
    public void showGiphy(String url) {
        if (url == null) {
            Utils.showMessage(this, "Error!");
            return;
        }
        iv_image.setVisibility(View.VISIBLE);
        youtube_player_view.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(iv_image);
    }

    private class AppWebViewClients extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }
}
