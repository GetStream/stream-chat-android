package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import androidx.room.Embedded
import io.getstream.chat.android.core.poc.library.utils.isUndefined
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChannelState {

    var cid: String = ""

    lateinit var channel: Channel
    val messages = mutableListOf<Message>()
    @Embedded(prefix = "last_message_")
    private var lastMessage: Message? = null
    var read = mutableListOf<ChannelUserRead>()
    var members: MutableList<Member> = mutableListOf()
    val watchers: MutableList<Watcher> = mutableListOf()
    var watcher_count = 0

    private var lastKnownActiveWatcher: Date = Date()

    fun preStorage() {
        //cid = channel.cid
        lastMessage = computeLastMessage()
    }

    private fun getLastKnownActiveWatcher(): Date {
        return lastKnownActiveWatcher
    }

//    fun copy(): ChannelState {
//        val clone = ChannelState()
//        clone.channel = channel
//        clone.read = ArrayList()
//        for (read in getReads()) {
//            clone.read.add(ChannelUserRead(read.user, read.lastRead))
//        }
//        clone.setLastMessage(getLastMessage())
//        return clone
//    }

    @Synchronized
    fun addWatcher(watcher: Watcher) {
        watchers.remove(watcher)
        watchers.add(watcher)
    }

    fun removeWatcher(watcher: Watcher) {
        if (watcher.user.last_active.after(getLastKnownActiveWatcher())) {
            lastKnownActiveWatcher = watcher.user.last_active
        }
        watchers!!.remove(watcher)
    }

    fun addOrUpdateMember(member: Member) {
        val index = members.indexOf(member)
        if (index >= 0) {
            members[index] = member
        } else {
            members.add(member)
        }
    }

    fun removeMemberById(userId: String) {
        val it = members.iterator()
        while (it.hasNext()) {
            val member = it.next()
            if (member.getUserId() == userId) {
                it.remove()
            }
        }
    }

    val otherUsers: List<User>
        get() {
            val users: MutableList<User> =
                ArrayList()

            for (m in members) {
                if (!channel.client.fromCurrentUser(m)) {
                    val user: User =
                        channel.client.getState().getUser(m.user.id)

                    users.add(user)
                }
            }

            for (w in watchers) {
                if (!channel.client.fromCurrentUser(w)) {
                    val user: User =
                        channel.client.getState().getUser(w.user.id)

                    if (!users.contains(user)) users.add(user)
                }
            }

            return users
        }

    // TODO: we should ignore messages that haven't been sent yet
    val oldestMessageId: String?
        get() { // TODO: we should ignore messages that haven't been sent yet
            val message: Message = oldestMessage ?: return null
            return message.id
        }

    // last time the channel had a message from another user or (when more recent) the time a watcher was last active
    val lastActive: Date?
        get() {
            var lastActive: Date = channel.createdAt
            if (getLastKnownActiveWatcher().after(lastActive)) {
                lastActive = getLastKnownActiveWatcher()
            }
            val message: Message? = lastMessageFromOtherUser
            if (message != null) {
                if (message.created_at.after(lastActive)) {
                    lastActive = message.created_at
                }
            }
            for (watcher in watchers) {
                if (lastActive.before(watcher.user.last_active)) {
                    if (channel.client.fromCurrentUser(watcher)) continue
                    lastActive = watcher.user.last_active
                }
            }
            return lastActive
        }

    val channelNameOrMembers: String
        get() {
            var channelName: String
            if (!TextUtils.isEmpty(channel.getName())) {
                channelName = channel.getName()
            } else {
                val users =
                    otherUsers
                val top3 =
                    users.subList(0, Math.min(3, users.size))
                val usernames: MutableList<String?> = ArrayList()
                for (u in top3) {
                    usernames.add(u.name)
                }
                channelName = TextUtils.join(", ", usernames)
                if (users.size > 3) {
                    channelName += "..."
                }
            }
            return channelName
        }

    private val oldestMessage: Message?
        private get() {
            return messages.lastOrNull()
        }

    fun getReads(): List<ChannelUserRead> {
        return read
    }

    val readsByUser: Map<String, Any>
        get() {
            val readsByUser: MutableMap<String, ChannelUserRead> = HashMap()
            for (r in getReads()) {
                readsByUser[r.getUserId()] = r
            }
            return readsByUser
        }

    // sort the reads
    @get:Synchronized
    val lastMessageReads: List<Any>
        get() {
            val lastMessage: Message? = getLastMessage()
            val readLastMessage: MutableList<ChannelUserRead> = ArrayList()
            if (read == null || lastMessage == null) return readLastMessage
            val client = channel.client
            val userID: String = client.getUserId()
            for (r in read) {
                if (r.getUserId().equals(userID)) continue
                if (r.lastRead.compareTo(lastMessage.created_at) > -1) {
                    readLastMessage.add(r)
                }
            }
            // sort the reads
            Collections.sort(
                readLastMessage,
                { o1: ChannelUserRead, o2: ChannelUserRead ->
                    o1.lastRead.compareTo(o2.lastRead)
                })
            return readLastMessage
        }

    fun getLastMessage(): Message? {
        if (lastMessage == null) {
            lastMessage = computeLastMessage()
        }
        return lastMessage
    }

    fun setLastMessage(lastMessage: Message?) {
        if (lastMessage == null) return
        if (lastMessage.deleted_at.isUndefined()) {
            this.lastMessage = computeLastMessage()
            return
        }
        this.lastMessage = lastMessage
    }

    fun computeLastMessage(): Message? {
        var lastMessage: Message? = null
        val messages: List<Message> = this.messages
        for (i in messages.indices.reversed()) {
            val message: Message = messages[i]
            if (message.deleted_at.isUndefined() && message.type == ModelType.message_regular) {
                lastMessage = message
                break
            }
        }
        if (lastMessage != null) Message.setStartDay(listOf(lastMessage), null)
        return lastMessage
    }

    private val lastMessageFromOtherUser: Message?
        private get() {
            return messages.reversed().firstOrNull {
                it.deleted_at.isUndefined() && !channel.client.fromCurrentUser(it)
            }?.apply {
                Message.setStartDay(listOf(this), null)
            }
        }

    val lastReader: User?
        get() {
            if (read == null || read!!.isEmpty()) return null
            var lastReadUser: User? = null
            for (i in read!!.indices.reversed()) {
                val channelUserRead: ChannelUserRead = read!![i]
                if (!channel.client.fromCurrentUser(channelUserRead)) {
                    lastReadUser = channelUserRead.user
                    break
                }
            }
            return lastReadUser
        }

    private fun addOrUpdateMessage(newMessage: Message) {
        if (messages!!.size > 0) {
            for (i in messages!!.indices.reversed()) {
                messages!![i] = newMessage
                return
                if (messages!![i].created_at.before(newMessage.created_at)) {
                    messages!!.add(newMessage)
                    return
                }
            }
        } else {
            messages!!.add(newMessage)
        }
    }

    fun addMessageSorted(message: Message) {
        val diff: MutableList<Message> = ArrayList()
        diff.add(message)
        addMessagesSorted(diff)
        setLastMessage(message)
    }

    private fun addMessagesSorted(messages: List<Message>?) {
        for (m in messages!!) {
            if (m.parentId == null) {
                addOrUpdateMessage(m)
            }
        }
    }

    fun init(incoming: ChannelState) {
        read = incoming.read
        watcher_count = incoming.watcher_count
        if (watcher_count > 1) {
            lastKnownActiveWatcher = Date()
        }
        if (incoming.messages != null) {
            addMessagesSorted(incoming.messages)
            lastMessage = computeLastMessage()
        }
        if (incoming.watchers != null) {
            for (watcher in incoming.watchers!!) {
                addWatcher(watcher)
            }
        }
        if (incoming.members != null) {
            members = ArrayList(incoming.members!!)
        }
        // TODO: merge with incoming.reads
    }

    val currentUserUnreadMessageCount: Int
        get() {
            val userID: String = channel.client.getUserId()
            return getUnreadMessageCount(userID)
        }

    private fun getUnreadMessageCount(userId: String): Int {
        var unreadMessageCount = 0
        if (read == null || read!!.isEmpty()) return unreadMessageCount
        val lastReadDate: Date =
            getReadDateOfChannelLastMessage(userId) ?: return unreadMessageCount
        for (i in messages!!.indices.reversed()) {
            val message: Message = messages!![i]
            if (message.user.id == userId) continue
            if (message.created_at.time > lastReadDate.time) unreadMessageCount++
        }
        return unreadMessageCount
    }

    fun getReadDateOfChannelLastMessage(userId: String?): Date? {
        if (read == null || read!!.isEmpty()) return null
        var lastReadDate: Date? = null
        try {
            for (i in read!!.indices.reversed()) {
                val channelUserRead: ChannelUserRead = read!![i]
                if (channelUserRead.user.id == userId) {
                    lastReadDate = channelUserRead.lastRead
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lastReadDate
    }

    fun setReadDateOfChannelLastMessage(
        user: User,
        readDate: Date
    ) {
        for (i in getReads().indices) {
            val current: ChannelUserRead = read!![i]
            if (current.getUserId().equals(user.id)) {
                read.removeAt(i)
                current.lastRead = readDate
                read.add(current)
                return
            }
        }
        val channelUserRead = ChannelUserRead(user, readDate)
        read.add(channelUserRead)
    }

    // if user read the last message returns true, else false.
    fun readLastMessage(): Boolean {
        val client = channel.client
        val userID: String = client.getUserId()
        val myReadDate: Date? = getReadDateOfChannelLastMessage(userID)
        return if (myReadDate == null) {
            false
        } else {
            val lastMessage1 = getLastMessage()
            if (lastMessage1 == null) {
                true
            } else {
                myReadDate.time > lastMessage1.created_at.time
            }
        }
    }

    override fun toString(): String {
        return "ChannelState{" +
                "cid='" + cid + '\'' +
                ", channel=" + channel +
                '}'
    }

    companion object {
        private val TAG = ChannelState::class.java.simpleName
    }

    init {
//        messages = channel.channelState.messages
//        reads = channel.channelState.reads
//        members = channel.channelState.members
    }
}

