package com.getstream.sdk.chat.model;

import java.util.List;

public class SelectAttachmentModel {
    private int attachmentIndex;
    private List<Attachment> attachments;

    public int getAttachmentIndex() {
        return attachmentIndex;
    }

    public void setAttachmentIndex(int attachmentIndex) {
        this.attachmentIndex = attachmentIndex;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
