package com.getstream.sdk.chat.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.utils.Global;
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

    private final String YouTube = "YouTube";

    int index;
    List<Attachment> attachments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);

        youtube_player_view = findViewById(R.id.youtube_player_view);
        webView = findViewById(R.id.webView);
        iv_image = findViewById(R.id.iv_image);
        progressBar = findViewById(R.id.progressBar);

        iv_image.setVisibility(View.GONE);
        youtube_player_view.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        init();
        configUIs();
        showAttachment(attachments.get(0));
    }


    private void init() {
        if (Global.selectAttachmentModel == null) {
            Log.d(TAG, "Model is NULL");
            return;
        }

        index = Global.selectAttachmentModel.getAttachmentIndex();
        attachments = Global.selectAttachmentModel.getAttachments();
        if (attachments == null) {
            Log.d(TAG, "index is : " + index);
            return;
        }

        iv_image.setVisibility(View.GONE);
        youtube_player_view.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
    }

    private void configUIs() {
        // WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new AppWebViewClients());
    }

    private void showAttachment(Attachment attachment) {
        String url = null;
        String type = null;
        switch (attachment.getType()) {
            case ModelType.attach_video:
                url = attachment.getTitleLink();
                if (url.contains("giphy"))
                    type = ModelType.attach_giphy;
                break;
            case ModelType.attach_giphy:
                url = attachment.getThumbURL();
                break;
            case ModelType.attach_image:
                if (attachment.getOgURL() != null) {
                    url = attachment.getOgURL();
                    type = ModelType.attach_link;
                } else {
                    url = attachment.getImageURL();
                }
                break;
            case ModelType.attach_product:
                url = attachment.getUrl();
                break;
            case ModelType.attach_file:
                break;
            default:
                break;
        }
        if (type == null) type = attachment.getType();

        switch (type) {
            case ModelType.attach_video:
                if(attachment.getAuthor().toLowerCase().equals("youtube")){
                    playYoutube(url);
                }else{
                    loadUrlToWeb(attachment.getAssetURL());
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
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(iv_image);
    }

    private class AppWebViewClients extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }
}
