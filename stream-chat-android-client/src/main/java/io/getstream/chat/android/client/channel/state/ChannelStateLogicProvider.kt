package io.getstream.chat.android.client.channel.state

public interface ChannelStateLogicProvider {

    public fun stateLogic(channelType: String, channelId: String): ChannelStateLogic
}
