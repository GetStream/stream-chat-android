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
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import io.getstream.chat.android.compose.R as ComposeR
import io.getstream.chat.android.compose.sample.R as SampleR

/**
 * Scheduled messages: the SDK sheets that pick when a message is sent, and the sample's banner under
 * the channel header and Scheduled messages screen.
 */
class ScheduledMessagesPage {

    class Banner {

        companion object {
            val view get() = By.res("Stream_ScheduledMessagesBanner")

            /** The banner of a channel with [count] scheduled messages. */
            fun messages(count: Int) = view.hasDescendant(By.text(quantityString(SampleR.plurals.scheduled_messages_count, count)))

            /** The banner of a thread with [count] scheduled replies. */
            fun replies(count: Int) =
                view.hasDescendant(By.text(quantityString(SampleR.plurals.scheduled_messages_replies_count, count)))

            /** The banner of a channel with [count] scheduled messages waiting for the connection. */
            fun waiting(count: Int) =
                view.hasDescendant(By.text(quantityString(SampleR.plurals.scheduled_messages_waiting_count, count)))

            /** The banner of a channel with [count] scheduled messages that failed. */
            fun failed(count: Int) =
                view.hasDescendant(By.text(quantityString(SampleR.plurals.scheduled_messages_failed_count, count)))
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

    class Screen {

        companion object {
            val title get() = By.text(appContext.getString(SampleR.string.scheduled_messages_title))
            val empty get() = By.res("Stream_ScheduledMessagesEmpty")
            val items get() = By.res("Stream_ScheduledMessageItem")
            val failedHeader get() = By.text(appContext.getString(SampleR.string.scheduled_messages_failed_header))
            val drawerItem get() = By.res("Stream_DrawerScheduledMessages")

            /** The row whose second line is [text]: the message text, or why it failed. */
            fun item(text: String) = items.hasDescendant(By.res("Stream_ScheduledMessageText").text(text))

            /** The time, or the progress, at the end of the row. */
            val status get() = By.res("Stream_ScheduledMessageStatus")

            /** Shown in a row while its change is on the way to the server. */
            val inProgress get() = By.res("Stream_ScheduledMessageInProgress")
            val dialogConfirmButton get() = By.res("Stream_ScheduledMessageDialogConfirm")
        }
    }

    /** The actions of a scheduled message, opened with a tap on its row. */
    class Actions {

        companion object {
            val view get() = By.res("Stream_ScheduledMessageActions")
            val edit get() = By.res("Stream_ScheduledMessageActionEdit")
            val reschedule get() = By.res("Stream_ScheduledMessageActionReschedule")
            val sendNow get() = By.res("Stream_ScheduledMessageActionSendNow")
            val retry get() = By.res("Stream_ScheduledMessageActionRetry")
            val scheduleAgain get() = By.res("Stream_ScheduledMessageActionScheduleAgain")
            val delete get() = By.res("Stream_ScheduledMessageActionDelete")
        }
    }

    class Snackbar {

        companion object {
            /** Shown once the server has the scheduled message. */
            val scheduled
                get() = By.textStartsWith(appContext.getString(SampleR.string.scheduled_messages_scheduled_for, ""))
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
