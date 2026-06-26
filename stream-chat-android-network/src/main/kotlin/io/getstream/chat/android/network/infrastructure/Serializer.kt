/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.getstream.chat.android.network.infrastructure

import com.squareup.moshi.Moshi

object Serializer {
    @JvmStatic
    val moshiBuilder: Moshi.Builder = Moshi.Builder()
        .add(io.getstream.chat.android.network.models.BlockListOptions.Behavior.BehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ChannelConfigOverrides.BlocklistBehavior.BlocklistBehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ChannelConfigOverrides.PushLevel.PushLevelAdapter())
        .add(io.getstream.chat.android.network.models.ChannelConfigWithInfo.Automod.AutomodAdapter())
        .add(io.getstream.chat.android.network.models.ChannelConfigWithInfo.AutomodBehavior.AutomodBehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ChannelConfigWithInfo.BlocklistBehavior.BlocklistBehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ChannelConfigWithInfo.PushLevel.PushLevelAdapter())
        .add(io.getstream.chat.android.network.models.ChannelOwnCapability.ChannelOwnCapabilityAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.ChannelMentions.ChannelMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.DefaultPreference.DefaultPreferenceAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.DirectMentions.DirectMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.GroupMentions.GroupMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.HereMentions.HereMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.RoleMentions.RoleMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.ThreadReplies.ThreadRepliesAdapter())
        .add(io.getstream.chat.android.network.models.ConfigOverridesRequest.BlocklistBehavior.BlocklistBehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ConfigOverridesRequest.PushLevel.PushLevelAdapter())
        .add(io.getstream.chat.android.network.models.CreateBlockListRequest.Type.TypeAdapter())
        .add(io.getstream.chat.android.network.models.CreateDeviceRequest.PushProvider.PushProviderAdapter())
        .add(io.getstream.chat.android.network.models.CreatePollRequest.VotingVisibility.VotingVisibilityAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.Comment.CommentAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.CommentMention.CommentMentionAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.CommentReaction.CommentReactionAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.CommentReply.CommentReplyAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.Follow.FollowAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.Mention.MentionAdapter())
        .add(io.getstream.chat.android.network.models.FeedsPreferences.Reaction.ReactionAdapter())
        .add(io.getstream.chat.android.network.models.MessageRequest.Type.TypeAdapter())
        .add(io.getstream.chat.android.network.models.PushPreferenceInput.CallLevel.CallLevelAdapter())
        .add(io.getstream.chat.android.network.models.PushPreferenceInput.ChatLevel.ChatLevelAdapter())
        .add(io.getstream.chat.android.network.models.PushPreferenceInput.FeedsLevel.FeedsLevelAdapter())
        .add(io.getstream.chat.android.network.models.TranslateMessageRequest.Language.LanguageAdapter())
        .add(io.getstream.chat.android.network.models.UpdatePollRequest.VotingVisibility.VotingVisibilityAdapter())
        .add(io.getstream.chat.android.network.infrastructure.BigDecimalAdapter())
        .add(io.getstream.chat.android.network.infrastructure.BigIntegerAdapter())
        .add(io.getstream.chat.android.network.infrastructure.ByteArrayAdapter())
        .add(io.getstream.chat.android.network.infrastructure.URIAdapter())
        .add(io.getstream.chat.android.network.infrastructure.UUIDAdapter())
        .add(io.getstream.chat.android.network.infrastructure.IsoDateAdapter())
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
    
    @JvmStatic
    val moshi: Moshi by lazy {
        moshiBuilder.build()
    }
}