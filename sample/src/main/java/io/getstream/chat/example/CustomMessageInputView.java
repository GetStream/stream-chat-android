package io.getstream.chat.example;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.Arrays;

import io.getstream.chat.example.databinding.ViewCustomMessageInputBinding;

public class CustomMessageInputView extends RelativeLayout {

    final static String TAG = CustomMessageInputView.class.getSimpleName();

    // binding for this view
    private ViewCustomMessageInputBinding binding;
    // our connection to the channel scope
    private ChannelViewModel viewModel;

    public CustomMessageInputView(Context context) {
        super(context);
        initBinding(context);
    }

    public CustomMessageInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBinding(context);
    }

    public void setViewModel(ChannelViewModel model, LifecycleOwner lifecycleOwner) {
        this.viewModel = model;
        binding.setLifecycleOwner(lifecycleOwner);
        // Edit Message
        viewModel.getEditMessage().observe(lifecycleOwner, this::editMessage);
    }

    private void initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewCustomMessageInputBinding.inflate(inflater, this, true);
        // Set Keystroke
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String messageText = binding.etMessage.getText().toString();
                Log.i(TAG, "Length is " + s.length());
                if (messageText.length() > 0) {
                    viewModel.keystroke(null);
                }
            }
        });

        // Send Text Message
        binding.btnSend.setOnClickListener(view -> {
            Message message = new Message();
            message.setText(binding.etMessage.getText().toString());
            sendMessage(message);
        });
        // Send Image Message
        binding.btnImage.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_image)));
            sendMessage(message);
        });
        // Send Giphy Message
        binding.btnGif.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_giphy)));
            sendMessage(message);
        });
        // Send File Message
        binding.btnFile.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_file)));
            sendMessage(message);
        });
    }

    private void sendMessage(Message message) {
        if (viewModel.isEditing()) {
            viewModel.getEditMessage().getValue().setText(message.getText());
            viewModel.getChannel().updateMessage(viewModel.getEditMessage().getValue(), new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    binding.etMessage.setText("");
                    ;
                    viewModel.setEditMessage(null);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    binding.etMessage.setText("");
                }
            });
        } else {
            message.setStatus(null);
            viewModel.sendMessage(message, new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    Log.i(TAG, "Sent message successfully!");
                    binding.etMessage.setText("");
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    Log.i(TAG, errMsg);
                    binding.etMessage.setText("");
                }
            });
        }
    }

    // Get Attachment: Image, Giphy, File
    private Attachment getAttachment(String modelType) {
        Attachment attachment = new Attachment();
        String url;
        switch (modelType) {
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

    // Edit Message
    private void editMessage(Message message) {
        if (message == null
                || TextUtils.isEmpty(message.getText())) return;

        binding.etMessage.requestFocus();
        binding.etMessage.setText(message.getText());
        binding.etMessage.setSelection(binding.etMessage.getText().length());
    }


    public void setMessageText(String t) {
        binding.etMessage.setText(t);
    }

    public String getMessageText() {
        return binding.etMessage.getText().toString();
    }
}
