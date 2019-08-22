package io.getstream.chat.example;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer;
import com.getstream.sdk.chat.view.Dialog.ReactionDialog;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.Dialog.MoreActionDialog;
import com.getstream.sdk.chat.view.activity.AttachmentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentMediaActivity;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.example.databinding.ActivityChannelBinding;

/**
 * Show the messages for a channel
 */
public class ChannelActivity extends AppCompatActivity
        implements MessageListView.MessageClickListener,
        MessageListView.MessageLongClickListener,
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel);
        // most the business logic of the chat is handled in the ChannelViewModel view model
        binding.setLifecycleOwner(this);

        Channel channel = client.getChannelByCid(channelType + ":" + channelID);
        if (channel == null)
            channel = client.channel(channelType, channelID);
        viewModel = ViewModelProviders.of(this,
                new ChannelViewModelFactory(this.getApplication(), channel)
        ).get(ChannelViewModel.class);
        viewModel.getChannel().setReactionTypes(reactionTypes);
        // connect the view model
        binding.channelHeader.setViewModel(viewModel, this);
        binding.channelHeader.setOnBackClickListener(v -> finish());

        MyMessageViewHolderFactory factory = new MyMessageViewHolderFactory();
        binding.messageList.setViewHolderFactory(factory);
        binding.messageList.setMessageClickListener(this);
        binding.messageList.setMessageLongClickListener(this);
        binding.messageList.setAttachmentClickListener(this);

        binding.messageList.setViewModel(viewModel, this);
        binding.messageInput.setViewModel(viewModel, this);
        binding.messageInput.setOpenCameraViewListener(this);
        // set the viewModel data for the activity_channel.xml layout
        binding.setViewModel(viewModel);
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
    public void onMessageClick(Message message, int position) {
        ReactionDialog reactionDialog = new ReactionDialog(this,
                viewModel.getChannel(), message, position, binding.messageList, binding.messageList.getStyle());
        reactionDialog.show();
    }

    @Override
    public void onAttachmentClick(Message message, Attachment attachment) {
        String url = null;
        String type = null;
        switch (attachment.getType()) {
            case ModelType.attach_file:
                loadFile(attachment);
                return;
            case ModelType.attach_image:
                if (attachment.getOgURL() != null) {
                    url = attachment.getOgURL();
                    type = ModelType.attach_link;
                } else {
                    List<String> imageUrls = new ArrayList<>();
                    for (Attachment a : message.getAttachments()) {
                        imageUrls.add(a.getImageURL());
                    }
                    int position = message.getAttachments().indexOf(attachment);

                    new ImageViewer.Builder<>(this, imageUrls)
                            .setStartPosition(position)
                            .show();
                    return;
                }
                break;
            case ModelType.attach_video:
                url = attachment.getTitleLink();
                break;
            case ModelType.attach_giphy:
                url = attachment.getThumbURL();
                break;
            case ModelType.attach_product:
                url = attachment.getUrl();
                break;
        }
        if (type == null) type = attachment.getType();
        Intent intent = new Intent(this, AttachmentActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    // region Load File
    private void loadFile(Attachment attachment) {
        // Media
        if (attachment.getMime_type().contains("audio") ||
                attachment.getMime_type().contains("video")) {
            Intent intent = new Intent(this, AttachmentMediaActivity.class);
            intent.putExtra("type", attachment.getMime_type());
            intent.putExtra("url", attachment.getAssetURL());
            startActivity(intent);
            return;
        }

        // Office
        if (attachment.getMime_type().equals("application/msword") ||
                attachment.getMime_type().equals(ModelType.attach_mime_txt) ||
                attachment.getMime_type().equals(ModelType.attach_mime_pdf) ||
                attachment.getMime_type().contains("application/vnd")) {

            Intent intent = new Intent(this, AttachmentDocumentActivity.class);
            intent.putExtra("url", attachment.getAssetURL());
            startActivity(intent);
        }
    }
    // endregion


    Map<String, String> reactionTypes = new HashMap<String, String>() {
        {
            put("like", "\uD83D\uDC4D");
            put("love", "\u2764\uFE0F");
            put("haha", "\uD83D\uDE02");
            put("wow", "\uD83D\uDE32");
            put("sad", " \uD83D\uDE41");
            put("angry", "\uD83D\uDE21");
            put("cheeky", "\uD83D\uDE1B");
        }
    };


    @Override
    public void onMessageLongClick(Message message) {
        MoreActionDialog moreActionDialog = new MoreActionDialog(this,
                viewModel.getChannel(),
                message,
                binding.messageList.getStyle());
        moreActionDialog.show();
    }
}