package io.getstream.chat.docs.java.client.cms;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static io.getstream.chat.android.client.api.models.Pagination.LESS_THAN;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.NeutralFilterObject;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.models.querysort.QuerySorter;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent;
import io.getstream.chat.android.client.events.UserStartWatchingEvent;
import io.getstream.chat.android.client.events.UserStopWatchingEvent;
import io.getstream.chat.android.client.extensions.ChannelExtensionKt;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.ChannelCapabilities;
import io.getstream.chat.android.models.ChannelMute;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.Member;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.models.User;

public class Channels {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/creating_channels/?language=java">Creating Channels</a>
     */
    class CreatingChannels {
        public void createAChannel() {
            ChannelClient channelClient = client.channel("messaging", "general");

            Map<String, Object> extraData = new HashMap<>();
            List<String> memberIds = new LinkedList<>();

            channelClient.create(memberIds, extraData)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            Channel newChannel = result.getOrNull();
                        } else {
                            // Handle error
                        }
                    });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/react/creating_channels/?language=java#2.-creating-a-channel-for-a-list-of-members">Creating a Channel for a List of Members</a>
         */
        public void createChannelWithListOfMembers() {
            ChannelClient channelClient = client.channel("messaging", "");

            Map<String, Object> extraData = new HashMap<>();
            List<String> memberIds = new LinkedList<>();
            memberIds.add("thierry");
            memberIds.add("tomasso");

            channelClient.create(memberIds, extraData)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            Channel newChannel = result.getOrNull();
                        } else {
                            // Handle error
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
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        public void watchingMultipleChannels(String currentUserId) {
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", Collections.singletonList(currentUserId))
            );
            int offset = 0;
            int limit = 10;
            QuerySortByField<Channel> sort = QuerySortByField.descByName("lastMessageAt");
            int messageLimit = 0;
            int memberLimit = 0;

            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit)
                    .withWatch() // Watches the channels automatically
                    .withState();

            // Run query on ChatClient
            client.queryChannels(request).enqueue((result) -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.getOrNull();
                } else {
                    // Handle error
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
                    // Handle error
                }
            });
        }

        public void watcherCount() {
            QueryChannelRequest request = new QueryChannelRequest().withState();
            channelClient.query(request).enqueue((result) -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                    channel.getWatcherCount();
                } else {
                    // Handle error
                }
            });
        }

        public void paginatingChannelWatchers() {
            int limit = 5;
            int offset = 0;
            QueryChannelRequest request = new QueryChannelRequest().withWatchers(limit, offset);
            channelClient.query(request).enqueue((result) -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                    List<User> watchers = channel.getWatchers();
                } else {
                    // Handle error
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
     * @see <a href="https://getstream.io/chat/docs/channel_update/?language=java">Updating a Channel</a>
     */
    class UpdatingAChannel {

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_update/?language=java#partial-update">Partial Update</a>
         */
        public void partialUpdate() {
            // Here's a channel with some custom field data that might be useful
            ChannelClient channelClient = client.channel("messaging", "general");

            List<String> members = Arrays.asList("thierry", "tommaso");

            Map<String, String> channelDetail = new HashMap<>();
            channelDetail.put("topic", "Plants and Animals");
            channelDetail.put("rating", "pg");

            Map<String, Integer> userId = new HashMap<>();
            userId.put("user_id", 123);

            Map<String, Object> extraData = new HashMap<>();
            extraData.put("source", "user");
            extraData.put("source_detail", userId);
            extraData.put("channel_detail", channelDetail);

            channelClient.create(members, extraData).execute();

            // let's change the source of this channel
            Map<String, Object> setField = Collections.singletonMap("source", "system");
            channelClient.updatePartial(setField, emptyList()).execute();

            // since it's system generated we no longer need source_detail
            List<String> unsetField = Collections.singletonList("source_detail");
            channelClient.updatePartial(emptyMap(), unsetField).execute();

            // and finally update one of the nested fields in the channel_detail
            Map<String, Object> setNestedField = Collections.singletonMap("channel_detail.topic", "Nature");
            channelClient.updatePartial(setNestedField, emptyList()).execute();

            // and maybe we decide we no longer need a rating
            List<String> unsetNestedField = Collections.singletonList("channel_detail.rating");
            channelClient.updatePartial(emptyMap(), unsetNestedField).execute();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_update/?language=java#full-update-(overwrite)">Full Update (overwrite)</a>
         */
        public void fullUpdate() {
            ChannelClient channelClient = client.channel("messaging", "general");

            Map<String, Object> channelData = new HashMap<>();
            channelData.put("name", "myspecialchannel");
            channelData.put("color", "green");
            Message updateMessage = new Message.Builder()
                    .withText("Thierry changed the channel color to green")
                    .build();
            channelClient.update(updateMessage, channelData).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_members/?language=java">Updating Channel Members</a>
     */
    class UpdatingChannelMembers {

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_members/?language=java#adding-removing-channel-members">Adding & Removing Channel Members</a>
         */
        public void addingAndRemovingChannelMembers() {
            ChannelClient channelClient = client.channel("messaging", "general");

            // Add members with ids "thierry" and "josh"
            channelClient.addMembers(Arrays.asList("thierry", "josh"), null, false, null, false).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            // Remove member with id "tommaso"
            channelClient.removeMembers(Collections.singletonList("tommaso"), null, false).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/channel_members/?language=java#message-parameter">Message Parameter</a>
         */
        public void messageParameter() {
            ChannelClient channelClient = client.channel("messaging", "general");

            Message addMemberSystemMessage = new Message.Builder()
                    .withText("Thierry and Josh were added to this channel")
                    .build();
            // Add members with ids "thierry" and "josh"
            channelClient.addMembers(Arrays.asList("thierry", "josh"), addMemberSystemMessage, false, null, false).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            Message removeMemberSystemMessage = new Message.Builder()
                    .withText("Tommaso was removed from this channel")
                    .build();
            // Remove member with id "tommaso"
            channelClient.removeMembers(Collections.singletonList("tommaso"), removeMemberSystemMessage, false).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java">Querying Channels</a>
     */
    class QueryingChannels {
        public void queryChannels() {
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", Collections.singletonList("thierry"))
            );
            int offset = 0;
            int limit = 10;
            QuerySortByField<Channel> sort = QuerySortByField.descByName("lastMessageAt");
            int messageLimit = 0;
            int memberLimit = 0;

            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit)
                    .withWatch()
                    .withState();

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java#common-filters-by-use-case">Common Filters</a>
         */
        class CommonFilters {
            public void channelsThatContainsSpecificUser() {
                FilterObject filter = Filters.in("members", Collections.singletonList("thierry"));
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
            QuerySorter<Channel> sort = new QuerySortByField<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            // Get the second 10 channels
            int nextOffset = 10; // Skips first 10
            QueryChannelsRequest nextRequest = new QueryChannelsRequest(filter, nextOffset, limit, sort, messageLimit, memberLimit);
            client.queryChannels(nextRequest).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_members/?language=java">Querying Members</a>
     */
    class QueryingMembers {

        public void paginationAndOrdering() {
            ChannelClient channelClient = client.channel("messaging", "general");
            int offset = 0;
            int limit = 10;
            FilterObject filterByName = Filters.neutral();

            // paginate by user_id in descending order
            QuerySorter<Member> sort = QuerySortByField.descByName("userId");
            channelClient.queryMembers(offset, limit, filterByName, sort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.getOrNull();
                } else {
                    // Handle error
                }
            });

            // paginate by created at in ascending order
            QuerySorter<Member> createdAtSort = QuerySortByField.ascByName("createdAt");
            channelClient.queryMembers(offset, limit, filterByName, createdAtSort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        public void queryingMembers() {
            ChannelClient channelClient = client.channel("messaging", "general");

            int offset = 0; // Use this value for pagination
            int limit = 10;
            QuerySortByField<Member> sort = new QuerySortByField<>();

            // Channel members can be queried with various filters
            // 1. Create the filter, e.g query members by user name
            FilterObject filterByName = Filters.eq("name", "tommaso");
            // 2. Call queryMembers with that filter
            channelClient.queryMembers(offset, limit, filterByName, sort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.getOrNull();
                } else {
                    // Handle error
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
            FilterObject filterByNone = NeutralFilterObject.INSTANCE;

            // We can order the results too with QuerySortByField param
            // Here example to order results by member created at descending
            QuerySortByField<Member> createdAtDescendingSort = QuerySortByField.descByName("createdAt");
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
                    List<Message> messages = result.getOrNull().getMessages();
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
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_capabilities/?language=java">Capabilities</a>
     */
    class Capabilities {
        public void frontendCapabilities() {
            ChannelClient channelClient = client.channel("messaging", "general");

            channelClient.query(new QueryChannelRequest()).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();

                    Set<String> capabilities = channel.getOwnCapabilities();
                    boolean userCanDeleteOwnMessage = capabilities.contains(ChannelCapabilities.DELETE_OWN_MESSAGE);
                    boolean userCanUpdateAnyMessage = capabilities.contains(ChannelCapabilities.UPDATE_ANY_MESSAGE);
                } else {
                    // Handle error
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

            List<String> memberIds = Arrays.asList("thierry", "tommaso");
            Map<String, Object> data = new HashMap<>();
            data.put("invites", Arrays.asList("nick"));

            channelClient.create(memberIds, data).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#inviting-users">Iniviting Users</a>
         */
        public void invitingUsersToExistingChannel() {
            ChannelClient channelClient = client.channel("messaging", "general");

            List<String> memberIds = Arrays.asList("nick");

            channelClient.inviteMembers(memberIds, null, false).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
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
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
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
                    // Handle error
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
            QuerySorter<Channel> sort = new QuerySortByField<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channel = result.getOrNull();
                } else {
                    // Handle error
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
            QuerySorter<Channel> sort = new QuerySortByField<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#query-for-pending-invites">Query For Pending Invites</a>
         */
        public void queryForPendingInvites() {
            FilterObject filter = Filters.eq("invite", "pending");
            int offset = 0;
            int limit = 10;
            QuerySorter<Channel> sort = new QuerySortByField<>();
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

    class MutingOrHidingChannels {

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java">Muting Channels</a>
         */
        class MutingChannels {

            /**
             * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#channel-mute">Channel Mute</a>
             */
            public void channelMute() {
                // Mute a channel
                ChannelClient channelClient = client.channel("messaging", "general");
                channelClient.mute().enqueue(result -> {
                    if (result.isSuccess()) {
                        // Channel is muted
                    } else {
                        // Handle error
                    }
                });

                // Get list of muted channels when user is connected
                User user = new User.Builder()
                        .withId("user-id")
                        .build();
                client.connectUser(user, "token").enqueue(result -> {
                    if (result.isSuccess()) {
                        // Result contains the list of channel mutes
                        List<ChannelMute> mutes = result.getOrNull().getUser().getChannelMutes();
                    } else {
                        // Handle error
                    }
                });

                // Get updates about muted channels
                client.subscribeFor(
                        new Class[]{NotificationChannelMutesUpdatedEvent.class},
                        channelsMuteEvent -> {
                            List<ChannelMute> mutes = ((NotificationChannelMutesUpdatedEvent) channelsMuteEvent).getMe().getChannelMutes();
                        }
                );
            }

            /**
             * @see <a href="https://getstream.io/chat/docs/android/muting_channels/?language=java#check-if-user-is-muted">Check if User is Muted</a>
             */
            public void checkIfUserIsMuted(Channel channel, User user) {
                boolean isMuted = ChannelExtensionKt.isMutedFor(channel, user);
                if (isMuted) {
                    // Handle UI for muted channel
                } else {
                    // Handle UI for not muted channel
                }
            }

            /**
             * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#query-muted-channels">Query Muted Channels</a>
             */
            public void queryMutedChannels(String currentUserId, FilterObject filter) {
                // Filter for all channels excluding muted ones
                FilterObject notMutedFilter = Filters.and(
                        Filters.eq("muted", false),
                        Filters.in("members", Collections.singletonList(currentUserId))
                );

                // Filter for muted channels
                FilterObject mutedFilter = Filters.eq("muted", true);

                // Executing a channels query with either of the filters
                int offset = 0;
                int limit = 10;
                QuerySorter<Channel> sort = new QuerySortByField<>();
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
                        List<Channel> channels = result.getOrNull();
                    } else {
                        // Handle error
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
                        // Handle error
                    }
                });
            }
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
                    // Handle error
                }
            });

            // Shows a previously hidden channel
            channelClient.show().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is shown
                } else {
                    // Handle error
                }
            });

            // Hide the channel and clear the message history
            channelClient.hide(true).enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is hidden
                } else {
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/disabling_channels/?language=java">Disabling Channels</a>
     */
    class DisablingChannels {

        public void freeze() {
            ChannelClient channelClient = client.channel("messaging", "general");
            Map<String, Object> set = new HashMap<>();
            set.put("freeze", true);
            List<String> unset = new ArrayList<>();

            channelClient.updatePartial(set, unset).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        public void unfreeze() {
            ChannelClient channelClient = client.channel("messaging", "general");
            Map<String, Object> set = new HashMap<>();
            List<String> unset = new ArrayList<>();
            unset.add("freeze");

            channelClient.updatePartial(set, unset).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=java">Deleting Channels</a>
     */
    class DeletingChannels {

        public void deletingAChannel() {
            ChannelClient channelClient = client.channel("messaging", "general");

            channelClient.delete().enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/truncate_channel/?language=java">Truncate a Channel</a>
     */
    class TruncateChannel {

        public void truncateAChannel() {
            // Removes all of the messages of the channel but doesn't affect the channel data or members
            channelClient.truncate().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is truncated
                } else {
                    // Handle error
                }
            });
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
                    Channel channel = channelResult.getOrNull();
                    int cooldown = channel.getCooldown();

                    Message message = new Message.Builder()
                            .withText("Hello")
                            .build();
                    channelClient.sendMessage(message).enqueue((messageResult) -> {
                        // After sending a message, block the UI temporarily
                        // The disable/enable UI methods have to be implemented by you
                        disableMessageSendingUi();

                        new Handler(Looper.getMainLooper())
                                .postDelayed(() -> enableMessageSendingUi(), cooldown);
                    });
                } else {
                    // Handle error
                }
            });
        }
    }
}
