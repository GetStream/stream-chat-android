### Message List

The message list renders a list of messages. You can use it like this:

```xml
<com.getstream.sdk.chat.view.MessageListView
    android:id="@+id/messageList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    android:background="#f3f5f8"
    app:layout_constraintBottom_toTopOf="@+id/message_input"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/channelHeader"/>
```
And here's a full example of an activity that renders a message list, channel header and message input

```java
package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import io.getstream.chat.example.databinding.ActivityChannelBinding;
import com.getstream.sdk.chat.ChatImpl;
import io.getstream.chat.android.client.models.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.view.Dialog.MoreActionDialog;
import com.getstream.sdk.chat.view.messageinput.MessageInputView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModelFactory;

/**
 * Show the messages for a channel
 */
public class ChannelActivity extends AppCompatActivity
        implements MessageListView.MessageLongClickListener,
        MessageListView.AttachmentClickListener,
        MessageListView.HeaderOptionsClickListener,
        MessageListView.HeaderAvatarGroupClickListener,
        MessageListView.UserClickListener,
        MessageInputView.PermissionRequestListener,
        MessageInputView.OpenCameraViewListener {

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

        Channel channel = client.channel(channelType, channelID);
        viewModel = ViewModelProviders.of(this,
                new ChannelViewModelFactory(this.getApplication(), channel)
        ).get(ChannelViewModel.class);

        // set listeners
        binding.messageInput.setPermissionRequestListener(this);
        binding.messageInput.setOpenCameraViewListener(this);

        // connect the view model
        binding.setViewModel(viewModel);
        binding.channelHeader.setViewModel(viewModel, this);
        binding.messageList.setViewModel(viewModel, this);
        binding.messageInput.setViewModel(viewModel, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.messageInput.captureMedia(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        binding.messageInput.permissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void openPermissionRequest() {
        PermissionChecker.permissionCheck(this, null);
        // If you are writing a Channel Screen in a Fragment, use the code below instead of the code above.
        //   PermissionChecker.permissionCheck(getActivity(), this);
    }

    @Override
    public void openCameraView(Intent intent, int REQUEST_CODE) {
        startActivityForResult(intent, REQUEST_CODE);
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
```

#### Customizing the message list - Attributes

You must use the following properties in your XML to change your MessageListView.

- **AvatarView**

| Properties                         | Type                   | Default          |
| ---------------------------------- | ---------------------- | ---------------- |
| `app:streamAvatarWidth`            | dimension              | 32dp             |
| `app:streamAvatarHeight`           | dimension              | 32dp             |
| `app:streamAvatarBorderWidth`      | dimension              | 3dp              |
| `app:streamAvatarBorderColor`      | color                  | WHITE            |
| `app:streamAvatarBackGroundColor`  | color                  | stream_gray_dark |
| `app:streamAvatarTextSize`         | dimension              | 14sp             |
| `app:streamAvatarTextColor`        | color                  | WHITE            |
| `app:streamAvatarTextStyle`        | normal, bold, italic   | bold             |
| `app:streamAvatarTextFont`         | reference              | -                |
| `app:streamAvatarTextFontAssets`   | string                 | -                |

- **ReadStateView**

| Properties                          | Type                   | Default |
| ----------------------------------- | ---------------------- | ------- |
| `app:streamShowReadState`           | boolean                | true    |
| `app:streamReadStateAvatarWidth`    | dimension              | 14dp    |
| `app:streamReadStateAvatarHeight`   | dimension              | 14dp    |
| `app:streamReadStateTextSize`       | dimension              | 8sp     |
| `app:streamReadStateTextColor`      | color                  | BLACK   |
| `app:streamReadStateTextStyle`      | normal, bold, italic   | bold    |
| `app:streamReadStateTextFont`       | reference              | -       |
| `app:streamReadStateTextFontAssets` | string                 | -       |

- **Reaction**

