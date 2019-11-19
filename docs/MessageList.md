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
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.view.Dialog.MoreActionDialog;
import com.getstream.sdk.chat.view.MessageInputView;
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

| Properties                         | Type                   | Default |
| ---------------------------------- | ---------------------- | ------- |
| `app:streamAvatarWidth`            | dimension              | 32dp    |
| `app:streamAvatarHeight`           | dimension              | 32dp    |
| `app:streamAvatarBorderWidth`      | dimension              | 3dp     |
| `app:streamAvatarBorderColor`      | color                  | WHITE   |
| `app:streamAvatarBackGroundColor`  | color                  | DKGRAY  |
| `app:streamAvatarTextSize`         | dimension              | 14sp    |
| `app:streamAvatarTextColor`        | color                  | WHITE   |
| `app:streamAvatarTextStyle`        | normal, bold, italic   | bold    |

- **ReadStateView**

| Properties                         | Type                   | Default |
| ---------------------------------- | ---------------------- | ------- |
| `app:streamShowReadState`          | boolean                | true    |
| `app:streamReadStateAvatarWidth`   | dimension              | 14dp    |
| `app:streamReadStateAvatarHeight`  | dimension              | 14dp    |
| `app:streamReadStateTextSize`      | dimension              | 8sp     |
| `app:streamReadStateTextColor`     | color                  | BLACK   |
| `app:streamReadStateTextStyle`     | normal, bold, italic   | bold    |

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



- **AttachmentView**

| Properties                                | Type                 | Default |
| ----------------------------------------- | -------------------- | ------- |
| `app:streamAttachmentTitleTextSize`       | dimension            | 13sp    |
| `app:streamAttachmentTitleTextColor`      | color                | #026DFE |
| `app:streamAttachmentTitleTextStyle`      | normal, bold, italic | bold    |
| `app:streamAttachmentDescriptionTextSize` | dimension            | 11sp    |
| `app:streamAttachmentDescriptionTextColor`| color                | DKGRAY  |
| `app:streamAttachmentDescriptionTextStyle`| normal, bold, italic | normal  |
| `app:streamAttachmentFileSizeTextSize`    | dimension            | 12sp    |
| `app:streamAttachmentFileSizeTextColor`   | color                | DKGRAY  |
| `app:streamAttachmentFileSizeTextStyle`   | normal, bold, italic | bold    |

- **Date Separator**

| Properties                                | Type                   | Default |
| ----------------------------------------- | ---------------------- | ------- |
| `app:streamDateSeparatorDateTextSize`     | dimension              | 12sp    |
| `app:streamDateSeparatorDateTextColor`    | color                  | DKGRAY  |
| `app:streamDateSeparatorDateTextStyle`    | normal, bold, italic   | bold    |
| `app:streamDateSeparatorLineWidth`        | dimension              | 1px     |
| `app:streamDateSeparatorLineColor`        | color                  | DKGRAY  |
| `app:streamDateSeparatorLineDrawable`     | reference              | -       |

- **Thread**

| Properties                          | Type                   | Default |
| ----------------------------------- | ---------------------- | ------- |
| `app:streamThreadEnabled`           |   boolean              | true    |

#### Customizing the message list - BubbleHelper

Many messaging apps will have rather complex message bubbles. The layout typically changes based on the position of the message, if it's your or written by someone else, and if it has attachments. Here's an example of the default bubble list helper.

```java
messageList.setBubbleHelper(new BubbleHelper() {
    @Override
    public Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions) {
	if (mine) {
	    // if the size is 0 the attachment has the corner change
	    if (positions.contains(MessageViewHolderFactory.Position.TOP) && message.getAttachments().size() == 0) {
		return getResources().getDrawable(R.drawable.message_bubble_mine_top);
	    }
	    return style.getMessageBubbleDrawableMine();
	} else {
	    if (positions.contains(MessageViewHolderFactory.Position.TOP) && message.getAttachments().size() == 0) {
		return getResources().getDrawable(R.drawable.message_bubble_theirs_top);
	    }
	    return style.getMessageBubbleDrawableTheirs();
	}
    }

    @Override
    public Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment) {
	if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
	    int attachmentPosition = message.getAttachments().indexOf(attachment);
	    if (attachmentPosition == 0) {
		return getResources().getDrawable(R.drawable.round_attach_media_incoming1);
	    }
	}
	return getResources().getDrawable(R.drawable.round_attach_media_incoming2);
    }
});
```

