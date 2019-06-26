package com.getstream.sdk.chat.function;

import android.app.Activity;
import android.content.Intent;

import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.message.Attachment;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer;
import com.getstream.sdk.chat.model.message.SelectAttachmentModel;
import com.getstream.sdk.chat.view.activity.AttachmentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentMediaActivity;

import java.util.ArrayList;
import java.util.List;

public class AttachmentFunction {
    private final String TAG = AttachmentFunction.class.getSimpleName();

    public void progressAttachment(SelectAttachmentModel model, Activity activity) {
        Global.selectAttachmentModel = model;

        List<Attachment> attachments = model.getAttachments();
        if (attachments == null) {
            return;
        }

        // File
        if (attachments.get(0).getType().equals(ModelType.attach_file)) {
            loadFile(model, activity);
            return;
        }

        // Image
        if (getImageURLs(attachments).size() > 0) {
            new ImageViewer.Builder<>(activity, getImageURLs(attachments))
                    .setStartPosition(0)
                    .show();
            return;
        }

        // Giphy, Video, Link, Product,...
        Intent intent = new Intent(activity, AttachmentActivity.class);
        activity.startActivity(intent);
    }

    // region Load Image
    private List<String> getImageURLs(List<Attachment> attachments) {
        List<String> imageURLs = new ArrayList<>();
        if (attachments.size() == 1) {
            if (attachments.get(0).getType().equals(ModelType.attach_image)) {
                if (attachments.get(0).getOgURL() == null) {
                    String url = attachments.get(0).getImageURL();
                    imageURLs.add(url);
                }
            }
        } else {
            Attachment attachment = attachments.get(0);
            if (attachment.getType().equals(ModelType.attach_image)) {
                for (int i = 0; i < attachments.size(); i++) {
                    if (attachments.get(i).getOgURL() == null && attachments.get(i).getImageURL() != null) {
                        imageURLs.add(attachments.get(i).getImageURL());
                    }
                }
            }
        }
        return imageURLs;
    }


    // endregion

    // region Load File
    private void loadFile(SelectAttachmentModel model, final Activity activity) {
        List<Attachment> attachments = model.getAttachments();
        int index = model.getAttachmentIndex();
        final Attachment attachment = attachments.get(index);

        // Media
        if (attachment.getMime_type().contains("audio") ||
                attachment.getMime_type().contains("video")) {
            Intent intent = new Intent(activity, AttachmentMediaActivity.class);
            activity.startActivity(intent);
            return;
        }

        // Office
        if (attachment.getMime_type().equals("application/msword") ||
                attachment.getMime_type().equals(ModelType.attach_mime_txt) ||
                attachment.getMime_type().equals(ModelType.attach_mime_pdf) ||
                attachment.getMime_type().contains("application/vnd")) {
            loadDocument(attachment.getAssetURL(), activity);
        }
    }

    private void loadDocument(String url, Activity activity) {
        Intent intent = new Intent(activity, AttachmentDocumentActivity.class);
        intent.putExtra(Constant.TAG_ATTACH_FILE_PATH, url);
        activity.startActivity(intent);
    }

    // endregion
}
