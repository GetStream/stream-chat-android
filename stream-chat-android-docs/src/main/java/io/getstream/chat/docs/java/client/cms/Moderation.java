package io.getstream.chat.docs.java.client.cms;

import java.util.Date;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.models.BannedUser;
import io.getstream.chat.android.client.models.BannedUsersSort;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Flag;
import io.getstream.chat.android.client.models.Mute;
import io.getstream.chat.android.client.models.User;

public class Moderation {
    private ChatClient client;
    private ChannelClient channelClient;

    class ModerationTools {

        /**
         * @see <a href="hhttps://getstream.io/chat/docs/android/moderation/?language=java#flag">Flag</a>
         */
        class Flags {

            public void flag() {
                client.flagMessage("message-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // Message was flagged
                        Flag flag = result.data();
                    } else {
                        // Handle result.error()
                    }
                });

                client.flagUser("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was flagged
                        Flag flag = result.data();
                    } else {
                        // Handle result.error()
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
                        Mute mute = result.data();
                    } else {
                        // Handle result.error()
                    }
                });

                client.unmuteUser("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was unmuted
                    } else {
                        // Handle result.error()
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
                        // Handle result.error()
                    }
                });

                channelClient.unbanUser("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // User was unbanned
                    } else {
                        // Handle result.error()
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
                        List<User> users = result.data();
                    } else {
                        // Handle result.error()
                    }
                });

                // Query for banned members from one channel
                FilterObject channelFilter = Filters.eq("channel_cid", "ChannelType:ChannelId");
                client.queryBannedUsers(channelFilter).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<BannedUser> bannedUsers = result.data();
                    } else {
                        // Handle result.error()
                    }
                });
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/react/moderation/?language=java#query-bans-endpoint">Query bans endpoint</a>
         */
        class QueryBansEndpoint {

            public void queryBans() {
                // Get the bans for channel livestream:123 in descending order
                FilterObject filter = Filters.eq("channel_cid", "livestream:123");
                QuerySort<BannedUsersSort> sort = new QuerySort<BannedUsersSort>().desc("created_at");
                client.queryBannedUsers(filter, sort).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<BannedUser> bannedUsers = result.data();
                    } else {
                        // Handle result.error()
                    }
                });

                // Get the page of bans which where created before or equal date for the same channel
                client.queryBannedUsers(filter, sort, null, null, null, null, null, new Date()).enqueue(result -> {
                    if (result.isSuccess()) {
                        List<BannedUser> bannedUsers = result.data();
                    } else {
                        // Handle result.error()
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
                        // Handle result.error()
                    }
                });

                channelClient.removeShadowBan("user-id").enqueue(result -> {
                    if (result.isSuccess()) {
                        // Shadow ban was removed
                    } else {
                        // Handle result.error()
                    }
                });
            }
        }
    }
}
