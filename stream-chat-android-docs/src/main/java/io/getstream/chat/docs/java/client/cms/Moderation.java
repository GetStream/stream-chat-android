package io.getstream.chat.docs.java.client.cms;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.models.BannedUser;
import io.getstream.chat.android.models.BannedUsersSort;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.Flag;
import io.getstream.chat.android.models.Mute;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.models.querysort.QuerySorter;

public class Moderation {
    private ChatClient client;
    private ChannelClient channelClient;

    class ModerationTools {

        /**
         * @see <a href="hhttps://getstream.io/chat/docs/android/moderation/?language=java#flag">Flag</a>
         */
        class Flags {

            public void flag() {
                client.flagMessage(
                        "message-id",
                        "This message is inappropriate",
                        Map.of("extra_info", "more details")
                ).enqueue(result -> {
                    if (result.isSuccess()) {
                        // Message was flagged
                        Flag flag = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });

                client.flagUser(
                        "user-id",
                        "This user is a spammer",
                        Map.of("extra_info", "more details")
                ).enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was flagged
                        Flag flag = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=java#mutes">Mutes</a>
         */
        class Mutes {

            public void mutes() {
                client.muteUser("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was muted
                        Mute mute = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });

                client.unmuteUser("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was unmuted
                    } else {
                        // Handle error
                    }
                });
            }
        }


        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=java#ban">Bans</a>
         */
        class Bans {

            public void banUser() {
                // Ban user for 60 minutes from a channel
                channelClient.banUser("user-id", "Bad words", 60).enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was banned
                    } else {
                        // Handle error
                    }
                });

                channelClient.unbanUser("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was unbanned
                    } else {
                        // Handle error
                    }
                });
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=java#query-banned-users">Query banned users</a>
         */
        class QueryBannedUsers {

            public void queryBannedUsers() {
                // Retrieve the list of banned users
                FilterObject filter = Filters.eq("banned", true);
                QueryUsersRequest request = new QueryUsersRequest(filter, 0, 10);
                client.queryUsers(request).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<User> users = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });

                // Query for banned members from one channel
                FilterObject channelFilter = Filters.eq("channel_cid", "ChannelType:ChannelId");
                client.queryBannedUsers(channelFilter).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<BannedUser> bannedUsers = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });
            }

            public void queryBansEndpoint() {
                // Get the bans for channel livestream:123 in descending order
                FilterObject filter = Filters.eq("channel_cid", "livestream:123");
                QuerySorter<BannedUsersSort> sort = QuerySortByField.descByName("createdAt");
                client.queryBannedUsers(filter, sort).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<BannedUser> bannedUsers = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });

                // Get the page of bans which where created before or equal date for the same channel
                client.queryBannedUsers(filter, sort, null, null, null, null, null, new Date()).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<BannedUser> bannedUsers = result.getOrNull();
                    } else {
                        // Handle error
                    }
                });
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=java#shadow-ban">Shadow ban</a>
         */
        class ShadowBan {

            public void shadowBanUser() {
                // Shadow ban user for 60 minutes from a channel
                channelClient.shadowBanUser("user-id", "Bad words", 60).enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was shadow banned
                    } else {
                        // Handle error
                    }
                });

                channelClient.removeShadowBan("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // Shadow ban was removed
                    } else {
                        // Handle error
                    }
                });
            }
        }
    }
}
