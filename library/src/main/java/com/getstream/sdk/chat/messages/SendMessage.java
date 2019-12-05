package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.users.UsersRepository;

import java.util.Date;
import java.util.List;

import static java.util.UUID.randomUUID;

public class SendMessage {

    private final UsersRepository usersRepository;

    public SendMessage(UsersRepository usersRepository) {

        this.usersRepository = usersRepository;
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

    public String generateMessageID() {
        return usersRepository.getCurrentId() + "-" + randomUUID().toString();
    }
}
