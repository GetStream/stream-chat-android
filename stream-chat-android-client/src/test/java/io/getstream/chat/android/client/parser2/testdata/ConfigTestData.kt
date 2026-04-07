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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ConfigTestData {

    // All required fields present, plus optional fields populated.
    private const val REQUIRED_FIELDS = """
        "typing_events":true,
        "read_events":true,
        "connect_events":true,
        "search":false,
        "reactions":true,
        "replies":true,
        "mutes":true,
        "uploads":true,
        "url_enrichment":true,
        "custom_events":false,
        "push_notifications":true,
        "polls":false,
        "message_retention":"30",
        "max_message_length":5000,
        "automod":"disabled",
        "automod_behavior":"flag",
        "commands":[{"name":"giphy","description":"Gif","args":"[text]","set":"fun"}],
        "mark_messages_pending":false
    """

    @Language("JSON")
    val jsonAllFields =
        """{$REQUIRED_FIELDS,"created_at":"2020-06-29T06:14:28.000Z","updated_at":"2020-06-30T06:14:28.000Z","name":"messaging","delivery_events":false,"skip_last_msg_update_for_system_msgs":true,"blocklist_behavior":"block","user_message_reminders":true,"shared_locations":true}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{$REQUIRED_FIELDS}"""

    // Error cases — one per field type to cover Boolean, String, Int, List
    @Language("JSON")
    val jsonMissingTypingEvents =
        """{"read_events":true,"connect_events":true,"search":false,"reactions":true,"replies":true,"mutes":true,"uploads":true,"url_enrichment":true,"custom_events":false,"push_notifications":true,"polls":false,"message_retention":"30","max_message_length":5000,"automod":"disabled","automod_behavior":"flag","commands":[],"mark_messages_pending":false}"""

    @Language("JSON")
    val jsonMissingMessageRetention =
        """{"typing_events":true,"read_events":true,"connect_events":true,"search":false,"reactions":true,"replies":true,"mutes":true,"uploads":true,"url_enrichment":true,"custom_events":false,"push_notifications":true,"polls":false,"max_message_length":5000,"automod":"disabled","automod_behavior":"flag","commands":[],"mark_messages_pending":false}"""

    @Language("JSON")
    val jsonMissingMaxMessageLength =
        """{"typing_events":true,"read_events":true,"connect_events":true,"search":false,"reactions":true,"replies":true,"mutes":true,"uploads":true,"url_enrichment":true,"custom_events":false,"push_notifications":true,"polls":false,"message_retention":"30","automod":"disabled","automod_behavior":"flag","commands":[],"mark_messages_pending":false}"""

    @Language("JSON")
    val jsonMissingCommands =
        """{"typing_events":true,"read_events":true,"connect_events":true,"search":false,"reactions":true,"replies":true,"mutes":true,"uploads":true,"url_enrichment":true,"custom_events":false,"push_notifications":true,"polls":false,"message_retention":"30","max_message_length":5000,"automod":"disabled","automod_behavior":"flag","mark_messages_pending":false}"""

    val expectedAllFields = Config(
        createdAt = Date(1593411268000),
        updatedAt = Date(1593497668000),
        name = "messaging",
        typingEventsEnabled = true,
        readEventsEnabled = true,
        deliveryEventsEnabled = false,
        connectEventsEnabled = true,
        searchEnabled = false,
        isReactionsEnabled = true,
        isThreadEnabled = true,
        muteEnabled = true,
        uploadsEnabled = true,
        urlEnrichmentEnabled = true,
        customEventsEnabled = false,
        pushNotificationsEnabled = true,
        skipLastMsgUpdateForSystemMsgs = true,
        pollsEnabled = false,
        messageRetention = "30",
        maxMessageLength = 5000,
        automod = "disabled",
        automodBehavior = "flag",
        blocklistBehavior = "block",
        commands = listOf(Command(name = "giphy", description = "Gif", args = "[text]", set = "fun")),
        messageRemindersEnabled = true,
        sharedLocationsEnabled = true,
        markMessagesPending = false,
    )

    val expectedOptionalFieldsMissing = Config(
        createdAt = null,
        updatedAt = null,
        name = "",
        typingEventsEnabled = true,
        readEventsEnabled = true,
        deliveryEventsEnabled = true,
        connectEventsEnabled = true,
        searchEnabled = false,
        isReactionsEnabled = true,
        isThreadEnabled = true,
        muteEnabled = true,
        uploadsEnabled = true,
        urlEnrichmentEnabled = true,
        customEventsEnabled = false,
        pushNotificationsEnabled = true,
        skipLastMsgUpdateForSystemMsgs = false,
        pollsEnabled = false,
        messageRetention = "30",
        maxMessageLength = 5000,
        automod = "disabled",
        automodBehavior = "flag",
        blocklistBehavior = "",
        commands = listOf(Command(name = "giphy", description = "Gif", args = "[text]", set = "fun")),
        messageRemindersEnabled = false,
        sharedLocationsEnabled = false,
        markMessagesPending = false,
    )
}
