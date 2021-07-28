package io.getstream.chat.android.compose.state.channel.list

import io.getstream.chat.android.client.models.Channel

/**
 * Represents the list of actions users can take with selected channels.
 *
 * @param channel - The selected channel.
 *
 * [ViewInfo] - Show more info about the channel.
 * [LeaveGroup] - Shows a dialog to leave the group.
 * [DeleteConversation] - Shows a dialog to delete the conversation, if we have the permission.
 * [Cancel] - Dismisses the actions.
 * */
public sealed class ChannelListAction(public val channel: Channel)

public class ViewInfo(channel: Channel) : ChannelListAction(channel)

public class LeaveGroup(channel: Channel) : ChannelListAction(channel)

public class DeleteConversation(channel: Channel) : ChannelListAction(channel)

public object Cancel : ChannelListAction(Channel())