| Properties                          | Type                  | Default |
| ----------------------------------- | --------------------- | ------- |
| `app:streamReactionEnabled`         | boolean               | true    |
| `app:streamrReactionViewBgDrawable` | reference             |   -     |
| `app:streamReactionViewBgColor`     | color                 | #292929 |
| `app:streamReactionViewEmojiSize`   | dimension             | 12sp    |
| `app:streamReactionViewEmojiMargin` | dimension             | 1dp     |
| `app:streamReactionInputbgColor`    | color   	          | #292929 |
| `app:streamReactionInputEmojiSize`  | dimension             | 27sp    |
| `app:streamReactionInputEmojiMargin`| dimension   	      | 5dp     |

- **Message**

| Properties                                      | Type                 | Default   |
| ----------------------------------------------- | -------------------- | --------- |
| `app:streamMessageTextSizeMine`                 | dimension            | 15sp      |
| `app:streamMessageTextSizeTheirs`               | dimension            | 15sp      |
| `app:streamMessageTextColorMine`                | color                | BLACK     |
| `app:streamMessageTextColorTheirs`              | color                | BLACK     |
| `app:streamMessageTextStyleMine`                | normal, bold, italic | normal    |
| `app:streamMessageTextStyleTheirs`              | normal, bold, italic | normal    |
| `app:streamMessageTextFontMine`                 | reference            | -         |
| `app:streamMessageTextFontTheirs`               | reference            | -         |
| `app:streamMessageTextFontMineAssets`           | string               | -         |
| `app:streamMessageTextFontTheirsAssets`         | string               | -         |
| `app:streamMessageBubbleDrawableMine`           | reference            | -         |
| `app:streamMessageBubbleDrawableTheirs`         | reference            | -         |
| `app:streamMessageTopLeftCornerRadiusMine`      | dimension            | 16dp      |
| `app:streamMessageTopRightCornerRadiusMine`     | dimension            | 16dp      |
| `app:streamMessageBottomRightCornerRadiusMine`  | dimension            | 2dp       |
| `app:streamMessageBottomLeftCornerRadiusMine`   | dimension            | 16dp      |
| `app:streamMessageTopLeftCornerRadiusTheirs`    | dimension            | 16dp      |
| `app:streamMessageTopRightCornerRadiusTheirs`   | dimension            | 16dp      |
| `app:streamMessageBottomRightCornerRadiusTheirs`| dimension            | 16dp      |
| `app:streamMessageBottomLeftCornerRadiusTheirs` | dimension            | 2dp       |
| `app:streamMessageBackgroundColorMine`          | color                | #0D000000 |
| `app:streamMessageBackgroundColorTheirs`        | color                | WHITE     |
| `app:streamMessageBorderColorMine`              | color                | #14000000 |
| `app:streamMessageBorderColorTheirs`            | color                | #14000000 |
| `app:streamMessageBorderWidthMine`              | dimension            | 1dp       |
| `app:streamMessageBorderWidthTheirs`            | dimension            | 1dp       |
| `app:streamMessageLinkTextColorMine`            | color                | -         |
| `app:streamMessageLinkTextColorTheirs`          | color                | -         |

- **AttachmentView**

