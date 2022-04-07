package io.getstream.chat.android.client.channel.manager

public object ChannelsManagerProvider {

    private var channelsManager: ChannelsManager? = null

    public fun setChannelsManager(channelsManager: ChannelsManager) {
        this.channelsManager = channelsManager
    }

    public fun getChannelsManager(): ChannelsManager {
        requireNotNull(channelsManager) {
            "Looks like ChannelsManager wasn't correctly initialized"
        }

        return channelsManager!!
    }
}
