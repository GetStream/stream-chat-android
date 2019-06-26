package com.getstream.sdk.chat.view.activity;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.getstream.sdk.chat.utils.exomedia.listener.OnPreparedListener;
import com.getstream.sdk.chat.utils.exomedia.ui.widget.VideoView;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.message.Attachment;
import com.getstream.sdk.chat.utils.Global;

import java.util.List;

public class AttachmentMediaActivity extends AppCompatActivity {

    VideoView videoView;
    ImageView iv_audio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment_media);
        videoView = findViewById(R.id.videoView);
        iv_audio = findViewById(R.id.iv_audio);


        int index = Global.selectAttachmentModel.getAttachmentIndex();
        List<Attachment> attachments = Global.selectAttachmentModel.getAttachments();

        Attachment attachment = attachments.get(index);
        if (attachment.getMime_type().contains("audio"))
            iv_audio.setVisibility(View.VISIBLE);
        else
            iv_audio.setVisibility(View.GONE);

        videoView.setVideoURI(Uri.parse(attachment.getAssetURL()));
        setupVideoView();
    }

    private void setupVideoView() {
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoView.start();
            }
        });
    }
}
