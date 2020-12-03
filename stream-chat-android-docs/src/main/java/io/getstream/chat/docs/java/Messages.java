package io.getstream.chat.docs.java;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.SearchMessagesRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.Reaction;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.client.utils.ProgressCallback;
import kotlin.Unit;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class Messages {
    static ChatClient client;
    static ChannelClient channelController;
    static Message message;
    static Message parentMessage;

    public static void sendAMessage() {
        // create a message
        Message message = new Message();
        message.setText("Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.");
        message.getExtraData().put("anotherCustomField", 234);

        // add an image attachment to the message
        Attachment attachment = new Attachment();
        attachment.setType("image");
        attachment.setImageUrl("https://bit.ly/2K74TaG");
        attachment.setFallback("test image");
        // add some custom data to the attachment
        attachment.getExtraData().put("myCustomField", 123);

        message.getAttachments().add(attachment);

        // include the user id of the mentioned user
        User user = new User();
        user.setId("josh-id");
        message.getMentionedUsers().add(user);

        // send the message to the channel
        channelController.sendMessage(message).enqueue(result -> Unit.INSTANCE);
    }

    public static void getAMessage() {
        channelController.getMessage("message-id").enqueue(result -> {
            Message message = result.data();
            return Unit.INSTANCE;
        });
    }

    public static void updateAMessage() {
        // update some field of the message
        message.setText("my updated text");

        // send the message to the channel
        channelController.updateMessage(message).enqueue(result -> {
            Message updatedMessage = result.data();
            return Unit.INSTANCE;
        });
    }

    public static void deleteAMessage() {
        channelController.deleteMessage("message-id").enqueue(result -> Unit.INSTANCE);
    }

    public static void fileUploads() {
        File imageFile = new File("path");
        File anyOtherFile = new File("path");

        // upload an image
        channelController.sendImage(imageFile, new ProgressCallback() {
            @Override
            public void onSuccess(@NotNull String file) {

            }

            @Override
            public void onError(@NotNull ChatError error) {

            }

            @Override
            public void onProgress(long progress) {

            }
        });

        // upload a file
        channelController.sendFile(anyOtherFile, new ProgressCallback() {
            @Override
            public void onSuccess(@NotNull String file) {

            }

            @Override
            public void onError(@NotNull ChatError error) {

            }

            @Override
            public void onProgress(long progress) {

            }
        });
    }

    public static void sendAReaction() {
        Reaction reaction = new Reaction();
        reaction.setMessageId("message-id");
        reaction.setType("like");
        reaction.setScore(1);

        channelController.sendReaction(reaction).enqueue(result -> Unit.INSTANCE);
    }

    public static void removeAReaction() {
        channelController.deleteReaction("message-id", "like").enqueue(result -> Unit.INSTANCE);
    }

    public static void paginatingReactions() {
        // get the first 10 reactions
        channelController.getReactions("message-id", 0, 10).enqueue(result -> Unit.INSTANCE);

        // get the second 10 reactions
        channelController.getReactions("message-id", 10, 10).enqueue(result -> Unit.INSTANCE);

        // get 10 reactions after particular reaction
        String reactionId = "reaction-id";
        channelController.getReactions("message-id", reactionId, 10).enqueue(result -> Unit.INSTANCE);
    }

    public static void cumulativeReactions() {
        int score = 5;
        Reaction reaction = new Reaction();
        reaction.setMessageId("message-id");
        reaction.setType("like");
        reaction.setScore(score);

        channelController.sendReaction(reaction).enqueue(result -> Unit.INSTANCE);
    }

    public static void startAThread() {
        // set the parent id to make sure a message shows up in a thread
        Message message = new Message();
        message.setText("hello world");
        message.setParentId(parentMessage.getId());

        // send the message to the channel
        channelController.sendMessage(message).enqueue(result -> Unit.INSTANCE);
    }

    public static void threadPagination() {
        int limit = 20;
        // retrieve the first 20 messages inside the thread
        client.getReplies(parentMessage.getId(), limit).enqueue(result -> {
            List<Message> replies = result.data();
            return Unit.INSTANCE;
        });

        // retrieve the 20 more messages before the message with id "42"
        client.getRepliesMore(parentMessage.getId(), "42", limit).enqueue(result -> {
            List<Message> replies = result.data();
            return Unit.INSTANCE;
        });
    }

    public static void silentMessage() {
        Message message = new Message();
        message.setText("text-of-a-message");
        message.setSilent(true);

        channelController.sendMessage(message).enqueue(result -> Unit.INSTANCE);
    }

    public static void searchMessages() {
        int offset = 0;
        int limit = 10;
        String query = "supercalifragilisticexpialidocious";
        ArrayList<String> searchUsersList = new ArrayList<>();
        searchUsersList.add("john");
        FilterObject channelFilter = Filters.in("members", searchUsersList);
        FilterObject messageFilter = Filters.autocomplete("text", query);

        client.searchMessages(
                new SearchMessagesRequest(
                        offset,
                        limit,
                        channelFilter,
                        messageFilter
                )
        ).enqueue(listResult -> {
            if (listResult.isSuccess()) {
                List<Message> messages = listResult.data();
            } else {
                Log.e(TAG, String.format("There was an error %s", listResult.error(), listResult.error().getCause()));
            }
            return Unit.INSTANCE;
        });

    }
}
