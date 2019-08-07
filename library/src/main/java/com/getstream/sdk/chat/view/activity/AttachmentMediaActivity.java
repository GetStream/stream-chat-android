package com.getstream.sdk.chat.view.activity;

import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.getstream.sdk.chat.utils.exomedia.ui.widget.VideoView;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.utils.Global;

import java.util.List;

/**
 * An Activity playing attachments such as audio and videos.
 */
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

        playVideo(attachment.getAssetURL());
    }

    /**
     * Play media file with url
     *
     * @param url media url
     */
    public void playVideo(String url) {
        videoView.setVideoURI(Uri.parse(url));
        videoView.setOnPreparedListener(() -> videoView.start());
    }
}
