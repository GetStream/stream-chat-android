package io.getstream.chat.android.compose.state.channel.list

import io.getstream.chat.android.client.models.Channel

/**
 * Represents the list of actions users can take with selected channels.
 *
 * @param channel The selected channel.
 */
public sealed class ChannelAction(public val channel: Channel)

/**
 * Show more info about the channel.
 */
public class ViewInfo(channel: Channel) : ChannelAction(channel)

/**
 * Shows a dialog to leave the group.
 */
public class LeaveGroup(channel: Channel) : ChannelAction(channel)

/**
 * Mutes the channel.
 */
public class MuteChannel(channel: Channel) : ChannelAction(channel)

/**
 * Unmutes the channel.
 */
public class UnmuteChannel(channel: Channel) : ChannelAction(channel)

/**
 * Shows a dialog to delete the conversation, if we have the permission.
 */
public class DeleteConversation(channel: Channel) : ChannelAction(channel)

/**
 * Dismisses the actions.
 */
public object Cancel : ChannelAction(Channel())
