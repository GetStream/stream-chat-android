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

package io.getstream.chat.android.compose.pages

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import java.util.regex.Pattern
import io.getstream.chat.android.compose.R as ComposeR
import io.getstream.chat.android.compose.sample.R as SampleR

/**
 * Scheduled messages: the SDK sheets that pick when a message is sent, the SDK banner above the
 * composer, and the SDK scheduled messages screen.
 */
class ScheduledMessagesPage {

    class Banner {

        companion object {
            val view get() = By.res("Stream_ScheduledMessagesBanner")

            private val scheduledForPrefix
                get() = appContext.getString(ComposeR.string.stream_compose_schedule_message_scheduled_for, "")

            /** The banner of a single scheduled message or reply, whatever its date. */
            val scheduledFor get() = view.hasDescendant(By.textStartsWith(scheduledForPrefix))

            /** The banner of a single scheduled message or reply, sent at [time]. */
            fun scheduledFor(time: String) = view.hasDescendant(
                By.text(Pattern.compile(Pattern.quote(scheduledForPrefix) + ".* " + Pattern.quote(time))),
            )

            /** The banner of a channel with [count] scheduled messages, more than one. */
            fun messages(count: Int) =
                view.hasDescendant(By.text(quantityString(ComposeR.plurals.stream_compose_scheduled_messages_count, count)))

            /** The banner of a channel with [count] of [limit] scheduled messages, the most it allows. */
            fun limitReached(count: Int, limit: Int) = view.hasDescendant(
                By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_messages_limit_reached, count, limit)),
            )

            /** The banner of a channel with [count] scheduled messages that failed. */
            fun failed(count: Int) = view.hasDescendant(
                By.text(quantityString(ComposeR.plurals.stream_compose_scheduled_messages_failed_count, count)),
            )