| Properties                                            | Type                 | Default                            |
| ----------------------------------------------------- | -------------------- | ---------------------------------- |
| `app:streamAttachmentBackgroundColorMine`             | color                | streamMessageBackgroundColorMine   |
| `app:streamAttachmentBackgroundColorTheirs`           | color                | streamMessageBackgroundColorTheirs |
| `app:streamAttachmentBorderColorMine`                 | color                | streamMessageBorderColorMine       |
| `app:streamAttachmentBorderColorTheirs`               | color                | streamMessageBorderColorTheirs     |
| `app:streamAttachmentTitleTextSizeMine`               | dimension            | 13sp                               |
| `app:streamAttachmentTitleTextSizeTheirs`             | dimension            | 13sp                               |
| `app:streamAttachmentTitleTextColorMine`              | color                | #026DFE                            |
| `app:streamAttachmentTitleTextColorTheirs`            | color                | #026DFE                            |
| `app:streamAttachmentTitleTextStyleMine`              | normal, bold, italic | bold                               |
| `app:streamAttachmentTitleTextStyleTheirs`            | normal, bold, italic | bold                               |
| `app:streamAttachmentTitleTextFontMine`               | reference            | -                                  |
| `app:streamAttachmentTitleTextFontTheirs`             | reference            | -                                  |
| `app:streamAttachmentTitleTextFontAssetsMine`         | string               | -                                  |
| `app:streamAttachmentTitleTextFontAssetsTheirs`       | string               | -                                  |
| `app:streamAttachmentDescriptionTextSizeMine`         | dimension            | 11sp                               |
| `app:streamAttachmentDescriptionTextSizeTheirs`       | dimension            | 11sp                               |
| `app:streamAttachmentDescriptionTextColorMine`        | color                | stream_gray_dark                   |
| `app:streamAttachmentDescriptionTextColorTheirs`      | color                | stream_gray_dark                   |
| `app:streamAttachmentDescriptionTextStyleMine`        | normal, bold, italic | normal                             |
| `app:streamAttachmentDescriptionTextStyleTheirs`      | normal, bold, italic | normal                             |
| `app:streamAttachmentDescriptionTextFontMine`         | reference            | -                                  |
| `app:streamAttachmentDescriptionTextFontTheirs`       | reference            | -                                  |
| `app:streamAttachmentDescriptionTextFontAssetsMine`   | string               | -                                  |
| `app:streamAttachmentDescriptionTextFontAssetsTheirs` | string               | -                                  |
| `app:streamAttachmentFileSizeTextSizeMine`            | dimension            | 12sp                               |
| `app:streamAttachmentFileSizeTextSizeTheirs`          | dimension            | 12sp                               |
| `app:streamAttachmentFileSizeTextColorMine`           | color                | stream_gray_dark                   |
| `app:streamAttachmentFileSizeTextColorTheirs`         | color                | stream_gray_dark                   |
| `app:streamAttachmentFileSizeTextStyleMine`           | normal, bold, italic | bold                               |
| `app:streamAttachmentFileSizeTextStyleTheirs`         | normal, bold, italic | bold                               |
| `app:streamAttachmentFileSizeTextFontMine`            | reference            | -                                  |
| `app:streamAttachmentFileSizeTextFontTheirs`          | reference            | -                                  |
| `app:streamAttachmentFileSizeTextFontAssetsMine`      | string               | -                                  |
| `app:streamAttachmentFileSizeTextFontAssetsTheirs`    | string               | -                                  |

- **Date Separator**

| Properties                                  | Type                   | Default           |
| ------------------------------------------- | ---------------------- | ----------------- |
| `app:streamDateSeparatorDateTextSize`       | dimension              | 12sp              |
| `app:streamDateSeparatorDateTextColor`      | color                  | stream_gray_dark  |
| `app:streamDateSeparatorDateTextStyle`      | normal, bold, italic   | bold              |
| `app:streamDateSeparatorDateTextFont`       | reference              | -                 |
| `app:streamDateSeparatorDateTextFontAssets` | string                 | -                 |
| `app:streamDateSeparatorLineWidth`          | dimension              | 1dp               |
| `app:streamDateSeparatorLineColor`          | color                  | stream_gray_dark  |
| `app:streamDateSeparatorLineDrawable`       | reference              | -                 |

- **User name and message date**

