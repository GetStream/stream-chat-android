### Message Input

Here's an example message input view

```xml
<com.getstream.sdk.chat.view.MessageInputView
    android:id="@+id/message_input"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="32dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toEndOf="parent" />
```

#### Styling via attributes

You must use the following properties in your XML to change your MessageInputView.

- **AvatarView**

| Properties                         | Type                   | Default |
| ---------------------------------- | ---------------------- | ------- |
| `app:streamAvatarWidth`            | dimension              | 32dp    |
| `app:streamAvatarHeight`           | dimension              | 32dp    |
| `app:streamAvatarBorderWidth`      | dimension              | 3dp     |
| `app:streamAvatarBorderColor`      | color                  | WHITE   |
| `app:streamAvatarBackGroundColor`  | color                  | #989898 |
| `app:streamAvatarTextSize`         | dimension              | 14sp    |
| `app:streamAvatarTextColor`        | color                  | WHITE   |
| `app:streamAvatarTextStyle`        | normal, bold, italic   | bold    |

- **Attachment Button**

| Properties                                          | Type          | Default    |
| --------------------------------------------------- | ------------- | ---------- |
| `app:streamShowAttachmentButton`                    | boolean       | true       |
| `app:streamAttachmentButtonDefaultIconColor`        | color         | DKGRAY     |
| `app:streamAttachmentButtonDefaultIconPressedColor` | color         | WHITE      |
| `app:streamAttachmentButtonDefaultIconDisabledColor`| color         | LTGRAY     |
| `app:streamAttachmentButtonSelectedIconColor`       | color         | BLACK      |
| `app:streamAttachmentButtonIcon`                    | reference     | -          |
| `app:streamAttachmentButtonWidth`                   | dimension     | 25dp       |
| `app:streamAttachmentButtonHeight`                  | dimension     | 25dp       |


- **Send Button**

| Properties                                      | Type          | Default    |
| ----------------------------------------------- | ------------- | ---------- |
| `app:streamInputButtonDefaultIconColor`         | color         | #0076FF    |
| `app:streamInputButtonEditIconColor`            | color         | #0DD25E    |
| `app:streamInputButtonDefaultIconPressedColor`  | color         | WHITE      |
| `app:streamInputButtonDefaultIconDisabledColor` | color         | LTGRAY     |
| `app:streamInputButtonIcon`                     | reference     | -          |
| `app:streamInputButtonWidth`                    | dimension     | 25dp       |
| `app:streamInputButtonHeight`                   | dimension     | 25dp       |
		
- **Input Text**

| Properties                  | Type                  | Default         |
| --------------------------- | --------------------- | --------------- |
| `app:streamInputHint`       | string                | Write a message |
| `app:streamInputTextSize`   | dimension             | 15sp            |
| `app:streamInputTextColor`  | color                 | BLACK           |
| `app:streamInputHintColor`  | color                 | DKGRAY          |
| `app:streamInputTextStyle`  | normal, bold, italic  | normal          |



- **Input Background**

| Properties                          | Type        | Default |
| ----------------------------------- | ----------- | ------- |
| `app:streamInputBackground`         | reference   | -       |
| `app:streamInputSelectedBackground` | reference   | -       |
| `app:streamInputEditBackground`     | reference   | -       |

#### Writing your own message input view

You can also create your own message input view. 
Building your own message list or channel list is a lot of work. 
A message input or channel header view is much easier to build though.

**ChannelViewModel**

As a first step connect the channel view model. The channel view model holds all the state for a channel activity.

```java
public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
	this.channelViewModel = viewModel;
	binding.setLifecycleOwner(lifecycleOwner);
	init();
	observeUIs(lifecycleOwner);
}
```

Second step is to forward typing events to the view model

```java
    private void stopTyping() {
        isTyping = false;
        channelViewModel.getChannel().stopTyping();
        if (typingListener != null) {
            typingListener.onStopTyping();
        }
    }

    private void keyStroke() {
        channelViewModel.getChannel().keystroke();
        isTyping = true;
        if (typingListener != null) {
            typingListener.onKeystroke();
        }
    }
```

Third step is to connect the sendMessage flow

TODO document this

#### Changing the layout

If you need to make a bigger change you can swap the layout for the Message Input.

Create your custom Messge input layout named `view_custom_message_input` as shown below.

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#1A1A32">

        <EditText
            android:id="@+id/et_message"
            android:layout_width="match_parent"
            android:layout_height="45dp"
            android:layout_marginStart="10dp"
            android:layout_marginEnd="10dp"
            android:background="@null"
            android:hint="Type your message..."
            android:textColor="#FFFFFF"
            android:textColorHint="#8F8F8F"
            app:layout_constraintTop_toTopOf="parent" />

        <ImageView
            android:id="@+id/imageView"
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="@color/stream_gray_light"
            app:layout_constraintTop_toBottomOf="@+id/et_message" />

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:orientation="horizontal"
            app:layout_constraintTop_toBottomOf="@+id/imageView">

            <Button
                android:id="@+id/btn_gif"
                android:layout_width="30dp"
                android:layout_height="25dp"
                android:layout_marginStart="10dp"
                android:background="@drawable/ic_gif"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />

            <Button
                android:id="@+id/btn_file"
                android:layout_width="25dp"
                android:layout_height="25dp"
                android:layout_marginStart="10dp"
                android:background="@drawable/ic_file"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintStart_toEndOf="@+id/btn_gif"
                app:layout_constraintTop_toTopOf="parent" />

            <Button
                android:id="@+id/btn_image"
                android:layout_width="30dp"
                android:layout_height="25dp"
                android:layout_marginStart="20dp"
                android:background="@drawable/ic_image"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintStart_toEndOf="@+id/btn_file"
                app:layout_constraintTop_toTopOf="parent" />

            <Button
                android:id="@+id/btn_send"
                android:layout_width="wrap_content"
                android:layout_height="20dp"
                android:layout_marginEnd="10dp"
                android:background="@null"
                android:text="Send"
                android:textColor="#646464"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintTop_toTopOf="parent" />
        </androidx.constraintlayout.widget.ConstraintLayout>
    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```

And change `com.getstream.sdk.chat.view.MessageInputView` as following

```xml
...
<include
    android:id="@+id/message_input"
    layout="@layout/view_custom_message_input"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="32dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toEndOf="parent" />
...
```

Please add the following code to `ChannelActivity`
```java
// Set typing event
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

...

// Send Message
private void sendMessage(Channel channel, Message message){
    message.setStatus(null);
    channel.sendMessage(message, new MessageCallback() {
        @Override
        public void onSuccess(MessageResponse response) {
            binding.messageInput.etMessage.setText("");
        }

        @Override
        public void onError(String errMsg, int errCode) {
            binding.messageInput.etMessage.setText("");
        }
    });
}

// Get Attachment: Image, Giphy, File
private Attachment getAttachment(String modelType){
    Attachment attachment = new Attachment();
    attachment.setType(ModelType.attach_image);
    String url;
    switch (modelType){
        case ModelType.attach_image:
            url = "https://cdn.pixabay.com/photo/2017/12/25/17/48/waters-3038803_1280.jpg";
            attachment.setImageURL(url);
            attachment.setFallback("test image");
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
            int size = 707971;
            attachment.setFile_size(size);
            attachment.setAssetURL(url);
            attachment.setType(ModelType.attach_file);
            attachment.setMime_type(ModelType.attach_mime_mp4);
            break;
    }
    return attachment;
}

```

<p align="center">
<img src="messageinput.png">
</p>