package io.getstream.chat.example;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.view.MessageListView;
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
        implements MessageListView.MessageClickListener,
        MessageListView.AttachmentClickListener,
        MessageInputView.OpenCameraViewListener {

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
        binding.messageList.setMessageClickListener(this);
        binding.messageList.setAttachmentClickListener(this);

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

    @Override
    public void onClick(Message message) {
        Log.i(TAG, "message was clicked");
        showReactionDialog(message);
    }

    @Override
    public void onClick(Message message, Attachment attachment) {
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

    }

    public void showReactionDialog(Message message) {
        final Dialog dialog = new Dialog(this); // Context, this, etc.
        dialog.setContentView(com.getstream.sdk.chat.R.layout.dialog_reaction);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rv_reaction = dialog.findViewById(com.getstream.sdk.chat.R.id.rv_reaction);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(binding.getViewModel().getChannel(), message, true, (View v) -> {
            dialog.dismiss();
        });
        rv_reaction.setAdapter(reactionAdapter);

        dialog.show();
    }
}