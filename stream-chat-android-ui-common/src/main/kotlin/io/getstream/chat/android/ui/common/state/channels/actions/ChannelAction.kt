/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.common.state.channels.actions

import io.getstream.chat.android.models.Channel

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