| Properties                                | Type                   | Default          |
| ----------------------------------------- | ---------------------- | ---------------- |
| `app:streamMessageUserNameTextSize`       | dimension              | 11sp             |
| `app:streamMessageUserNameTextColor`      | color                  | stream_gray_dark |
| `app:streamMessageUserNameTextStyle`      | normal, bold, italic   | normal           |
| `app:streamMessageUserNameTextFont`       | reference              | -                |
| `app:streamMessageUserNameTextFontAssets` | string                 | -                |
| `app:streamMessageDateTextSizeMine`       | dimension              | 11sp             |
| `app:streamMessageDateTextSizeTheirs`     | dimension              | 11sp             |
| `app:streamMessageDateTextColorMine`      | color                  | stream_gray_dark |
| `app:streamMessageDateTextColorTheirs`    | color                  | stream_gray_dark |
| `app:streamMessageDateTextStyleMine`      | normal, bold, italic   | normal           |
| `app:streamMessageDateTextStyleTheirs`    | normal, bold, italic   | normal           |
| `app:streamMessageDateTextFontMine`       | reference              | -                |
| `app:streamMessageDateTextFontAssetsMine` | string                 | -                |
| `app:streamUserNameShow`                  | boolean                | true             |
| `app:streamMessageDateShow`               | boolean                | true             |

- **Thread**

| Properties                          | Type                   | Default |
| ----------------------------------- | ---------------------- | ------- |
| `app:streamThreadEnabled`           |   boolean              | true    |

#### Customizing the message list - BubbleHelper

Many messaging apps will have rather complex message bubbles. The layout typically changes based on the position of the message, if it's your or written by someone else, and if it has attachments. Here's an example of the default bubble list helper.

```java
binding.messageList.setBubbleHelper(new MessageListView.BubbleHelper() {
    @Override
    public Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions) {
        if (style.getMessageBubbleDrawable(mine) != -1)
            return context.getDrawable(style.getMessageBubbleDrawable(mine));

        return getBubbleDrawable(message, null, mine, positions);
    }

    @Override
    public Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment) {
        if (style.getMessageBubbleDrawable(mine) != -1)
            return context.getDrawable(style.getMessageBubbleDrawable(mine));

        return getBubbleDrawable(message, attachment, mine, positions);
    }

    @Override
    public Drawable getDrawableForAttachmentDescription(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions) {
        if (style.getMessageBubbleDrawable(mine) != -1)
            return context.getDrawable(style.getMessageBubbleDrawable(mine));

        return getBubbleDrawable(message, null, mine, positions);
    }
});

private void getBubbleDrawable(Message message, Attachment attachment, boolean isMine, List<MessageViewHolderFactory.Position> positions){
    // TODO: Create your bubble drawable based on message, attachment, isMine and positions
    return drawable;
}
```

#### Customizing the message item view with your own layout file.

You might want to create your own type(s) of `Message`(`Attachment`) or use the custom message(attachment) item view about the specific `Message`(`Attachment`).<br/>
In this case you can define your _own MessageListViewHolder(AttachmentViewHolder)_ with your own layout.<br/>
We need to 3 steps for customizing Custom Message List.
- Create your own Message/Attachment item layout
- Create your own view holders
- Create your own ViewHolderFactory

Let's see how to create your _own message item view_ step by step below.

##### 1. Create your own Message/Attachment item layout
- `list_item_message_custom`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <com.getstream.sdk.chat.view.AttachmentListView
        android:id="@+id/attachmentview"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:visibility="visible"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- `list_item_attachment_custom`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView
        android:id="@+id/iv_media_thumb"
        android:layout_width="match_parent"
        android:layout_height="@dimen/stream_attach_image_height"
        android:layout_marginStart="1px"
        android:layout_marginTop="1px"
        android:layout_marginEnd="1px"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

    <TextView
        android:id="@+id/tv_logo"
        android:layout_width="150dp"
        android:layout_height="50dp"
        android:background="@drawable/imgur_logo"
        app:layout_constraintBottom_toBottomOf="@+id/iv_media_thumb"
        app:layout_constraintEnd_toEndOf="@+id/iv_media_thumb"
        app:layout_constraintStart_toStartOf="@+id/iv_media_thumb"
        app:layout_constraintTop_toTopOf="@+id/iv_media_thumb" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

