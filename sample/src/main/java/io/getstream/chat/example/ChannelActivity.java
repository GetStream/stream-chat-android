package io.getstream.chat.example;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.view.Dialog.MoreActionDialog;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModelFactory;

import java.util.Arrays;

import io.getstream.chat.example.databinding.ActivityChannelBinding;

/**
 * Show the messages for a channel
 */
public class ChannelActivity extends AppCompatActivity
        implements MessageListView.MessageLongClickListener,
        MessageListView.AttachmentClickListener,
        MessageListView.HeaderOptionsClickListener,
        MessageListView.HeaderAvatarGroupClickListener,
        MessageListView.UserClickListener{

    static final String TAG = ChannelActivity.class.getSimpleName();
    static final String STATE_TEXT = "messageText";

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
        if (savedInstanceState != null) {
            String messageText = savedInstanceState.getString(STATE_TEXT);
            binding.messageInput.etMessage.setText(messageText);
        }

        Channel channel = client.channel(channelType, channelID);
        viewModel = ViewModelProviders.of(this,
                new ChannelViewModelFactory(this.getApplication(), channel)
        ).get(ChannelViewModel.class);

        // set listeners
        binding.messageList.setMessageLongClickListener(this);
        binding.messageList.setUserClickListener(this);
        binding.messageList.setAttachmentClickListener(this);

        binding.messageList.setViewHolderFactory(new MyMessageViewHolderFactory());

        // connect the view model
        binding.setViewModel(viewModel);
        binding.channelHeader.setViewModel(viewModel, this);
        binding.channelHeader.setHeaderOptionsClickListener(this);
        binding.channelHeader.setHeaderAvatarGroupClickListener(this);
        binding.messageList.setViewModel(viewModel, this);

        // Set Keystroke
        binding.messageInput.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String messageText = binding.messageInput.etMessage.getText().toString();
                Log.i(TAG, "Length is " + s.length());
                if (messageText.length() > 0) {
                    viewModel.keystroke();
                }
            }
        });

        // Send Text Message
        binding.messageInput.btnSend.setOnClickListener(view -> {
            Message message = new Message();
            message.setText(binding.messageInput.etMessage.getText().toString());
            sendMessage(channel, message);
        });
        // Send Image Message
        binding.messageInput.btnImage.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_image)));
            sendMessage(channel, message);
        });
        // Send Giphy Message
        binding.messageInput.btnGif.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_giphy)));
            sendMessage(channel, message);
        });
        // Send File Message
        binding.messageInput.btnFile.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_file)));
            sendMessage(channel, message);
        });
    }

    private void sendMessage(Channel channel, Message message){
        message.setStatus(null);
        channel.sendMessage(message, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                Log.i(TAG, "Sent message successfully!");
                binding.messageInput.etMessage.setText("");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.i(TAG, errMsg);
                binding.messageInput.etMessage.setText("");
            }
        });
    }

    private Attachment getAttachment(String modelType){
        Attachment attachment = new Attachment();
        String url;
        switch (modelType){
            case ModelType.attach_image:
                url = "https://cdn.pixabay.com/photo/2017/12/25/17/48/waters-3038803_1280.jpg";
                attachment.setImageURL(url);
                attachment.setFallback("test image");
                attachment.setType(ModelType.attach_image);
                break;
            case ModelType.attach_giphy:
                url = "https://media1.giphy.com/media/l4FB5yXHoVSheWQ5a/giphy.gif";
                attachment.setThumbURL(url);
                attachment.setTitleLink(url);
                attachment.setTitle("hi");
                attachment.setType(ModelType.attach_giphy);
                break;
            case ModelType.attach_file:
                url = "https://stream-cloud-uploads.imgix.net/attachments/47574/08cd5fba-f157-4c97-9ab1-fd57a1fafc03.VID_20190928_213042.mp4?dl=VID_20190928_213042.mp4&s=0d8f2c1501e0f6a1de34c5fe1c84a0a5";
                attachment.setTitle("video.mp4");
                attachment.setFile_size(707971);
                attachment.setAssetURL(url);
                attachment.setType(ModelType.attach_file);
                attachment.setMime_type(ModelType.attach_mime_mp4);
                break;
        }
        return attachment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TEXT, binding.messageInput.etMessage.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onMessageLongClick(Message message) {
        new MoreActionDialog(this)
                .setChannelViewModel(viewModel)
                .setMessage(message)
                .setStyle(binding.messageList.getStyle())
                .show();
    }

    @Override
    public void onAttachmentClick(Message message, Attachment attachment) {
        binding.messageList.showAttachment(message, attachment);

    }

    @Override
    public void onHeaderOptionsClick(Channel channel) {
        new AlertDialog.Builder(this)
                .setTitle("Options for channel " + channel.getName())
                .setMessage("You pressed on the options, well done")
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.stream_ic_settings)
                .show();
    }

    @Override
    public void onHeaderAvatarGroupClick(Channel channel) {
        new AlertDialog.Builder(this)
                .setTitle("Avatar group click for channel " + channel.getName())
                .setMessage("You pressed on the avatar group, well done")
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.stream_ic_settings)
                .show();
    }

    @Override
    public void onUserClick(User user) {
        // open your user profile
    }
}