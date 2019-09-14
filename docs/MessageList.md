### Message List

The message list renders a list of messages. You can use it like this:

```xml
<com.getstream.sdk.chat.view.MessageListView
    android:id="@+id/messageList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    android:background="#FFF"
    app:layout_constraintBottom_toTopOf="@+id/message_input"
    app:layout_constraintEnd_toStartOf="@+id/message_input"
    app:layout_constraintStart_toEndOf="@+id/channelHeader"
    app:layout_constraintTop_toBottomOf="@+id/channelHeader"
    stream:reactionDlgEmojiSize="50dp"
    stream:reactionDlgbgColor="#001DC4"
    stream:showUsersReactionDlg="true"
    stream:userAvatarTextColor="#FFF"
    stream:userAvatarTextStyle="bold"
    stream:userReadStateAvatarHeight="15dp"
    stream:userReadStateAvatarWidth="15dp"
    stream:userReadStateTextColor="#FFF"
    stream:userReadStateTextStyle="bold"
    stream:userRreadStateTextSize="9sp" />
```

And here's a full example of an activity that renders a message list, channel header and message input

```java
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
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.PermissionChecker;
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
//        binding.messageList.setMessageClickListener(this);
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
    public void onMessageClick(Message message, int position) {
        Log.i(TAG, "message was clicked");
        showReactionDialog(message);
    }

    @Override
    public void onAttachmentClick(Message message, Attachment attachment) {
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

    }
}
```

#### Customizing the message list - Attributes

TODO document the attributes for styling the message list

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
    private static int NOT_FOUND = 0;
    private static int DATE_SEPARATOR = 1;
    private static int MESSAGE = 2;
    private static int TYPING = 3;

    private static int GENERIC_ATTACHMENT = 1;
    private static int IMAGE_ATTACHMENT = 2;
    private static int VIDEO_ATTACHMENT = 3;
    private static int FILE_ATTACHMENT = 4;


    public enum Position {
        TOP, MIDDLE, BOTTOM
    }

    public int getEntityViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        // typing
        // date
        // various message types
        MessageListItemAdapter.EntityType messageListItemType = messageListItem.getType();
        if (messageListItemType == MessageListItemAdapter.EntityType.DATE_SEPARATOR) {
            return DATE_SEPARATOR;
        } else if (messageListItemType == MessageListItemAdapter.EntityType.MESSAGE) {
            return MESSAGE;
        } else if (messageListItemType == MessageListItemAdapter.EntityType.TYPING) {
            return TYPING;
        }
        return NOT_FOUND;
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
        } else if (t.equals(ModelType.attach_image)) {
            return IMAGE_ATTACHMENT;
        } else if (t.equals(ModelType.attach_file)) {
            return FILE_ATTACHMENT;
        } else {
            return GENERIC_ATTACHMENT;
        }

    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent,int viewType) {
        if (viewType == DATE_SEPARATOR) {
            DateSeparatorViewHolder holder = new DateSeparatorViewHolder(R.layout.list_item_date_separator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == MESSAGE) {
            MessageListItemViewHolder holder = new MessageListItemViewHolder(R.layout.list_item_message, parent);
            holder.setViewHolderFactory(this);
            holder.setStyle(adapter.getStyle());
            return holder;

        } else if (viewType == TYPING) {
            TypingIndicatorViewHolder holder = new TypingIndicatorViewHolder(R.layout.list_item_type_indicator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else {
            return null;
        }
    }

    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == VIDEO_ATTACHMENT || viewType == IMAGE_ATTACHMENT) {
            AttachmentViewHolderMedia holder = new AttachmentViewHolderMedia(R.layout.list_item_attachment_video, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == FILE_ATTACHMENT) {
            AttachmentViewHolderFile holder = new AttachmentViewHolderFile(R.layout.list_item_attachment_file, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else {
            AttachmentViewHolder holder = new AttachmentViewHolder(R.layout.list_item_attachment, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        }

    }
}
```
