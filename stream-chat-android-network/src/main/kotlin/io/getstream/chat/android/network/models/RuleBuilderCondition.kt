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

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * 
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class RuleBuilderCondition (
    @Json(name = "confidence")
    val confidence: kotlin.Float? = null,

    @Json(name = "type")
    val type: kotlin.String? = null,

    @Json(name = "call_custom_property_params")
    val callCustomPropertyParams: io.getstream.chat.android.network.models.CallCustomPropertyParameters? = null,

    @Json(name = "call_type_rule_params")
    val callTypeRuleParams: io.getstream.chat.android.network.models.CallTypeRuleParameters? = null,

    @Json(name = "call_violation_count_params")
    val callViolationCountParams: io.getstream.chat.android.network.models.CallViolationCountParameters? = null,

    @Json(name = "channel_message_count_rule_params")
    val channelMessageCountRuleParams: io.getstream.chat.android.network.models.ChannelMessageCountRuleParameters? = null,

    @Json(name = "closed_caption_rule_params")
    val closedCaptionRuleParams: io.getstream.chat.android.network.models.ClosedCaptionRuleParameters? = null,

    @Json(name = "content_count_rule_params")
    val contentCountRuleParams: io.getstream.chat.android.network.models.ContentCountRuleParameters? = null,

    @Json(name = "content_flag_count_rule_params")
    val contentFlagCountRuleParams: io.getstream.chat.android.network.models.FlagCountRuleParameters? = null,

    @Json(name = "image_content_params")
    val imageContentParams: io.getstream.chat.android.network.models.ImageContentParameters? = null,

    @Json(name = "image_rule_params")
    val imageRuleParams: io.getstream.chat.android.network.models.ImageRuleParameters? = null,

    @Json(name = "keyframe_ocr_rule_params")
    val keyframeOcrRuleParams: io.getstream.chat.android.network.models.KeyframeOCRRuleParameters? = null,

    @Json(name = "keyframe_rule_params")
    val keyframeRuleParams: io.getstream.chat.android.network.models.KeyframeRuleParameters? = null,

    @Json(name = "text_content_params")
    val textContentParams: io.getstream.chat.android.network.models.TextContentParameters? = null,

    @Json(name = "text_rule_params")
    val textRuleParams: io.getstream.chat.android.network.models.TextRuleParameters? = null,

    @Json(name = "user_created_within_params")
    val userCreatedWithinParams: io.getstream.chat.android.network.models.UserCreatedWithinParameters? = null,

    @Json(name = "user_custom_property_params")
    val userCustomPropertyParams: io.getstream.chat.android.network.models.UserCustomPropertyParameters? = null,

    @Json(name = "user_flag_count_rule_params")
    val userFlagCountRuleParams: io.getstream.chat.android.network.models.FlagCountRuleParameters? = null,

    @Json(name = "user_identical_content_count_params")
    val userIdenticalContentCountParams: io.getstream.chat.android.network.models.UserIdenticalContentCountParameters? = null,

    @Json(name = "user_role_params")
    val userRoleParams: io.getstream.chat.android.network.models.UserRoleParameters? = null,

    @Json(name = "user_rule_params")
    val userRuleParams: io.getstream.chat.android.network.models.UserRuleParameters? = null,

    @Json(name = "video_content_params")
    val videoContentParams: io.getstream.chat.android.network.models.VideoContentParameters? = null,

    @Json(name = "video_rule_params")
    val videoRuleParams: io.getstream.chat.android.network.models.VideoRuleParameters? = null
)
