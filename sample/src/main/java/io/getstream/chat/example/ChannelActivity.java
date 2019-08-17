package io.getstream.chat.example;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.AttachmentViewHolder;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.view.activity.AttachmentActivity;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.example.databinding.ActivityChannelBinding;

/**
 * Show the messages for a channel
 */
public class ChannelActivity extends AppCompatActivity
        implements MessageInputView.OpenCameraViewListener {

    final String TAG = ChannelActivity.class.getSimpleName();

    private ChannelViewModel viewModel;
    private ActivityChannelBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // receive the intent and create a channel object
        Intent intent = getIntent();
        String channelType = intent.getStringExtra(MainActivity.EXTRA_CHANNEL_TYPE);
        String channelID = intent.getStringExtra(MainActivity.EXTRA_CHANNEL_ID);
        Client client = StreamChat.getInstance(getApplication());


        // we're using data binding in this example
        binding =
                DataBindingUtil.setContentView(this, R.layout.activity_channel);
        // most the business logic of the chat is handled in the ChannelViewModel view model
        binding.setLifecycleOwner(this);

        Channel channel = client.getChannelByCid(channelType + ":" + channelID);
        if (channel == null)
            channel = client.channel(channelType, channelID);
        viewModel = ViewModelProviders.of(this,
                new ChannelViewModelFactory(this.getApplication(), channel)
        ).get(ChannelViewModel.class);

        // connect the view model
        binding.channelHeader.setViewModel(viewModel, this);
        binding.channelHeader.setOnBackClickListener(v -> finish());

        MyMessageViewHolderFactory factory = new MyMessageViewHolderFactory();
        binding.messageList.setViewHolderFactory(factory);
        binding.messageList.setMessageClickListener(message -> {
            Log.i(TAG, "message was clicked");
        });
        binding.messageList.setAttachmentClickListener((message, attachment) -> {
            Log.i(TAG, "attachment was clicked");
            // Image

            if (attachment.getType().equals(ModelType.attach_image)) {
                List<String> imageUrls = new ArrayList<>();
                for (Attachment a : message.getAttachments()) {
                    imageUrls.add(a.getImageURL());
                }

                int position = message.getAttachments().indexOf(attachment);

                new ImageViewer.Builder<>(this, imageUrls)
                        .setStartPosition(position)
                        .show();
            } else {
                // Giphy, Video, Link, Product,...
                Intent mediaIntent = new Intent(this, AttachmentActivity.class);
                this.startActivity(mediaIntent);
            }

        });

        binding.messageInput.setViewModel(viewModel, this);
        binding.messageList.setViewModel(viewModel, this);
        binding.messageInput.setOpenCameraViewListener(this);
        // set the viewModel data for the activity_channel.xml layout
        binding.setViewModel(viewModel);
        // Permission Check
        PermissionChecker.permissionCheck(this, null);
    }

    @Override
    public void onBackPressed() {
        viewModel.removeEventHandler();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int grantResult : grantResults)
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            if (!granted) PermissionChecker.showRationalDialog(this, null);
        }
    }

    @Override
    public void openCameraView(Intent intent, int REQUEST_CODE) {
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.messageInput.progressCapturedMedia(requestCode, resultCode, data);
    }

    private List<String> getImageURLs(List<Attachment> attachments) {
        List<String> imageURLs = new ArrayList<>();
        if (attachments.size() == 1) {
            if (attachments.get(0).getType().equals(ModelType.attach_image)) {
                if (attachments.get(0).getOgURL() == null) {
                    String url = attachments.get(0).getImageURL();
                    imageURLs.add(url);
                }
            }
        } else {
            Attachment attachment = attachments.get(0);
            if (attachment.getType().equals(ModelType.attach_image)) {
                for (int i = 0; i < attachments.size(); i++) {
                    if (attachments.get(i).getOgURL() == null && attachments.get(i).getImageURL() != null) {
                        imageURLs.add(attachments.get(i).getImageURL());
                    }
                }
            }
        }
        return imageURLs;
    }
}