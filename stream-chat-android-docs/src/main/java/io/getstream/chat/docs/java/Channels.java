package io.getstream.chat.docs.java;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.Pagination;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.ChannelsMuteEvent;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelMute;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Member;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.socket.InitConnectionListener;
import io.getstream.chat.android.client.utils.FilterObject;
import kotlin.Unit;

import static io.getstream.chat.docs.StaticInstances.TAG;
import static java.util.Collections.emptyList;

public class Channels {
    private ChatClient client;
    private ChannelClient channelController;

    /**
     * @see <a href="https://getstream.io/chat/docs/initialize_channel/?language=java">Channel Initialization</a>
     */
    class ChannelInitialization {
        public void initialization() {
            // Create channel controller using channel type and channel id
            ChannelClient channelController = client.channel("channel-type", "channel-id");

            // Or create channel controller using channel cid
            ChannelClient anotherChannelController = client.channel("cid");
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/creating_channels/?language=java">Creating Channels</a>
     */
    class CreatingChannels {
        public void createAChannel() {
            String channelType = "messaging";
            String channelId = "id";
            Map<String, Object> extraData = new HashMap<>();
            client.createChannel(channelType, channelId, extraData).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel newChannel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=java">Watching A Channel</a>
     */
    class WatchingAChannel {
        public void watchingChannel() {
            channelController.watch().enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/watch_channel/?language=java#unwatching">Unwacthing</a>
         */
        public void stopWatchingChannel() {
            channelController.stopWatching().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel unwatched
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java">Querying Channels</a>
     */
    class QueryingChannels {
        public void queryChannels() {
            FilterObject filter = Filters.in("members", "thierry").put("type", "messaging");
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<Channel>().desc("last_message_at");
            int messageLimit = 0;
            int memberLimit = 0;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);
            request.setWatch(true);
            request.setState(true);

            client.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_channels/?language=java#common-filters-by-use-case">Common Filters</a>
         */
        class CommonFilters {
            public void channelsThatContainsSpecificUser() {
                FilterObject filter = Filters
                        .in("members", "thierry")
                        .put("type", "messaging");
            }

            public void channelsThatWithSpecificStatus() {
                FilterObject filter = Filters
                        .in("status", "pending", "open", "new")
                        .put("agent_id", "user-id");
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
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Get the second 10 channels
            int nextOffset = 10;
            QueryChannelsRequest nextRequest = new QueryChannelsRequest(filter, nextOffset, limit, sort, messageLimit, memberLimit);
            client.queryChannels(nextRequest).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_pagination/?language=java">Channel Pagination</a>
     */
    class ChannelPagination {

        private int pageSize = 10;

        // Get the first 10 messages
        public void loadFirstPage() {
            QueryChannelRequest firstPage = new QueryChannelRequest().withMessages(pageSize);
            client.queryChannel("channel-type", "channel-id", firstPage).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.data().getMessages();
                    if (messages.size() < pageSize) {
                        // All messages loaded
                    } else {
                        loadSecondPage(messages.get(messages.size() - 1).getId());
                    }
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().getCause()));
                }
                return Unit.INSTANCE;
            });
        }

        // Get the second 10 messages
        public void loadSecondPage(String lastMessageId) {
            QueryChannelRequest secondPage = new QueryChannelRequest().withMessages(Pagination.LESS_THAN, lastMessageId, pageSize);
            client.queryChannel("channel-type", "channel-id", secondPage).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Message> messages = result.data().getMessages();
                    if (messages.size() < pageSize) {
                        // All messages loaded
                    } else {
                        // Load another page
                    }
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().getCause()));
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_update/?language=java">Updating a Channel</a>
     */
    class UpdatingAChannel {
        public void updateChannel() {
            Map<String, Object> channelData = new HashMap<>();
            channelData.put("color", "green");
            Message updateMessage = new Message();
            updateMessage.setText("Thierry changed the channel color to green");
            channelController.update(updateMessage, channelData).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
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
            // Add member with id "thierry" and "josh"
            channelController.addMembers("thierry", "josh").enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Remove member with id "thierry" and "josh"
            channelController.removeMembers("thierry", "josh").enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
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
            List<String> members = Arrays.asList("thierry", "tomasso");
            String channelType = "messaging";
            client.createChannel(channelType, members).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
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
            List<String> members = Arrays.asList("thierry", "tommaso");
            List<String> invites = Arrays.asList("nick");
            Map<String, Object> data = new HashMap<>();
            data.put("members", members);
            data.put("invites", invites);

            channelController.create(data).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#accepting-an-invite">Accept an Invite</a>
         */
        public void acceptingAnInvite() {
            channelController.acceptInvite("Nick joined this channel!").enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#rejecting-an-invite">Rejecting an Invite</a>
         */
        public void rejectingAnInvite() {
            channelController.rejectInvite().enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#query-for-accepted-invites">Query For Accepted Invites</a>
         */
        public void queryForAcceptedInvites() {
            FilterObject filter = new FilterObject("invite", "accepted");
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
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_invites/?language=java#query-for-rejected-invites">Query For Rejected Invites</a>
         */
        public void queryForRejectedInvites() {
            FilterObject filter = new FilterObject("invite", "rejected");
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
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=java">Deleting & Hiding a Channel</a>
     */
    class DeletingAndHidingAChannel {

        public void deletingAChannel() {
            channelController.delete().enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=java#hiding-a-channel">Hiding a Channel</a>
         */
        public void hidingAChannel() {
            // Hides the channel until a new message is added there
            channelController.hide(false).enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is hidden
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Shows a previously hidden channel
            channelController.show().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is shown
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Hide the channel and clear the message history
            channelController.hide(true).enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is hidden
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
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
        public void channelMute() {
            client.muteChannel("channel-type", "channel-id").enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is muted
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Get list of muted channels when user is connected
            User user = new User();
            user.setId("user-id");
            client.setUser(user, "token", new InitConnectionListener() {
                @Override
                public void onSuccess(@NotNull ConnectionData data) {
                    // Mutes contains the list of channel mutes
                    List<ChannelMute> mutes = data.getUser().getChannelMutes();
                }
            });

            // Get updates about muted channels
            client.subscribeFor(
                    new Class[]{ChannelsMuteEvent.class},
                    channelsMuteEvent -> {
                        List<ChannelMute> mutes = ((ChannelsMuteEvent) channelsMuteEvent).getChannelsMute();
                        return Unit.INSTANCE;
                    }
            );
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#query-muted-channels">Query Muted Channels</a>
         */
        public void queryMutedChannels() {
            // Retrieve channels excluding muted ones
            FilterObject notMutedFilter = Filters.eq("muted", false);
            int offset = 0;
            int limit = 10;
            QuerySort<Channel> sort = new QuerySort<>();
            int messageLimit = 0;
            int memberLimit = 0;
            client.queryChannels(new QueryChannelsRequest(notMutedFilter, offset, limit, sort, messageLimit, memberLimit)).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Retrieve muted channels
            FilterObject mutedFilter = Filters.eq("muted", true);
            client.queryChannels(new QueryChannelsRequest(mutedFilter, offset, limit, sort, messageLimit, memberLimit)).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Channel> channels = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/muting_channels/?language=java#remove-a-channel-mute">Remove a Channel Mute</a>
         */
        public void removeAChannelMute() {
            // Unmute channel for current user
            channelController.unmute().enqueue(result -> {
                if (result.isSuccess()) {
                    // Channel is unmuted
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_members/?language=java">Query Members</a>
     */
    class QueryMembers {
        public void queryingMembers() {
            int offset = 0;
            int limit = 10;
            QuerySort<Member> sort = new QuerySort<>();

            // We can query channel members with specific filters
            // 1. Create the filters query, e.g query members by user name
            FilterObject filterByName = Filters.eq("name", "tommaso");

            // 2. Call queryMembers with that filter
            channelController.queryMembers(offset, limit, filterByName, sort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });

            // Here are some commons filters you can use:
            // Autocomplete members by user name
            FilterObject filterByAutoCompleteName = Filters.autocomplete("name", "tommaso");

            // Query member by id
            FilterObject filterById = Filters.eq("id", "tommaso");

            // Query multiple members by id
            FilterObject filterByIds = Filters.in("id", "tommaso", "thierry");

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
            channelController.queryMembers(offset, limit, new FilterObject(), createdAtDescendingSort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
                return Unit.INSTANCE;
            });
        }
    }
}
