package io.getstream.chat.docs.java;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
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
import io.getstream.chat.docs.java.helpers.MyFileUploader;

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
            Message message = new Message();
            message.setText("Josh, I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.");

            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void sendAComplexMessage() {
            // Create an image attachment
            Attachment attachment = new Attachment();
            attachment.setType("image");
            attachment.setImageUrl("https://bit.ly/2K74TaG");
            attachment.setThumbUrl("https://bit.ly/2Uumxti");
            attachment.getExtraData().put("myCustomField", 123);

            // Create a message with the attachment and a user mention
            Message message = new Message();
            message.setText("@Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.");
            message.getAttachments().add(attachment);
            message.setMentionedUsersIds(Arrays.asList("josh-id"));
            message.getExtraData().put("anotherCustomField", 234);

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#get-a-message">Get A Message</a>
         */
        public void getAMessage() {
            channelClient.getMessage("message-id").enqueue(result -> {
                if (result.isSuccess()) {
                    Message message = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#update-a-message">Update A Message</a>
         */
        public void updateAMessage() {
            // Update some field of the message
            message.setText("my updated text");

            // Send the message to the channel
            channelClient.updateMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message updatedMessage = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#delete-a-message">Delete A Message</a>
         */
        public void deleteAMessage() {
            channelClient.deleteMessage("message-id").enqueue(result -> {
                if (result.isSuccess()) {
                    Message deletedMessage = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    class MessageFormat {
        public void openGraphScraper() {
            Message message = new Message();
            message.setText("Check this bear out https://imgur.com/r/bears/4zmGbMN");

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
                    // to an message that you then send to a channel
                    String imageUrl = result.data();

                    Attachment attachment = new Attachment();
                    attachment.setType("image");
                    attachment.setImageUrl(imageUrl);

                    Message message = new Message();
                    message.getAttachments().add(attachment);

                    channelClient.sendMessage(message).enqueue(res -> { /* ... */ });
                }
            });

            // Upload a file, monitoring for progress with a ProgressCallback
            channelClient.sendFile(anyOtherFile, new ProgressCallback() {
                @Override
                public void onSuccess(@NotNull String file) {
                    String fileUrl = file;
                }

                @Override
                public void onError(@NotNull ChatError error) {
                    // Handle error
                }

                @Override
                public void onProgress(long progress) {
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
     * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java">Reactions</a>
     */
    class Reactions {
        public void sendAReaction() {
            ChannelClient channelClient = client.channel("messaging", "general");

            // Add reaction 'like' with a custom field
            Reaction reaction = new Reaction();
            reaction.setMessageId("message-id");
            reaction.setType("like");
            reaction.setScore(1);
            reaction.getExtraData().put("customField", 1);

            boolean enforceUnique = false; // Don't remove other existing reactions
            channelClient.sendReaction(reaction, enforceUnique).enqueue(result -> {
                if (result.isSuccess()) {
                    Reaction sentReaction = result.data();
                } else {
                    // Handle result.error()
                }
            });

            // Add reaction 'like' and replace all other reactions of this user by it
            enforceUnique = true;
            channelClient.sendReaction(reaction, enforceUnique).enqueue(result -> {
                if (result.isSuccess()) {
                    Reaction sentReaction = result.data();
                } else {
                    // Handle result.error()
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
                    Message message = result.data();
                } else {
                    // Handle result.error()
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
                    List<Reaction> reactions = result.data();
                } else {
                    // Handle result.error()
                }
            });

            // Get the second 10 reactions
            offset = 10;
            channelClient.getReactions("message-id", offset, limit)
                    .enqueue(result -> { /* ... */ });

            // Get 10 reactions after particular reaction
            String reactionId = "reaction-id";
            channelClient.getReactions("message-id", reactionId, limit)
                    .enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#cumulative-clap-reactions">Cumulative (Clap) Reactions</a>
         */
        public void cumulativeReactions() {
            Reaction reaction = new Reaction();
            reaction.setMessageId("message-id");
            reaction.setType("like");
            reaction.setScore(5);

            boolean enforceUnique = false;
            channelClient.sendReaction(reaction, enforceUnique).enqueue(result -> { /* ... */ });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/threads/?language=java">Threads & Replies</a>
     */
    class ThreadsAndReplies {
        public void startAThread() {
            Message message = new Message();
            message.setText("Hello there!");
            message.setParentId(parentMessage.getId());

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.data();
                } else {
                    // Handle result.error()
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
                    List<Message> replies = result.data();
                } else {
                    // Handle result.error()
                }
            });

            // Retrieve the 20 more messages before the message with id "42"
            client.getRepliesMore(parentMessage.getId(), "42", limit).enqueue(result -> { /* ... */ });
        }

        Message originalMessage;

        public void quoteMessage() {
            Message message = new Message();
            message.setText("This message quotes another message!");
            message.setReplyMessageId(originalMessage.getId());

            channelClient.sendMessage(message).enqueue(result -> { /* ... */ });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/silent_messages/?language=java">Silent Messages</a>
     */
    class SilentMessages {
        User systemUser;

        public void silentMessage() {
            Message message = new Message();
            message.setText("You and Jane are now matched!");
            message.setUser(systemUser);
            message.setSilent(true);

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

            FilterObject channelFilter = Filters.in("members", Arrays.asList("john"));
            FilterObject messageFilter = Filters.autocomplete("text", "supercalifragilisticexpialidocious");

            client.searchMessages(
                    new SearchMessagesRequest(
                            offset,
                            limit,
                            channelFilter,
                            messageFilter
                    )
            ).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void searchMessagesWithAttachments() {
            int offset = 0;
            int limit = 10;
            String type = "image";
            channelClient.getMessagesWithAttachments(offset, limit, type).enqueue(result -> {
                if (result.isSuccess()) {
                    // These messages will contain at least one of the desired
                    // type of attachment, but not necessarily all of their
                    // attachments will have the specified type
                    List<Message> messages = result.data();
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

            Message message = new Message();
            message.setText("Hey punk");
            message.setPinned(true);
            message.setPinExpires(pinExpirationDate);

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
                    List<Message> pinnedMessages = result.data().getPinnedMessages();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void searchForAllPinnedMessages() {
            int offset = 0;
            int limit = 10;
            FilterObject channelFilter = Filters.in("cid", "channelType:channelId");
            FilterObject messageFilter = Filters.eq("pinned", true);
            SearchMessagesRequest request = new SearchMessagesRequest(offset, limit, channelFilter, messageFilter);

            client.searchMessages(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> pinnedMessages = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }
}
