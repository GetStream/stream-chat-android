package io.getstream.chat.docs.java.client.cms;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.utils.ProgressCallback;
import io.getstream.chat.android.models.Attachment;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.models.Reaction;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.docs.java.client.helpers.MyFileUploader;
import io.getstream.result.Error;

public class Messages {
    private ChatClient client;
    private ChannelClient channelClient;
    private Message message;
    private Message parentMessage;

    /**
     * @see <a href="https://getstream.io/chat/docs/send_message/?language=java">Messages Overview</a>
     */
    class MessagesOverview {
        public void sendAMessage() {
            ChannelClient channelClient = client.channel("messaging", "general");
            Message message = new Message.Builder()
                    .withText("Josh, I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.")
                    .build();

            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        public void sendAComplexMessage() {
            // Create an image attachment
            Map<String, Object> customData = new HashMap<>();
            customData.put("myCustomField", 123);
            Attachment attachment = new Attachment.Builder()
                    .withType("image")
                    .withImageUrl("https://bit.ly/2K74TaG")
                    .withThumbUrl("https://bit.ly/2Uumxti")
                    .withExtraData(customData)
                    .build();

            // Create a message with the attachment and a user mention
            Message message = new Message.Builder()
                    .withText("@Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.")
                    .withAttachments(Collections.singletonList(attachment))
                    .withMentionedUsersIds(Collections.singletonList("josh-id"))
                    .withExtraData(Collections.singletonMap("anotherCustomField", 234))
                    .build();

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#get-a-message">Get A Message</a>
         */
        public void getAMessage() {
            channelClient.getMessage("message-id").enqueue(result -> {
                if (result.isSuccess()) {
                    Message message = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#update-a-message">Update A Message</a>
         */
        public void updateAMessage() {
            // Update some field of the message
            Message updatedMessage = message.newBuilder()
                    .withText("my updated text")
                    .build();

            // Send the message to the channel
            channelClient.updateMessage(updatedMessage).enqueue(result -> {
                if (result.isSuccess()) {
                    Message remoteUpdatedMessage = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        public void partialUpdateAMessage() {
            // Update some field of the message
            Message updatedMessage = message.newBuilder()
                    .withText("my updated text")
                    .build();

            // Send the message to the channel
            channelClient.updateMessage(updatedMessage).enqueue(result -> {
                if (result.isSuccess()) {
                    Message remoteUpdatedMessage = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#delete-a-message">Delete A Message</a>
         */
        public void deleteAMessage() {
            channelClient.deleteMessage("message-id", false).enqueue(result -> {
                if (result.isSuccess()) {
                    Message deletedMessage = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

    class MessageFormat {
        /**
         * @see <a href="https://getstream.io/chat/docs/android/message_format/?language=java">Open Graph Scrapper</a>
         */
        public void openGraphScraper() {
            Message message = new Message.Builder()
                    .withText("Check this bear out https://imgur.com/r/bears/4zmGbMN")
                    .build();

            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/file_uploads/?language=java">File Uploads</a>
     */
    class FileUploads {
        File imageFile = new File("path");
        File anyOtherFile = new File("path");

        public void fileUploads() {
            ChannelClient channelClient = client.channel("messaging", "general");

            // Upload an image without detailed progress
            channelClient.sendImage(imageFile).enqueue(result -> {
                if (result.isSuccess()) {
                    // Successful upload, you can now attach this image
                    // to a message that you then send to a channel
                    String imageUrl = result.getOrNull().getFile();

                    Attachment attachment = new Attachment.Builder()
                            .withImageUrl(imageUrl)
                            .withType("image")
                            .build();

                    Message message = new Message();
                    message.getAttachments().add(attachment);

                    channelClient.sendMessage(message).enqueue(res -> { /* ... */ });
                } else {
                    // Handle error
                }
            });

            // Upload a file, monitoring for progress with a ProgressCallback
            channelClient.sendFile(anyOtherFile, new ProgressCallback() {
                @Override
                public void onSuccess(@NotNull String file) {
                    String fileUrl = file;
                }

                @Override
                public void onError(@NotNull Error error) {
                    // Handle error
                }

                @Override
                public void onProgress(long bytesUploaded, long totalBytes) {
                    // You can render the uploading progress here
                }
            }).enqueue(); // No callback passed to enqueue, as we'll get notified above anyway
        }

        public void usingYourOwnCdn(String apiKey, Context context) {
            // Set a custom FileUploader implementation when building your client
            ChatClient client = new ChatClient.Builder("{{ api_key }}", context)
                    .fileUploader(new MyFileUploader())
                    .build();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin#deleting-files-and-images">Deleting Files and Images</a>
     */
    void deleteFileOrImage() {
        ChannelClient channelClient = client.channel("messaging", "general");

        // Deletes the image
        channelClient.deleteImage("{{ url of uploaded image }}").enqueue();

        // Deletes the file
        channelClient.deleteFile("{{ url of uploaded file }}").enqueue();
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java">Reactions</a>
     */
    class Reactions {
        public void sendAReaction() {
            ChannelClient channelClient = client.channel("messaging", "general");

            // Add reaction 'like' with a custom field
            Map<String, Object> customFields = new HashMap<>();
            customFields.put("customField", 1);
            Reaction reaction = new Reaction.Builder()
                    .withMessageId("message-id")
                    .withType("like")
                    .withScore(1)
                    .withExtraData(customFields)
                    .build();

            boolean enforceUnique = false; // Don't remove other existing reactions
            channelClient.sendReaction(reaction, enforceUnique).enqueue(result -> {
                if (result.isSuccess()) {
                    Reaction sentReaction = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            // Add reaction 'like' and replace all other reactions of this user by it
            enforceUnique = true;
            channelClient.sendReaction(reaction, enforceUnique).enqueue(result -> {
                if (result.isSuccess()) {
                    Reaction sentReaction = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#removing-a-reaction">Removing A Reaction</a>
         */
        public void removeAReaction() {
            String reactionType = "like";
            channelClient.deleteReaction("message-id", reactionType).enqueue(result -> {
                if (result.isSuccess()) {
                    Message message = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#paginating-reactions">Paginating Reactions</a>
         */
        public void paginatingReactions() {
            // Get the first 10 reactions
            int offset = 0;
            int limit = 10;
            channelClient.getReactions("message-id", offset, limit).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Reaction> reactions = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            // Get the second 10 reactions
            offset = 10;
            channelClient.getReactions("message-id", offset, limit)
                    .enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#cumulative-clap-reactions">Cumulative (Clap) Reactions</a>
         */
        public void cumulativeReactions() {
            Reaction reaction = new Reaction.Builder()
                    .withMessageId("message-id")
                    .withType("like")
                    .withScore(5)
                    .build();

            boolean enforceUnique = false;
            channelClient.sendReaction(reaction, enforceUnique).enqueue(result -> { /* ... */ });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/threads/?language=java">Threads & Replies</a>
     */
    class ThreadsAndReplies {
        public void startAThread() {
            Message message = new Message.Builder()
                    .withText("Hello there!")
                    .withParentId(parentMessage.getId())
                    .build();

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/threads/?language=java#thread-pagination">Thread Pagination</a>
         */
        public void threadPagination() {
            int limit = 20;
            // Retrieve the first 20 messages inside the thread
            client.getReplies(parentMessage.getId(), limit).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> replies = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            // Retrieve the 20 more messages before the message with id "42"
            client.getRepliesMore(parentMessage.getId(), "42", limit).enqueue(result -> { /* ... */ });
        }

        Message originalMessage;

        public void quoteMessage() {
            Message message = new Message.Builder()
                    .withText("This message quotes another message!")
                    .withReplyMessageId(originalMessage.getId())
                    .build();

            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/silent_messages/?language=java">Silent Messages</a>
     */
    class SilentMessages {
        User systemUser;

        public void silentMessage() {
            Message message = new Message.Builder()
                    .withText("You and Jane are now matched!")
                    .withUser(systemUser)
                    .withSilent(true)
                    .build();

            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/search/?language=java">Search</a>
     */
    class Search {
        public void searchMessages() {
            int offset = 0;
            int limit = 10;

            FilterObject channelFilter = Filters.in("members", Collections.singletonList("john"));
            FilterObject messageFilter = Filters.autocomplete("text", "supercalifragilisticexpialidocious");

            client.searchMessages(
                    channelFilter,
                    messageFilter,
                    offset,
                    limit,
                    null,
                    null
            ).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.getOrNull().getMessages();
                } else {
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/search/?language=java">Pinned Messages</a>
     */
    class PinnedMessages {
        public void pinAndUnpinAMessage() {
            // Create pinned message
            Calendar calendar = Calendar.getInstance();
            calendar.set(2077, 1, 1);
            Date pinExpirationDate = calendar.getTime();

            Message message = new Message.Builder()
                    .withText("Hey punk")
                    .withPinned(true)
                    .withPinExpires(pinExpirationDate)
                    .build();

            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });

            // Unpin message
            channelClient.unpinMessage(message).enqueue(result -> { /* ... */ });

            // Pin message for 120 seconds
            channelClient.pinMessage(message, 120).enqueue(result -> { /* ... */ });

            // Change message expiration to 2077
            channelClient.pinMessage(message, pinExpirationDate).enqueue(result -> { /* ... */ });

            // Remove expiration date from pinned message
            channelClient.pinMessage(message, null).enqueue(result -> { /* ... */ });
        }

        public void retrievePinnedMessages() {
            channelClient.query(new QueryChannelRequest()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> pinnedMessages = result.getOrNull().getPinnedMessages();
                } else {
                    // Handle error
                }
            });
        }

        public void paginateOverAllPinnedMessages() {
            // List the first page of pinned messages, pinned before now, of the channel with descending direction (newest on top)
            channelClient.getPinnedMessages(10, QuerySortByField.descByName("pinnedAt"), new PinnedMessagesPagination.BeforeDate(new Date(), false))
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            List<Message> pinnedMessages = result.getOrNull();
                        } else {
                            // Handle error
                        }
                    });

            // You can use a pinnedAt date retrieved from the previous request to get the next page
            Date nextDate = new Date();
            // List the next page of pinned messages
            channelClient.getPinnedMessages(10, QuerySortByField.descByName("pinnedAt"), new PinnedMessagesPagination.BeforeDate(nextDate, false))
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            List<Message> pinnedMessages = result.getOrNull();
                        } else {
                            // Handle error
                        }
                    });
        }
    }

    public class Translation {

        /**
         * @see <a href="https://getstream.io/chat/docs/translation/?language=java#message-translation-endpoint">Message Translation</a>
         */
        public void messageTranslation() {
            // Translate message to French
            ChannelClient channelClient = client.channel("messaging", "general");

            Message message = new Message.Builder()
                    .withText("Hello, I would like to have more information about your product.")
                    .build();

            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    String messageId = result.getOrNull().getId();
                    client.translate(messageId, "fr").enqueue(translationResult -> {
                        if (result.isSuccess()) {
                            Message translatedMessage = result.getOrNull();
                            String translation = translatedMessage.getI18n().get("fr_text");
                        }
                         else {
                             // Handle error
                        }
                    });
                } else {
                    // Handle error
                }
            });
        }
    }
}