##### 2. Create your own view holders
Create own view holders with _extends_ _BaseViewHolder_.

###### Base ViewHolders

Let's see _BaseViewHolders_:`BaseMessageListItemViewHolder` and `BaseAttachmentViewHolder` before creating your own view holders.

- `BaseMessageListItemViewHolder`
```java
public abstract class BaseMessageListItemViewHolder extends RecyclerView.ViewHolder {

    public BaseMessageListItemViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(@NonNull Context context,
                              @NonNull ChannelState channelState,
                              @NonNull MessageListItem messageListItem,
                              @NonNull MessageListViewStyle style,
                              @NonNull MessageListView.BubbleHelper bubbleHelper,
                              @NonNull MessageViewHolderFactory factory,
                              int position);
}
```
- `BaseAttachmentViewHolder`
```java
public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder{

    public BaseAttachmentViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(@NonNull Context context,
                              @NonNull MessageListItem messageListItem,
                              @NonNull Message message,
                              @NonNull Attachment attachment,
                              @NonNull MessageListViewStyle style,
                              @NonNull MessageListView.BubbleHelper bubbleHelper,
                              @Nullable MessageListView.AttachmentClickListener clickListener,
                              @Nullable MessageListView.MessageLongClickListener longClickListener);
}
```

###### Create your own view holders
As you can see above, `BaseViewHolders` are _abstract classes_ so you must override the functions of `BaseViewHolder` in your own view holders.<br/>
Your own type of `Message` might has `Attachment` or not.<br/>
Therefore, depending on the case, you may or may not define 'CustomAttachmentViewHolder'.<br/>
Even though the own type of `Message` has an own type of `Attachment`, you may or may not define 'CustomAttachmentViewHolder'.<br/>
In our example, will take the case that has 'CustomAttachmentViewHolder'.
- `CustomMessageViewHolder`
```java
public class CustomMessageViewHolder extends BaseMessageListItemViewHolder {

    private AttachmentListView attachmentview;

    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.MessageLongClickListener messageLongClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;

    public CustomMessageViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        attachmentview = itemView.findViewById(com.getstream.sdk.chat.R.id.attachmentview);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull ChannelState channelState,
                     @NonNull MessageListItem messageListItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @NonNull MessageViewHolderFactory factory,
                     int position){

        attachmentview.setStyle(style);
        attachmentview.setViewHolderFactory(factory);
        attachmentview.setEntity(messageListItem);
        attachmentview.setBubbleHelper(bubbleHelper);
        attachmentview.setAttachmentClickListener(attachmentClickListener);
        attachmentview.setLongClickListener(messageLongClickListener);
    }

    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    public void setMessageLongClickListener(MessageListView.MessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }
}
```
- `CustomAttachmentViewHolder`
```java
public class CustomAttachmentViewHolder extends BaseAttachmentViewHolder {

    private PorterShapeImageView iv_media_thumb;

    private Context context;
    private MessageListItem messageListItem;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;
    private MessageListView.BubbleHelper bubbleHelper;
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;

    public CustomAttachmentViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull MessageListItem messageListItem,
                     @NonNull Message message,
                     @NonNull Attachment attachment,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @Nullable MessageListView.AttachmentClickListener clickListener,
                     @Nullable MessageListView.MessageLongClickListener longClickListener) {

        this.context = context;
        this.messageListItem = messageListItem;
        this.message = message;
        this.attachment = attachment;
        this.style = style;
        this.bubbleHelper = bubbleHelper;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        configAttachment();
        configClickListeners();
    }

    private void configAttachment(){
        Drawable background = bubbleHelper.getDrawableForAttachment(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions(), attachment);
        iv_media_thumb.setShape(context, background);

        Glide.with(context)
                .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(attachment.getThumbURL()))
                .into(iv_media_thumb);
    }

    private void configClickListeners(){
        iv_media_thumb.setOnClickListener(view -> {
            if (clickListener != null)
                clickListener.onAttachmentClick(message, attachment);
        });

        iv_media_thumb.setOnLongClickListener(view -> {
            if (longClickListener != null)
                longClickListener.onMessageLongClick(message);
            return true;
        });
    }
}
```