#### Customizing the message list - ViewHolderFactory

You can configure your own viewholder factory like this:

```java
MyMessageViewHolderFactory factory = new MyMessageViewHolderFactory();
binding.messageList.setViewHolderFactory(factory);
```

This allows you to swap the layout file that's used for the typing indicator, the date seperator or the message. You can also implement your own view holder. It's common to implement your own message or attachment type and render it in a custom way.

```java
public class MessageViewHolderFactory {

    private static String TAG = MessageViewHolderFactory.class.getName();

    private static int GENERIC_ATTACHMENT = 1;
    private static int IMAGE_ATTACHMENT = 2;
    private static int VIDEO_ATTACHMENT = 3;
    private static int FILE_ATTACHMENT = 4;

    public MessageListItemType getEntityViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        // typing
        // date
        // various message types
        MessageListItemType messageListItemType = messageListItem.getType();
        if (messageListItemType == MessageListItemType.DATE_SEPARATOR) {
            return MessageListItemType.DATE_SEPARATOR;
        } else if (messageListItemType == MessageListItemType.MESSAGE) {
            return MessageListItemType.MESSAGE;
        } else if (messageListItemType == MessageListItemType.TYPING) {
            return MessageListItemType.TYPING;
        } else if (messageListItemType == MessageListItemType.THREAD_SEPARATOR) {
            return MessageListItemType.THREAD_SEPARATOR;
        }else if (messageListItemType == MessageListItemType.NO_CONNECTION) {
            return MessageListItemType.NO_CONNECTION;
        }
        return MessageListItemType.NOT_FOUND;
    }

    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        // video
        // image
        // link/card layout
        // custom attachment types
        String t = attachment.getType();
        if (t == null) {
            return GENERIC_ATTACHMENT;
        } else if (t.equals(ModelType.attach_video)) {
            return VIDEO_ATTACHMENT;
        } else if (t.equals(ModelType.attach_image) ||
                t.equals(ModelType.attach_giphy)) {
            return IMAGE_ATTACHMENT;
        } else if (t.equals(ModelType.attach_file)) {
            return FILE_ATTACHMENT;
        } else {
            return GENERIC_ATTACHMENT;
        }

    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent, MessageListItemType viewType) {
        if (viewType == MessageListItemType.DATE_SEPARATOR) {
            DateSeparatorViewHolder holder = new DateSeparatorViewHolder(R.layout.stream_item_date_separator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == MessageListItemType.MESSAGE) {
            MessageListItemViewHolder holder = new MessageListItemViewHolder(R.layout.stream_item_message, parent);
            holder.setViewHolderFactory(this);
            holder.setStyle(adapter.getStyle());
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;

        } else if (viewType == MessageListItemType.TYPING) {
            TypingIndicatorViewHolder holder = new TypingIndicatorViewHolder(R.layout.stream_item_type_indicator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == MessageListItemType.THREAD_SEPARATOR) {
            ThreadSeparatorViewHolder holder = new ThreadSeparatorViewHolder(R.layout.stream_item_thread_separator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        }else if (viewType == MessageListItemType.NO_CONNECTION) {
            NoConnectionViewHolder holder = new NoConnectionViewHolder(R.layout.stream_item_no_connection, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else {
            return null;
        }
    }

    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == VIDEO_ATTACHMENT || viewType == IMAGE_ATTACHMENT) {
            AttachmentViewHolderMedia holder = new AttachmentViewHolderMedia(R.layout.stream_item_attach_media, parent);
            holder.setStyle(adapter.getStyle());
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;
        } else if (viewType == FILE_ATTACHMENT) {
            AttachmentViewHolderFile holder = new AttachmentViewHolderFile(R.layout.stream_item_attachment_file, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else {
            AttachmentViewHolder holder = new AttachmentViewHolder(R.layout.stream_item_attachment, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        }
    }

    public enum Position {
        TOP, MIDDLE, BOTTOM
    }
}
```
