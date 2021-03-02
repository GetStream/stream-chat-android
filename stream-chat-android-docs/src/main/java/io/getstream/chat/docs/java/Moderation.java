package io.getstream.chat.docs.java;

import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Flag;
import io.getstream.chat.android.client.models.Member;
import io.getstream.chat.android.client.models.Mute;
import io.getstream.chat.android.client.models.User;

import static java.util.Collections.emptyList;

public class Moderation {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="hhttps://getstream.io/chat/docs/android/moderation/?language=java#flag">Flag</a>
     */
    class Flags {

        public void flagMessage() {
            client.flagMessage("message-id").enqueue(result -> {
                if (result.isSuccess()) {
                    // Message was flagged
                    Flag flag = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void flagUser() {
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

        public void muteUser() {
            client.muteUser("user-id").enqueue(result -> {
                if (result.isSuccess()) {
                    // User was muted
                    Mute mute = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void unmuteUser() {
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
        }

        public void unbanUser() {
            channelClient.unBanUser("user-id").enqueue(result -> {
                if (result.isSuccess()) {
                    // User was unbanned
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/moderation/?language=java#list-banned-users">List banned users</a>
     */
    class ListBannedUsers {

        public void queryBannedUsers() {
            FilterObject filter = Filters.eq("banned", true);
            QueryUsersRequest request = new QueryUsersRequest(filter, 0, 10);
            client.queryUsers(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        public void queryBannedMembers() {
            FilterObject filter = Filters.eq("banned", true);
            QuerySort<Member> sort = new QuerySort<>();
            channelClient.queryMembers(0, 10, filter, sort, emptyList()).enqueue(result -> {
                if (result.isSuccess()) {
                    List<Member> members = result.data();
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
        }

        public void removeShadowBan() {
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