##### 3. Create your own ViewHolderFactory

`CustomViewHolderFactory` allows you to swap the layout file that's used for default view holders and your own view holder.<br/>
It's common to implement your own message or attachment type and render it in a custom way.<br/>
When you define your _own MessageViewHolderFactory_ need to extent `MessageViewHolderFactory` and avoid to use default view holder types of **Super class**

**default `Message` view holder type**
- MESSAGEITEM_DATE_SEPARATOR = 1
- MESSAGEITEM_MESSAGE = 2
- MESSAGEITEM_TYPING = 3
- MESSAGEITEM_THREAD_SEPARATOR = 4
- MESSAGEITEM_NOT_FOUND = 5

**default `Attachment` view holder type**
- GENERIC_ATTACHMENT = 1
- IMAGE_ATTACHMENT = 2
- VIDEO_ATTACHMENT = 3
- FILE_ATTACHMENT = 4

Let's see how to create your own ViewHolderFactory below.
```java
public class CustomMessageViewHolderFactory extends MessageViewHolderFactory {
    private int CUSTOM_MEASSAGE_TYPE = 0;
    private int CUSTOM_ATTACHMENT_TYPE = 0;

    @Override
    public int getMessageViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        if (isCUSTOM_MESSAGE_TYPE(messageListItem))
            return CUSTOM_MEASSAGE_TYPE;

        return super.getMessageViewType(messageListItem, mine, positions);
    }

    @Override
    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        if (isCUSTOM_ATTACHMENT_TYPE(attachment))
            return CUSTOM_ATTACHMENT_TYPE;

        return super.getAttachmentViewType(message, mine, position, attachments, attachment);
    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == CUSTOM_MEASSAGE_TYPE){
            CustomMessageViewHolder holder = new CustomMessageViewHolder(R.layout.list_item_message_custom, parent);
            holder.setMessageClickListener(adapter.getMessageClickListener());
            holder.setMessageLongClickListener(adapter.getMessageLongClickListener());
            holder.setAttachmentClickListener(adapter.getAttachmentClickListener());

              /*you can set more variables you need in you CustomViewHolder.*/
//            holder.setMarkdownListener(MarkdownImpl.getMarkdownListener());
//            holder.setReactionViewClickListener(adapter.getReactionViewClickListener());
//            holder.setUserClickListener(adapter.getUserClickListener());
//            holder.setReadStateClickListener(adapter.getReadStateClickListener());
//            holder.setGiphySendListener(adapter.getGiphySendListener());

            return holder;
        }

        return super.createMessageViewHolder(adapter, parent, viewType);
    }

    @Override
    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == CUSTOM_ATTACHMENT_TYPE){
            CustomAttachmentViewHolder holder = new CustomAttachmentViewHolder(R.layout.list_item_attach_custom, parent);
              /*you can set more variables you need in you CustomViewHolder.*/
//            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;
        }
        else
            return super.createAttachmentViewHolder(adapter, parent, viewType);
    }

    private boolean isCUSTOM_MESSAGE_TYPE(MessageListItem messageListItem){
        // TODO: check if messageListItem has attachamt of CUSTOM_TYPE type.
        return false;
    }

    private boolean isCUSTOM_ATTACHMENT_TYPE(Attachment attachment){
        // TODO: check if the attachment is CUSTOM_TYPE type.
        return false;
    }
}
```

Finally you can configure your own viewholder factory like this:
```java
CustomMessageViewHolderFactory factory = new CustomMessageViewHolderFactory();
binding.messageList.setViewHolderFactory(factory);
```