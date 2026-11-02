/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.infrastructure

import com.squareup.moshi.Moshi

internal object Serializer {
    @JvmStatic
    internal val moshiBuilder: Moshi.Builder = Moshi.Builder()
        .add(io.getstream.chat.android.network.models.AutomodMode.AutomodModeAdapter())
        .add(io.getstream.chat.android.network.models.BlocklistBehavior.BlocklistBehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ChannelModerationBehavior.ChannelModerationBehaviorAdapter())
        .add(io.getstream.chat.android.network.models.ChannelOwnCapability.ChannelOwnCapabilityAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.ChannelMentions.ChannelMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.DefaultPreference.DefaultPreferenceAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.DirectMentions.DirectMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.GroupMentions.GroupMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.HereMentions.HereMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.RoleMentions.RoleMentionsAdapter())
        .add(io.getstream.chat.android.network.models.ChatPreferencesInput.ThreadReplies.ThreadRepliesAdapter())
        .add(io.getstream.chat.android.network.models.CreatePollRequest.VotingVisibility.VotingVisibilityAdapter())
        .add(io.getstream.chat.android.network.models.MessageRequest.Type.TypeAdapter())
        .add(io.getstream.chat.android.network.models.PushLevel.PushLevelAdapter())
        .add(io.getstream.chat.android.network.models.PushPreferenceInput.ChatLevel.ChatLevelAdapter())
        .add(io.getstream.chat.android.network.models.PushProviderName.PushProviderNameAdapter())
        .add(io.getstream.chat.android.network.models.ScheduledMessageFailureReason.ScheduledMessageFailureReasonAdapter())
        .add(io.getstream.chat.android.network.models.ScheduledMessageQueryStatus.ScheduledMessageQueryStatusAdapter())
        .add(io.getstream.chat.android.network.models.ScheduledMessageStatus.ScheduledMessageStatusAdapter())
        .add(io.getstream.chat.android.network.models.TranslateMessageRequest.Language.LanguageAdapter())
        .add(io.getstream.chat.android.network.models.UpdatePollRequest.VotingVisibility.VotingVisibilityAdapter())
        .add(io.getstream.chat.android.network.infrastructure.IsoDateAdapter())
        .add(io.getstream.chat.android.network.infrastructure.ExactDateAdapter())
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())

    @JvmStatic
    internal val moshi: Moshi by lazy {
        moshiBuilder.build()
    }
}
