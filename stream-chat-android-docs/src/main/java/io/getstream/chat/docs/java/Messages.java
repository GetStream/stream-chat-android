package io.getstream.chat.docs.java;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
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

import static io.getstream.chat.docs.StaticInstances.TAG;

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
            // Create a message
            Message message = new Message();
            message.setText("Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.");
            message.getExtraData().put("anotherCustomField", 234);

            // Add an image attachment to the message
            Attachment attachment = new Attachment();
            attachment.setType("image");
            attachment.setImageUrl("https://bit.ly/2K74TaG");
            attachment.setFallback("test image");
            // Add some custom data to the attachment
            attachment.getExtraData().put("myCustomField", 123);

            message.getAttachments().add(attachment);

            // Include the user id of the mentioned user
            User user = new User();
            user.setId("josh-id");
            message.getMentionedUsers().add(user);

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=java#get-a-message">Get A Message</a>
         */
        public void getAMessage() {
            channelClient.getMessage("message-id").enqueue(result -> {
                if (result.isSuccess()) {
                    Message message = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
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
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
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
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/file_uploads/?language=java">File Uploads</a>
     */
    class FileUploads {
        public void fileUploads() {
            File imageFile = new File("path");
            File anyOtherFile = new File("path");

            // Upload an image
            channelClient.sendImage(imageFile, new ProgressCallback() {
                @Override
                public void onSuccess(@NotNull String file) {
                    String fileUrl = file;
                }

                @Override
                public void onError(@NotNull ChatError error) {
                    Log.e(TAG, String.format("There was an error %s", error), error.getCause());
                }

                @Override
                public void onProgress(long progress) {
                    // You can render the uploading progress here
                }
            });

            // Upload a file
            channelClient.sendFile(anyOtherFile, new ProgressCallback() {
                @Override
                public void onSuccess(@NotNull String file) {
                    String fileUrl = file;
                }

                @Override
                public void onError(@NotNull ChatError error) {
                    Log.e(TAG, String.format("There was an error %s", error), error.getCause());
                }

                @Override
                public void onProgress(long progress) {
                    // You can render the uploading progress here
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java">Reactions</a>
     */
    class Reactions {
        public void sendAReaction() {
            Reaction reaction = new Reaction();
            reaction.setMessageId("message-id");
            reaction.setType("like");
            reaction.setScore(1);

            channelClient.sendReaction(reaction).enqueue(result -> {
                if (result.isSuccess()) {
                    Reaction sentReaction = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#removing-a-reaction">Removing A Reaction</a>
         */
        public void removeAReaction() {
            channelClient.deleteReaction("message-id", "like").enqueue(result -> {
                if (result.isSuccess()) {
                    Message message = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#paginating-reactions">Paginating Reactions</a>
         */
        public void paginatingReactions() {
            // Get the first 10 reactions
            channelClient.getReactions("message-id", 0, 10).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Reaction> reactions = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });

            // Get the second 10 reactions
            channelClient.getReactions("message-id", 10, 10).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Reaction> reactions = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });

            // Get 10 reactions after particular reaction
            String reactionId = "reaction-id";
            channelClient.getReactions("message-id", reactionId, 10).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=java#cumulative-clap-reactions">Cumulative (Clap) Reactions</a>
         */
        public void cumulativeReactions() {
            int score = 5;
            Reaction reaction = new Reaction();
            reaction.setMessageId("message-id");
            reaction.setType("like");
            reaction.setScore(score);

            channelClient.sendReaction(reaction).enqueue(result -> {
                if (result.isSuccess()) {
                    Reaction sentReaction = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/threads/?language=java">Threads & Replies</a>
     */
    class ThreadsAndReplies {
        public void startAThread() {
            // Set the parent id to make sure a message shows up in a thread
            Message message = new Message();
            message.setText("hello world");
            message.setParentId(parentMessage.getId());

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
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
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });

            // Retrieve the 20 more messages before the message with id "42"
            client.getRepliesMore(parentMessage.getId(), "42", limit).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> replies = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/silent_messages/?language=java">Silent Messages</a>
     */
    class SilentMessages {
        public void silentMessage() {
            Message message = new Message();
            message.setText("text-of-a-message");
            message.setSilent(true);

            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/search/?language=java">Search</a>
     */
    class Search {
        public void searchMessages() {
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
            ).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });

        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/search/?language=java">Pinned Messages</a>
     */
    class PinnedMessages {
        public void pinAndUnpinAMessage() {
            // create pinned message
            Calendar calendar = Calendar.getInstance();
            calendar.set(2077, 1, 1);
            Date pinExpirationDate = calendar.getTime();

            Message message = new Message();
            message.setText("my-message");
            message.setPinned(true);
            message.setPinExpires(pinExpirationDate);

            channelClient.sendMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message sentMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()));
                }
            });

            // unpin message
            channelClient.unpinMessage(message).enqueue(result -> {
                if (result.isSuccess()) {
                    Message unpinnedMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()));
                }
            });

            // pin message for 120 seconds
            channelClient.pinMessage(message, 120).enqueue(result -> {
                if (result.isSuccess()) {
                    Message pinnedMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()));
                }
            });

            // change message expiration to 2077
            channelClient.pinMessage(message, pinExpirationDate).enqueue(result -> {
                if (result.isSuccess()) {
                    Message pinnedMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()));
                }
            });

            // remove expiration date from pinned message
            channelClient.pinMessage(message, null).enqueue(result -> {
                if (result.isSuccess()) {
                    Message pinnedMessage = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()));
                }
            });
        }

        public void retrievePinnedMessages() {
            int messagesLimit = 10;
            QueryChannelRequest request = new QueryChannelRequest().withMessages(messagesLimit);
            channelClient.query(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> pinnedMessages = result.data().getPinnedMessages();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()));
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
                    Log.e(TAG, String.format("There was an error %s", result.error()));
                }
            });
        }
    }
}