            /** The link of a banner about several messages. */
            val viewAll get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_messages_view_all))
        }
    }

    /** The SDK sheet with the quick times, opened with a long press on send. */
    class ScheduleSheet {

        companion object {
            val view get() = By.res("Stream_ScheduleMessageSheet")

            /** Tomorrow at 9:00. */
            val tomorrowMorning get() = quickDate(0)
            val customTime get() = By.res("Stream_ScheduleMessageCustomTime")

            /** The quick time at [index]: tomorrow at 9:00, tomorrow at 13:00, Monday at 9:00. */
            fun quickDate(index: Int) = By.res("Stream_ScheduleMessageQuickDate$index")

            /** The title when the channel has [limit] scheduled messages, the most it allows. */
            fun limitReachedTitle(limit: Int) =
                By.text(appContext.getString(ComposeR.string.stream_compose_schedule_message_limit_reached, limit))
        }
    }

    /** The SDK sheet with the date and time pickers, for a custom time or a reschedule. */
    class ScheduleTimeSheet {

        companion object {
            val view get() = By.res("Stream_ScheduleMessageTimeSheet")
            val preview get() = By.res("Stream_ScheduleMessagePreview")
            val date get() = By.res("Stream_ScheduleMessageDate")
            val time get() = By.res("Stream_ScheduleMessageTime")
            val error get() = By.res("Stream_ScheduleMessageError")
            val cancelButton get() = By.res("Stream_ScheduleMessageCancel")
            val confirmButton get() = By.res("Stream_ScheduleMessageConfirm")

            /** The error that tells [text]. */
            fun error(text: String) = error.hasDescendant(By.text(text))
        }
    }

    /**
     * The Material date and time dialogs the time sheet opens. Their own labels come from Material
     * and are in English, like the device of the tests.
     */
    class Pickers {

        companion object {
            val datePicker get() = By.res("Stream_ScheduleMessageDatePicker")
            val timePicker get() = By.res("Stream_ScheduleMessageTimePicker")
            val okButton get() = By.text(appContext.getString(ComposeR.string.stream_compose_schedule_message_ok))

            val textInputModeButton get() = By.desc("Switch to text input mode")
            val dateField get() = By.clazz("android.widget.EditText").hasAncestor(datePicker)

            val hourSelector get() = By.desc("Select hour")
            val minuteSelector get() = By.desc("Select minutes")

            /** The field of the time dialog that has the focus: the hour when the dialog opens. */
            val focusedField get() = By.clazz("android.widget.EditText").focused(true).hasAncestor(timePicker)
            val amButton get() = By.text("AM").hasAncestor(timePicker)
            val pmButton get() = By.text("PM").hasAncestor(timePicker)
        }
    }

    /** The SDK scheduled messages screen. */
    class Screen {

        companion object {
            val title get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_message_list_title))
            val empty get() = By.res("Stream_ScheduledMessageListEmpty")
            val loadFailed get() = By.res("Stream_ScheduledMessageListError")
            val retryButton get() = By.res("Stream_ScheduledMessageListRetry")
            val items get() = By.res("Stream_ScheduledMessageItem")
            val drawerItem get() = By.res("Stream_DrawerScheduledMessages")

            /**
             * The row whose preview ends with [text]: a prefix or an attachment icon can come before
             * it, and the preview formatter can add a space after it.
             */
            fun item(text: String) = items.hasDescendant(
                By.res("Stream_ScheduledMessageText").text(Pattern.compile(".*" + Pattern.quote(text) + "\\s*")),
            )

            /** The date line of a row, sent at [time]. */
            fun date(time: String) = By.res("Stream_ScheduledMessageDate").textEndsWith(time)

            /** The status line of a row that tells [text]: sending, uploading, or why it failed. */
            fun status(text: String) = By.res("Stream_ScheduledMessageStatus").hasDescendant(By.text(text))

            /** The row of a reply whose preview ends with [text], after the name of the quoted author. */
            fun reply(text: String): BySelector {
                val prefix = appContext.getString(ComposeR.string.stream_compose_scheduled_message_list_reply_to, "\u0000")
                    .split("\u0000")
                val pattern = Pattern.quote(prefix.first()) + ".+" + Pattern.quote(prefix.last()) + " .*" +
                    Pattern.quote(text) + "\\s*"
                return items.hasDescendant(By.res("Stream_ScheduledMessageText").text(Pattern.compile(pattern)))
            }

            /** The prefix of the preview of a thread reply. */
            val threadReplyPrefix
                get() = appContext.getString(ComposeR.string.stream_compose_scheduled_message_list_thread_reply)
        }
    }

    /** The SDK sheet with the actions of a scheduled message, opened with a tap on its row. */
    class Actions {

        companion object {
            val view get() = By.res("Stream_ScheduledMessageActions")
            val edit get() = action("Edit")
            val reschedule get() = action("Reschedule")
            val sendNow get() = action("SendNow")
            val editAndReschedule get() = action("EditAndReschedule")
            val copyText get() = action("CopyText")
            val delete get() = action("Delete")

            private fun action(name: String) = By.res("Stream_ScheduledMessageAction_$name")
        }
    }

    /** The SDK dialog that asks to confirm the deletion. */
    class DeleteDialog {

        companion object {
            val view get() = By.res("Stream_ScheduledMessageDeleteDialog")
            val confirmButton get() = By.res("Stream_ScheduledMessageDeleteConfirm")
        }
    }

    class Snackbar {

        companion object {
            /** Shown when a message scheduled offline is sent right away, as its time passed. */
            val sent get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_messages_sent))
            val viewButton get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_messages_view))

            /** Shown once the server has the change of an edited scheduled message. */
            val scheduled
                get() = By.textStartsWith(appContext.getString(SampleR.string.scheduled_messages_scheduled_for, ""))

            /** Shown when the message the user acts on was already sent, or deleted on another device. */
            val gone
                get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_message_list_gone))

            /** Shown when the user sends a message now while offline. */
            val sendNowQueued
                get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_message_list_send_now_queued))

            /** Shown when the user taps a message that is sending. */
            val stillSending
                get() = By.text(appContext.getString(ComposeR.string.stream_compose_scheduled_message_list_still_sending))
        }
    }

    /** The composer while a scheduled message is edited. */
    class Editor {

        companion object {
            val saveButton get() = By.res("Stream_ScheduledMessageSaveButton")
        }
    }
}

private fun quantityString(id: Int, count: Int): String = appContext.resources.getQuantityString(id, count, count)
