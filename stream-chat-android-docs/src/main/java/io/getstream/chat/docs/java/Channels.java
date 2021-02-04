package io.getstream.chat.docs.java;

import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.ChannelsMuteEvent;
import io.getstream.chat.android.client.events.UserStartWatchingEvent;
import io.getstream.chat.android.client.events.UserStopWatchingEvent;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelMute;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Member;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;

import static io.getstream.chat.android.client.api.models.Pagination.LESS_THAN;
import static java.util.Collections.emptyList;

public class Channels {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/initialize_channel/?language=java">Channel Initialization</a>
     */
    class ChannelInitialization {
        public void initialization() {
            ChannelClient channelClient = client.channel("messaging", "general");

            List<String> members = Arrays.asList("thierry", "tommaso");

            Map<String, Object> extraData = new HashMap<>();
            extraData.put("name", "Founder Chat");
            extraData.put("image", "http://bit.ly/2O35mws");

            channelClient.create(members, extraData)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            Channel channel = result.data();
                        } else {
                            // Handle result.error()
                        }
                    });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/creating_channels/?language=java">Creating Channels</a>
     */
    class CreatingChannels {
        public void createAChannel() {
            ChannelClient channelClient = client.channel("messaging", "general");

            Map<String, Object> extraData = new HashMap<>();
            channelClient.create(extraData)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            Channel newChannel = result.data();
                        } else {
                            // Handle result.error()
                        }
                    });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=java">Watching A Channel</a>
     */
    class WatchingAChannel {
        public void watchingASingleChannel() {
            ChannelClient channelClient = client.channel("messaging", "general");

            channelClient.watch().enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void watchingMultipleChannels(String currentUserId) {
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", Arrays.asList(currentUserId))
            );
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<Channel>().desc("last_message_at");
            int messageLimit = 0;
            int memberLimit = 0;

            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit)
                    .withWatch() // Watches the channels automatically
                    .withState();

            // Run query on ChatClient
            client.queryChannels(request).enqueue((result) -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=java#unwatching">Unwacthing</a>
         */
        public void unwatchAChannel() {
            channelClient.stopWatching().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel unwatched
                } else {
                    // Handle result.error()
                }
            });
        }

        public void watcherCount() {
            QueryChannelRequest request = new QueryChannelRequest().withState();
            channelClient.query(request).enqueue((result) -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                    channel.getWatcherCount();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void paginatingChannelWatchers() {
            int limit = 5;
            int offset = 0;
            QueryChannelRequest request = new QueryChannelRequest().withWatchers(limit, offset);
            channelClient.query(request).enqueue((result) -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                    List<User> watchers = channel.getWatchers();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void listeningToChangesInWatchers() {
            // Start watching channel
            channelClient.watch().enqueue((result) -> {
                /* Handle result */
            });

            // Subscribe for watching events
            channelClient.subscribeFor(
                    new Class[]{
                            UserStartWatchingEvent.class,
                            UserStopWatchingEvent.class,
                    },
                    (event) -> {
                        if (event instanceof UserStartWatchingEvent) {
                            // User who started watching the channel
                            User user = ((UserStartWatchingEvent) event).getUser();
                        } else if (event instanceof UserStopWatchingEvent) {
                            // User who stopped watching the channel
                            User user = ((UserStopWatchingEvent) event).getUser();
                        }
                    }
            );
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java">Querying Channels</a>
     */
    class QueryingChannels {
        public void queryChannels() {
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", Arrays.asList("thierry"))
            );
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<Channel>().desc("last_message_at");
            int messageLimit = 0;
            int memberLimit = 0;

            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit)
                    .withWatch()
                    .withState();

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java#common-filters-by-use-case">Common Filters</a>
         */
        class CommonFilters {
            public void channelsThatContainsSpecificUser() {
                FilterObject filter = Filters.in("members", Arrays.asList("thierry"));
            }

            public void channelsThatWithSpecificStatus(User user) {
                FilterObject filter = Filters.and(
                        Filters.eq("agent_id", user.getId()),
                        Filters.in("status", Arrays.asList("pending", "open", "new"))
                );
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java#response">Response</a>
         */
        public void paginatingChannels() {
            // Get the first 10 channels
            FilterObject filter = Filters.in("members", "thierry");
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });

            // Get the second 10 channels
            int nextOffset = 10; // Skips first 10
            QueryChannelsRequest nextRequest = new QueryChannelsRequest(filter, nextOffset, limit, sort, messageLimit, memberLimit);
            client.queryChannels(nextRequest).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_pagination/?language=java">Channel Pagination</a>
     */
    class ChannelPagination {

        public void channelPagination() {
            ChannelClient channelClient = client.channel("messaging", "general");
            int pageSize = 10;

            // Request for the first page
            QueryChannelRequest request = new QueryChannelRequest()
                    .withMessages(pageSize);

            channelClient.query(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.data().getMessages();
                    if (messages.size() < pageSize) {
                        // All messages loaded
                    } else {
                        // Load next page
                        Message lastMessage = messages.get(messages.size() - 1);
                        QueryChannelRequest nextRequest = new QueryChannelRequest()
                                .withMessages(LESS_THAN, lastMessage.getId(), pageSize);
                        // ...
                    }
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_update/?language=java">Updating a Channel</a>
     */
    class UpdatingAChannel {
        public void fullUpdate() {
            ChannelClient channelClient = client.channel("messaging", "general");

            Map<String, Object> channelData = new HashMap<>();
            channelData.put("name", "myspecialchannel");
            channelData.put("color", "green");
            Message updateMessage = new Message();
            updateMessage.setText("Thierry changed the channel color to green");

            channelClient.update(updateMessage, channelData).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_members/?language=java">Updating a Channel</a>
     */
    class ChangingChannelMembers {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_members/?language=java#adding-removing-channel-members">Adding & Removing Channel Members</a>
         */
        public void addingAndRemovingChannelMembers() {
            ChannelClient channelClient = client.channel("messaging", "general");

            // Add members with ids "thierry" and "josh"
            channelClient.addMembers("thierry", "josh").enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });

            // Remove member with id "tommaso"
            channelClient.removeMembers("tommaso").enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=java">One to One Conversations</a>
     */
    class OneToOneConversations {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=java#creating-conversations">Creating Conversations</a>
         */
        public void creatingConversation() {
            String channelType = "messaging";
            List<String> members = Arrays.asList("thierry", "tomasso");
            client.createChannel(channelType, members).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_conversations/?language=java">Invites</a>
     */
    class Invites {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#inviting-users">Iniviting Users</a>
         */
        public void invitingUsers() {
            ChannelClient channelClient = client.channel("messaging", "general");

            Map<String, Object> data = new HashMap<>();
            data.put("members", Arrays.asList("thierry", "tommaso"));
            data.put("invites", Arrays.asList("nick"));

            channelClient.create(data).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#accepting-an-invite">Accept an Invite</a>
         */
        public void acceptingAnInvite() {
            channelClient.acceptInvite("Nick joined this channel!").enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#rejecting-an-invite">Rejecting an Invite</a>
         */
        public void rejectingAnInvite() {
            channelClient.rejectInvite().enqueue(result -> {
                if (result.isSuccess()) {
                    // Invite rejected
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#query-for-accepted-invites">Query For Accepted Invites</a>
         */
        public void queryForAcceptedInvites() {
            FilterObject filter = Filters.eq("invite", "accepted");
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#query-for-rejected-invites">Query For Rejected Invites</a>
         */
        public void queryForRejectedInvites() {
            FilterObject filter = Filters.eq("invite", "rejected");
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=java">Deleting & Hiding a Channel</a>
     */
    class DeletingAndHidingAChannel {

        public void deletingAChannel() {
            ChannelClient channelClient = client.channel("messaging", "general");

            channelClient.delete().enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=java#hiding-a-channel">Hiding a Channel</a>
         */
        public void hidingAChannel() {
            // Hides the channel until a new message is added there
            channelClient.hide(false).enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is hidden
                } else {
                    // Handle result.error()
                }
            });

            // Shows a previously hidden channel
            channelClient.show().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is shown
                } else {
                    // Handle result.error()
                }
            });

            // Hide the channel and clear the message history
            channelClient.hide(true).enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is hidden
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java">Muting Channels</a>
     */
    class MutingChannels {

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#channel-mute">Channel Mute</a>
         */
        // TODO code in this method doesn't match the CMS, review it
        public void channelMute() {
            client.muteChannel("channel-type", "channel-id").enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is muted
                } else {
                    // Handle result.error()
                }
            });

            // Get list of muted channels when user is connected
            User user = new User();
            user.setId("user-id");
            client.connectUser(user, "token").enqueue(result -> {
                if (result.isSuccess()) {
                    // Mutes contains the list of channel mutes
                    List<ChannelMute> mutes = result.data().getUser().getChannelMutes();
                }
            });

            // Get updates about muted channels
            client.subscribeFor(
                    new Class[]{ChannelsMuteEvent.class},
                    channelsMuteEvent -> {
                        List<ChannelMute> mutes = ((ChannelsMuteEvent) channelsMuteEvent).getChannelsMute();
                    }
            );
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#query-muted-channels">Query Muted Channels</a>
         */
        public void queryMutedChannels(String currentUserId, FilterObject filter) {
            // Filter for all channels excluding muted ones
            FilterObject notMutedFilter = Filters.and(
                    Filters.eq("muted", false),
                    Filters.in("members", Arrays.asList(currentUserId))
            );

            // Filter for muted channels
            FilterObject mutedFilter = Filters.eq("muted", true);

            // Executing a channels query with either of the filters
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<>();
            int messageLimit = 0;
            int memberLimit = 0;
            client.queryChannels(new QueryChannelsRequest(
                    filter, // Set the correct filter here
                    offset,
                    limit,
                    sort,
                    messageLimit,
                    memberLimit)).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#remove-a-channel-mute">Remove a Channel Mute</a>
         */
        public void removeAChannelMute() {
            // Unmute channel for current user
            channelClient.unmute().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is unmuted
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_members/?language=java">Query Members</a>
     */
    class QueryMembers {
        public void queryingMembers() {
            ChannelClient channelClient = client.channel("messaging", "general");

            int offset = 0; // Use this value for pagination
            int limit = 10;
            QuerySort<Member> sort = new QuerySort<>();

            // Channel members can be queried with various filters
            // 1. Create the filter, e.g query members by user name
            FilterObject filterByName = Filters.eq("name", "tommaso");
            // 2. Call queryMembers with that filter
            channelClient.queryMembers(offset, limit, filterByName, sort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.data();
                } else {
                    // Handle result.error()
                }
            });

            // Here are some other commons filters you can use:

            // Autocomplete members by user name (names containing "tom")
            FilterObject filterByAutoCompleteName = Filters.autocomplete("name", "tom");

            // Query member by id
            FilterObject filterById = Filters.eq("id", "tommaso");

            // Query multiple members by id
            FilterObject filterByIds = Filters.in("id", Arrays.asList("tommaso", "thierry"));

            // Query channel moderators
            FilterObject filterByModerator = Filters.eq("is_moderator", true);

            // Query for banned members in channel
            FilterObject filterByBannedMembers = Filters.eq("banned", true);

            // Query members with pending invites
            FilterObject filterByPendingInvite = Filters.eq("invite", "pending");

            // Query all the members
            FilterObject filterByNone = new FilterObject();

            // We can order the results too with QuerySort param
            // Here example to order results by member created at descending
            QuerySort<Member> createdAtDescendingSort = new QuerySort<Member>().desc("created_at");
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/slow_mode/?language=java">Throttling & Slow mode</a>
     */
    class ThrottlingAndSlowMode {
        private void disableMessageSendingUi() {
        }

        private void enableMessageSendingUi() {
        }

        public void enableAndDisable() {
            final ChannelClient channelClient = client.channel("messaging", "general");

            // Enable slow mode and set cooldown to 1s
            channelClient.enableSlowMode(1).enqueue(result -> { /* Result handling */ });

            // Increase cooldown to 30s
            channelClient.enableSlowMode(30).enqueue(result -> { /* Result handling */ });

            // Disable slow mode
            channelClient.disableSlowMode().enqueue(result -> { /* Result handling */ });
        }

        public void blockUi() {
            final ChannelClient channelClient = client.channel("messaging", "general");

            // Get the cooldown value
            channelClient.query(new QueryChannelRequest()).enqueue(channelResult -> {
                if (channelResult.isSuccess()) {
                    final Channel channel = channelResult.data();
                    int cooldown = channel.getCooldown();

                    Message message = new Message();
                    message.setText("Hello");
                    channelClient.sendMessage(message).enqueue((messageResult) -> {
                        // After sending a message, block the UI temporarily
                        // The disable/enable UI methods have to be implemented by you
                        disableMessageSendingUi();

                        new Handler(Looper.getMainLooper())
                                .postDelayed(() -> enableMessageSendingUi(), cooldown);
                    });
                }
            });
        }
    }
}
