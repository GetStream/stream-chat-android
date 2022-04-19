package io.getstream.chat.android.client.persistance.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import java.util.Date

public interface ChannelRepository {
    public suspend fun insertChannel(channel: Channel)
    public suspend fun insertChannels(channels: Collection<Channel>)
    public suspend fun deleteChannel(cid: String)
    public suspend fun selectChannelWithoutMessages(cid: String): Channel?

    /**
     * Selects all channels' cids.
     *
     * @return A list of channels' cids stored in the repository.
     */
    public suspend fun selectAllCids(): List<String>

    /**
     * Select channels by full channel IDs [Channel.cid]
     *
     * @param channelCIDs A list of [Channel.cid] as query specification.
     * @param forceCache A boolean flag that forces cache in repository and fetches data directly in database if passed
     * value is true.
     *
     * @return A list of channels found in repository.
     */
    public suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean = false): List<Channel>
    public suspend fun selectChannelsSyncNeeded(): List<Channel>
    public suspend fun setChannelDeletedAt(cid: String, deletedAt: Date)
    public suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date)
    public suspend fun setHiddenForChannel(cid: String, hidden: Boolean)
    public suspend fun selectMembersForChannel(cid: String): List<Member>
    public suspend fun updateMembersForChannel(cid: String, members: List<Member>)
    public suspend fun evictChannel(cid: String)

    @VisibleForTesting
    public fun clearChannelCache()
}
