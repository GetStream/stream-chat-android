package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.view.Dialog.MessageMoreActionDialog;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.example.adapter.CustomMessageViewHolderFactory;
import io.getstream.chat.example.databinding.ActivityChannelBinding;
import io.getstream.chat.example.navigation.SearchDestination;
import io.getstream.chat.example.view.fragment.ChannelListFragment;

/**
 * Show the messages for a channel
 */
public class ChannelActivity extends AppCompatActivity
        implements MessageListView.MessageLongClickListener,
        MessageListView.AttachmentClickListener,
        MessageListView.HeaderOptionsClickListener,
        MessageListView.HeaderAvatarGroupClickListener,
        MessageListView.UserClickListener,
        MessageInputView.PermissionRequestListener {

    static final String TAG = ChannelActivity.class.getSimpleName();
    static final String STATE_TEXT = "messageText";

    private ChannelViewModel viewModel;
    private ActivityChannelBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // receive the intent and create a channel object
        Intent intent = getIntent();
        String channelType = intent.getStringExtra(ChannelListFragment.EXTRA_CHANNEL_TYPE);
        String channelID = intent.getStringExtra(ChannelListFragment.EXTRA_CHANNEL_ID);
        ChatClient client = StreamChat.getInstance();

        // we're using data binding in this example
        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel);
        // most the business logic of the chat is handled in the ChannelViewModel view model
        binding.setLifecycleOwner(this);
        if (savedInstanceState != null) {
            String messageText = savedInstanceState.getString(STATE_TEXT);
            binding.messageInput.setMessageText(messageText);
        }

        Channel channel = new Channel();
        channel.setType(channelType);
        channel.setId(channelID);

        // setup the viewmodel, remember to also set the channel
        viewModel = ViewModelProviders.of(this).get(ChannelViewModel.class);
        viewModel.setChannel(channel);
        viewModel.getCurrentUserUnreadMessageCount().observe(this, (Number count) -> {
            Log.i(TAG, String.format("The current user unread count is now %d", count));
        });

        // set listeners
        binding.messageList.setMessageLongClickListener(this);
        binding.messageList.setUserClickListener(this);
        binding.messageList.setAttachmentClickListener(this);
        // If you are using own MessageInputView please comment this line.
        binding.messageInput.setPermissionRequestListener(this);
        binding.messageList.setViewHolderFactory(new CustomMessageViewHolderFactory());

        // connect the view model
        Log.d(TAG, "Set View Model = " + viewModel);
        binding.setViewModel(viewModel);
        binding.channelHeader.setViewModel(viewModel, this);
        binding.channelHeader.setHeaderOptionsClickListener(this);
        binding.channelHeader.setHeaderAvatarGroupClickListener(this);
        binding.messageList.setViewModel(viewModel, this);
        binding.messageInput.setViewModel(viewModel, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TEXT, binding.messageInput.getMessageText());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If you are using own MessageInputView please comment this line.
        binding.messageInput.captureMedia(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // If you are using own MessageInputView please comment this line.
        binding.messageInput.permissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void openPermissionRequest() {
        PermissionChecker.permissionCheck(this, null);
        // If you are writing a Channel Screen in a Fragment, use the code below instead of the code above.
        //   PermissionChecker.permissionCheck(getActivity(), this);
    }

    @Override
    public void onMessageLongClick(Message message) {
        new MessageMoreActionDialog(this)
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
        openSearchActivity();
    }

    @Override
    public void onHeaderAvatarGroupClick(Channel channel) {

        String name = LlcMigrationUtils.getName(channel);

        new AlertDialog.Builder(this)
                .setTitle("Avatar group click for channel " + name)
                .setMessage("You pressed on the avatar group, well done")
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.stream_ic_settings)
                .show();
    }

    @Override
    public void onUserClick(User user) {
        // open your user profile
    }

    private void openSearchActivity() {
        StreamChat.getNavigator().navigate(new SearchDestination(viewModel.getChannel().getCid(), this));
    }
}