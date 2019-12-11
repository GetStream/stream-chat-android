package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.users.UsersCache;
import com.getstream.sdk.chat.utils.UseCase;

import java.util.Date;
import java.util.List;

import static java.util.UUID.randomUUID;

public class SendNewMessage extends UseCase {

    private final UsersCache usersCache;
    private final MessagesRepository messagesRepository;
    private final Client client;

    public SendNewMessage(
            UsersCache usersCache,
            MessagesRepository messagesRepository,
            Client client
    ) {

        this.usersCache = usersCache;
        this.messagesRepository = messagesRepository;
        this.client = client;
    }

    public void send(String message, List<Attachment> attachments, boolean isUploadingFile, String parentThreadId) {

    }

    /**
     * Prepare message takes the message input string and returns a message object
     * You can overwrite this method in case you want to attach more custom properties to the message
     */
    private Message prepareMessage(String message, List<Attachment> attachments, boolean isUploadingFile, String parentThreadId) {
        Message m = new Message();
        m.setText(message);
        m.setAttachments(attachments);
        m.setUser(client.getState().user);
        // set the thread id if we are viewing a thread
        if (parentThreadId != null)
            m.setParentId(parentThreadId);
        // set the current user

        //m.setUser(viewModel.client().getUser());
        // Check file uploading
        if (isUploadingFile) {
            //String clientSideID = viewModel.getChannel().getClient().generateMessageID();
            m.setId(generateMessageID());
            m.setCreatedAt(new Date());
            m.setSyncStatus(Sync.LOCAL_UPDATE_PENDING);
            m.setAttachments(null);
        }
        return m;
    }

    private String generateMessageID() {
        return usersCache.getCurrentId() + "-" + randomUUID().toString();
    }
}
