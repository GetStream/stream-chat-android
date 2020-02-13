package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.UndefinedDate
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class Message : UserEntity {

    var id: String = ""
    var cid: String = ""
    var text: String = ""
    val html: String = ""

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


    val extraData = mutableMapOf<String, Any>()

    var isStartDay = false
    var isYesterday = false
    var isToday = false
    var date: String = ""
    var time: String = ""

    companion object {

        val locale = Locale("en", "US", "POSIX")
        val messageDateFormat: DateFormat =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                locale
            )

        fun setStartDay(messages: List<Message>?, preMessage0: Message?) {
            if (messages == null) return
            if (messages.size == 0) return
            var preMessage =
                preMessage0 ?: messages[0]
            setFormattedDate(
                preMessage
            )
            val startIndex = if (preMessage0 != null) 0 else 1
            for (i in startIndex until messages.size) {
                if (i != startIndex) {
                    preMessage = messages[i - 1]
                }
                val message = messages[i]
                setFormattedDate(
                    message
                )
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
        return user!!.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (cid != other.cid) return false
        if (text != other.text) return false
        if (html != other.html) return false
        if (user != other.user) return false
        if (channel != other.channel) return false
        if (userID != other.userID) return false
        if (attachments != other.attachments) return false
        if (type != other.type) return false
        if (latest_reactions != other.latest_reactions) return false
        if (own_reactions != other.own_reactions) return false
        if (reply_count != other.reply_count) return false
        if (created_at != other.created_at) return false
        if (updated_at != other.updated_at) return false
        if (deleted_at != other.deleted_at) return false
        if (mentioned_users != other.mentioned_users) return false
        if (mentionedUsersId != other.mentionedUsersId) return false
        if (reactionCounts != other.reactionCounts) return false
        if (parentId != other.parentId) return false
        if (command != other.command) return false
        if (commandInfo != other.commandInfo) return false
        if (extraData != other.extraData) return false
        if (isStartDay != other.isStartDay) return false
        if (isYesterday != other.isYesterday) return false
        if (isToday != other.isToday) return false
        if (date != other.date) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cid.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + html.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + channel.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + attachments.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + latest_reactions.hashCode()
        result = 31 * result + own_reactions.hashCode()
        result = 31 * result + reply_count
        result = 31 * result + created_at.hashCode()
        result = 31 * result + updated_at.hashCode()
        result = 31 * result + deleted_at.hashCode()
        result = 31 * result + mentioned_users.hashCode()
        result = 31 * result + (mentionedUsersId?.hashCode() ?: 0)
        result = 31 * result + (reactionCounts?.hashCode() ?: 0)
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + (command?.hashCode() ?: 0)
        result = 31 * result + (commandInfo?.hashCode() ?: 0)
        result = 31 * result + extraData.hashCode()
        result = 31 * result + isStartDay.hashCode()
        result = 31 * result + isYesterday.hashCode()
        result = 31 * result + isToday.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }


}