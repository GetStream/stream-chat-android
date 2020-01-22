package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.json.IgnoreSerialisation
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class Message : UserEntity {

    val id: String = ""
    var cid: String = ""
    var text: String = ""
    val html: String = ""

    var syncStatus: Int = 0
    lateinit var user: User
    lateinit var channel: Channel
    val userID: String = ""

    val attachments = emptyList<Reaction>()
    @IgnoreSerialisation
    val type: String = ""
    @IgnoreSerialisation
    val latest_reactions = emptyList<Reaction>()
    @IgnoreSerialisation
    val own_reactions = emptyList<Reaction>()
    @IgnoreSerialisation
    val reply_count = 0

    @IgnoreSerialisation
    var created_at: Date = UndefinedDate
    @IgnoreSerialisation
    var updated_at: Date = UndefinedDate
    @IgnoreSerialisation
    var deleted_at: Date = UndefinedDate

    val mentioned_users = emptyList<User>()
    val mentionedUsersId: List<String>? = null
    val reactionCounts: Map<String, Int>? = null

    val parentId: String? = null

    val command: String? = null

    val commandInfo: Map<String, String>? = null


    val extraData: HashMap<String, Any>? = null

    var isStartDay = false
    var isYesterday = false
    var isToday = false
    var date: String = ""
    var time: String = ""


    companion object {

        val locale = Locale("en", "US", "POSIX")
        val messageDateFormat: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", locale)

        fun setStartDay(messages: List<Message>?, preMessage0: Message?) {
            if (messages == null) return
            if (messages.size == 0) return
            var preMessage =
                preMessage0 ?: messages[0]
            setFormattedDate(preMessage)
            val startIndex = if (preMessage0 != null) 0 else 1
            for (i in startIndex until messages.size) {
                if (i != startIndex) {
                    preMessage = messages[i - 1]
                }
                val message = messages[i]
                setFormattedDate(message)
                message.isStartDay = !message.date.equals(preMessage.date)
            }
        }

        private fun setFormattedDate(message: Message?) {
            if (message == null || message.date != null) return
            messageDateFormat.timeZone = TimeZone.getTimeZone("GMT")
            val smsTime = Calendar.getInstance()
            smsTime.timeInMillis = message.created_at.time
            val now = Calendar.getInstance()
            if (now[Calendar.DATE] === smsTime[Calendar.DATE]) {
                message.isToday = true
                message.date = "today"
            } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] === 1) {
                message.isYesterday = true
                message.date = "yesterday"
            } else if (now[Calendar.WEEK_OF_YEAR] === smsTime[Calendar.WEEK_OF_YEAR]) {
                val dayName: DateFormat = SimpleDateFormat("EEEE")
                message.date = dayName.format(message.created_at)
            } else {
                val dateFormat: DateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG)
                message.date = dateFormat.format(message.created_at)
            }
            val timeFormat: DateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
            message.time = timeFormat.format(message.created_at)
        }
    }

    override fun getUserId(): String {
        return user?.id
    }


}