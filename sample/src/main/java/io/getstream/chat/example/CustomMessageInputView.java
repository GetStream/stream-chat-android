package io.getstream.chat.example;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.getstream.sdk.chat.interfaces.MessageInputManager;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.getstream.chat.example.databinding.ViewCustomMessageInputBinding;

public class CustomMessageInputView extends MessageInputView implements MessageInputManager {

    final static String TAG = CustomMessageInputView.class.getSimpleName();

    public CustomMessageInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBinding(context);
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        super.setViewModel(viewModel, lifecycleOwner);
        setMessageInputManager(this);
    }

    private void initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewCustomMessageInputBinding binding = ViewCustomMessageInputBinding.inflate(inflater, this, true);
        // Don't miss this line to set editText!
        setEditText(binding.etMessage);
        // Send Text Message
        binding.btnSend.setOnClickListener(view -> {
            Message message = isEdit() ? getEditMessage() : new Message();
            message.setText(getMessageText());
            // if you want to set custom data, uncomment the line below.
            // setExtraData(message);
            onSendMessage(message);
        });
        // Send Image Message
        binding.btnImage.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(getAttachments(ModelType.attach_image));
            onSendMessage(message);
        });
        // Send Giphy Message
        binding.btnGif.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(getAttachments(ModelType.attach_giphy));
            onSendMessage(message);
        });
        // Send File Message
        binding.btnFile.setOnClickListener(view -> {
            Message message = new Message();
            message.setAttachments(getAttachments(ModelType.attach_file));
            onSendMessage(message);
        });
    }

    // note that you typically want to use custom fields on attachments instead of messages
    private void setExtraData(Message message){
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("mycustomfield", "123");
        message.setExtraData(extraData);
    }

    @Override
    public void onSendMessageSuccess(Message message) {
        Log.d(TAG, "Sent message! :" + message.getText());
    }

    @Override
    public void onSendMessageError(String errMsg) {
        Log.d(TAG, "Failed send message! :" + errMsg);
    }

    // If you want to customize editing message behavior you can override the function below
//    @Override
//    public void editMessage(Message message) {
//        // TODO: Customize Editing Message behavior.
//    }

    private List<Attachment> getAttachments(String modelType) {
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
        List<Attachment> attachments = new ArrayList<>();
        attachments.add(attachment);
        return attachments;
    }
}
