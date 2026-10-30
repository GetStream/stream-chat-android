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
import io.getstream.chat.android.compose.sample.R as SampleR

/**
 * The sample's scheduled messages: the banner under the channel header, the sheets that pick when
 * a message is sent, and the Scheduled messages screen.
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

    /** The quick times, opened with a long press on send. */
    class ScheduleSheet {

        companion object {
            val view get() = By.res("Stream_ScheduleMessageSheet")

            /** Tomorrow at 9:00. */
            val tomorrowMorning get() = By.res("Stream_ScheduleMessagePreset0")
        }
    }

    /** The date and time pickers, for a custom time or a reschedule. */
    class ScheduleTimeSheet {

        companion object {
            val time get() = By.res("Stream_ScheduleTimeTime")
            val confirmButton get() = By.res("Stream_ScheduleTimeConfirm")

            /** The hour field of the time dialog: its first text field. */
            val hourField get() = By.clazz("android.widget.EditText")
            val timeDialogConfirmButton get() = By.text(appContext.getString(SampleR.string.scheduled_messages_ok))
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
