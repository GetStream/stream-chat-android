package io.getstream.chat.example;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.getstream.sdk.chat.interfaces.StreamMessageInputManager;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.EditTextUtils;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.Arrays;
import java.util.HashMap;

import io.getstream.chat.example.databinding.ViewCustomMessageInputBinding;

public class CustomMessageInputView extends MessageInputView
        implements StreamMessageInputManager {

    final static String TAG = CustomMessageInputView.class.getSimpleName();

    // binding for this view
    private ViewCustomMessageInputBinding binding;

    public CustomMessageInputView(Context context) {
        super(context);
        initBinding(context);
    }

    public CustomMessageInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBinding(context);
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        super.setViewModel(viewModel, lifecycleOwner);
        // Set Keystroke
        EditTextUtils.afterTextChanged(binding.etMessage, editable -> {
            String messageText = getMessageText();
            if (messageText.length() > 0) {
                viewModel.keystroke();
            }
        });
        setMessageInputManager(this);
    }
    Message message;
    private void initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewCustomMessageInputBinding.inflate(inflater, this, true);

        // Send Text Message
        binding.btnSend.setOnClickListener(view -> {
            message = new Message(binding.etMessage.getText().toString());
            onSendMessage();
        });
        // Send Image Message
        binding.btnImage.setOnClickListener(view -> {
            message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_image)));
            onSendMessage();
        });
        // Send Giphy Message
        binding.btnGif.setOnClickListener(view -> {
            message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_giphy)));
            onSendMessage();
        });
        // Send File Message
        binding.btnFile.setOnClickListener(view -> {
            message = new Message();
            message.setAttachments(Arrays.asList(getAttachment(ModelType.attach_file)));
            onSendMessage();
        });
    }

    @Override
    public Message prepareMessage(Message message_) {
        // note that you typically want to use custom fields on attachments instead of messages
        // attachment UI is easier to customize than the message UI
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("mycustomfield", "123");
        message.setExtraData(extraData);

        return super.prepareMessage(message);
    }

    @Override
    protected Message getEditMessage(String input) {
        return super.getEditMessage(binding.etMessage.getText().toString());
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

    @Override
    public void onSendMessageSuccess(Message message) {
        binding.etMessage.setText("");
    }

    @Override
    public void onEditMessage(Message message) {
        if (message == null
                || TextUtils.isEmpty(message.getText())) return;

        binding.etMessage.requestFocus();
        binding.etMessage.setText(message.getText());
        binding.etMessage.setSelection(binding.etMessage.getText().length());
    }

    @Override
    public void onSendMessageError(String errMsg) {
        binding.etMessage.setText("");
    }

}
