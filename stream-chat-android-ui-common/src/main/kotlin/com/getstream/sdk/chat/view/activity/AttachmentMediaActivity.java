package com.getstream.sdk.chat.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.getstream.sdk.chat.ChatUI;
import com.getstream.sdk.chat.utils.exomedia.ui.widget.VideoView;

import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.ui.common.R;

/**
 * An Activity playing attachments such as stream_ic_audio and videos.
 */
public class AttachmentMediaActivity extends AppCompatActivity {

    public static final String TYPE_KEY = "type";
    public static final String URL_KEY = "url";

    VideoView videoView;
    ImageView iv_audio;

    private final TaggedLogger logger = ChatLogger.Companion.get("AttachmentMediaActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_activity_attachment_media);
        videoView = findViewById(R.id.videoView);
        iv_audio = findViewById(R.id.iv_audio);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        String type = intent.getStringExtra(TYPE_KEY);
        String url = intent.getStringExtra(URL_KEY);
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(url)) {
            logger.logE("This file can't be displayed. The TYPE or the URL are null");
            Toast.makeText(this, getString(R.string.stream_ui_attachment_display_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if (type.contains("audio"))
            iv_audio.setVisibility(View.VISIBLE);
        else
            iv_audio.setVisibility(View.GONE);


        playVideo(ChatUI.instance().getUrlSigner().signFileUrl(url));
    }

    /**
     * Play media file with url
     *
     * @param url media url
     */
    public void playVideo(String url) {
        videoView.setVideoURI(Uri.parse(ChatUI.instance().getUrlSigner().signFileUrl(url)));
        videoView.setOnPreparedListener(() -> videoView.start());
    }
}
