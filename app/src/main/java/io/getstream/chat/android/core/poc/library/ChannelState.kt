package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import androidx.room.Embedded
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ChannelState @JvmOverloads constructor() {

    var cid: String? = null

    lateinit var channel: Channel
    
    @SerializedName("messages")
    @Expose
    private var messages: MutableList<Message>? = null
    @Embedded(prefix = "last_message_")
    private var lastMessage: Message? = null
    @SerializedName("read")
    @Expose
    private var reads: MutableList<ChannelUserRead>? = null
    @SerializedName("members")
    @Expose
    private var members: MutableList<Member> = mutableListOf()
    
    @SerializedName("watchers")
    @Expose
    private var watchers: MutableList<Watcher> = mutableListOf()
    
    @SerializedName("watcher_count")
    var watcherCount = 0
    
    private var lastKnownActiveWatcher: Long = 0

    fun getWatchers(): List<Watcher> {
        return watchers
    }

    fun preStorage() {
        cid = channel.cid
        lastMessage = computeLastMessage()
    }

    private fun getLastKnownActiveWatcher(): Long {
        return lastKnownActiveWatcher
    }

    fun copy(): ChannelState {
        val clone = ChannelState()
        clone.channel = channel
        clone.reads = ArrayList()
        for (read in getReads()!!) {
            clone.reads!!.add(ChannelUserRead(read.getUser(), read.getLastRead()))
        }
        clone.setLastMessage(getLastMessage())
        return clone
    }

    @Synchronized
    fun addWatcher(watcher: Watcher) {
        watchers.remove(watcher)
        watchers.add(watcher)
    }

    fun removeWatcher(watcher: Watcher) {
        if (watcher.user.lastActive.after(getLastKnownActiveWatcher())) {
            lastKnownActiveWatcher = watcher.user.lastActive
        }
        watchers!!.remove(watcher)
    }

    fun addOrUpdateMember(member: Member) {
        if (members == null) {
            members = ArrayList()
        }
        val index = members!!.indexOf(member)
        if (index >= 0) {
            members!![index] = member
        } else {
            members!!.add(member)
        }
    }

    fun removeMemberById(userId: String) {
        if (members == null || members!!.isEmpty()) return
        val it =
            members!!.iterator()
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
            if (members != null) {
                for (m in members!!) {
                    if (!channel.client.fromCurrentUser(m)) {
                        val user: User =
                            channel.client.getState().getUser(m.user.id)

                        users.add(user)
                    }
                }
            }
            if (watchers != null) {
                for (w in watchers!!) {
                    if (!channel.client.fromCurrentUser(w)) {
                        val user: User =
                            channel.client.getState().getUser(w.getUser().getId())

                        if (!users.contains(user)) users.add(user)
                    }
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
            var lastActive: Date? = channel.getCreatedAt()
            if (lastActive == null) lastActive = Date()
            if (getLastKnownActiveWatcher().after(lastActive)) {
                lastActive = getLastKnownActiveWatcher()
            }
            val message: Message? = lastMessageFromOtherUser
            if (message != null) {
                if (message.getCreatedAt().after(lastActive)) {
                    lastActive = message.getCreatedAt()
                }
            }
            for (watcher in getWatchers()) {
                if (watcher.getUser() == null || watcher.getUser().getLastActive() == null) continue
                if (lastActive.before(watcher.getUser().getLastActive())) {
                    if (channel.getClient().fromCurrentUser(watcher)) continue
                    lastActive = watcher.getUser().getLastActive()
                }
            }
            return lastActive
        }

    val channelNameOrMembers: String
        get() {
            var channelName: String
            Log.i(TAG, "Channel name is" + channel.getName() + channel.getCid())
            if (!TextUtils.isEmpty(channel.getName())) {
                channelName = channel.getName()
            } else {
                val users =
                    otherUsers
                val top3 =
                    users.subList(0, Math.min(3, users.size))
                val usernames: MutableList<String?> = ArrayList()
                for (u in top3) {
                    if (u == null) continue
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
            if (messages == null) {
                return null
            }
            for (m in messages!!) {
                if (m.getSyncStatus() === Sync.SYNCED) {
                    return m
                }
            }
            return null
        }

    fun getMessages(): List<Message> {
        if (messages == null) {
            return ArrayList()
        }
        for (m in messages!!) {
            m.setCid(cid)
        }
        return messages
    }

    fun setMessages(messages: MutableList<Message>?) {
        this.messages = messages
    }

    fun getReads(): List<ChannelUserRead>? {
        if (reads == null) {
            reads = ArrayList()
        }
        return reads
    }

    fun setReads(reads: MutableList<ChannelUserRead>?) {
        this.reads = reads
    }

    val readsByUser: Map<String, Any>
        get() {
            val readsByUser: MutableMap<String, ChannelUserRead> = HashMap()
            for (r in getReads()!!) {
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
            if (reads == null || lastMessage == null) return readLastMessage
            val client = channel.client
            val userID: String = client.setUser()
            for (r in reads!!) {
                if (r.getUserId().equals(userID)) continue
                if (r.getLastRead().compareTo(lastMessage.getCreatedAt()) > -1) {
                    readLastMessage.add(r)
                }
            }
            // sort the reads
            Collections.sort(
                readLastMessage,
                { o1: ChannelUserRead, o2: ChannelUserRead ->
                    o1.getLastRead().compareTo(o2.getLastRead())
                })
            return readLastMessage
        }

    fun getMembers(): List<Member> {
        return members
    }

    fun setMembers(members: MutableList<Member>) {
        this.members = members
    }

    fun getLastMessage(): Message? {
        if (lastMessage == null) {
            lastMessage = computeLastMessage()
        }
        return lastMessage
    }

    fun setLastMessage(lastMessage: Message?) {
        if (lastMessage == null) return
        if (lastMessage.getDeletedAt() != null) {
            this.lastMessage = computeLastMessage()
            return
        }
        this.lastMessage = lastMessage
    }

    fun computeLastMessage(): Message? {
        var lastMessage: Message? = null
        val messages: List<Message> = getMessages()
        for (i in messages.indices.reversed()) {
            val message: Message = messages[i]
            if (message.getDeletedAt() == null && message.getType().equals(ModelType.message_regular)) {
                lastMessage = message
                break
            }
        }
        Message.setStartDay(Collections.singletonList(lastMessage), null)
        return lastMessage
    }

    private val lastMessageFromOtherUser: Message?
        private get() {
            var lastMessage: Message? = null
            try {
                val messages: List<Message> = getMessages()
                for (i in messages.indices.reversed()) {
                    val message: Message = messages[i]
                    if (message.getDeletedAt() == null && !channel.getClient().fromCurrentUser(
                            message
                        )
                    ) {
                        lastMessage = message
                        break
                    }
                }
                Message.setStartDay(Collections.singletonList(lastMessage), null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return lastMessage
        }

    val lastReader: User?
        get() {
            if (reads == null || reads!!.isEmpty()) return null
            var lastReadUser: User? = null
            for (i in reads!!.indices.reversed()) {
                val channelUserRead: ChannelUserRead = reads!![i]
                if (!channel.getClient().fromCurrentUser(channelUserRead)) {
                    lastReadUser = channelUserRead.getUser()
                    break
                }
            }
            return lastReadUser
        }

    private fun addOrUpdateMessage(newMessage: Message) {
        if (messages!!.size > 0) {
            for (i in messages!!.indices.reversed()) {
                if (messages!![i].getId().equals(newMessage.getId())) {
                    messages!![i] = newMessage
                    return
                }
                if (messages!![i].getCreatedAt().before(newMessage.getCreatedAt())) {
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
            if (m.getParentId() == null) {
                addOrUpdateMessage(m)
            }
        }
    }

    fun init(incoming: ChannelState) {
        reads = incoming.reads
        watcherCount = incoming.watcherCount
        if (watcherCount > 1) {
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
            val client: com.sun.security.ntlm.Client = channel.getClient()
            val userID: String = client.getUserId()
            return getUnreadMessageCount(userID)
        }

    private fun getUnreadMessageCount(userId: String): Int {
        var unreadMessageCount = 0
        if (reads == null || reads!!.isEmpty()) return unreadMessageCount
        val lastReadDate: Date =
            getReadDateOfChannelLastMessage(userId) ?: return unreadMessageCount
        for (i in messages!!.indices.reversed()) {
            val message: Message = messages!![i]
            if (message.getUser().getId().equals(userId)) continue
            if (message.getDeletedAt() != null) continue
            if (message.getCreatedAt().getTime() > lastReadDate.getTime()) unreadMessageCount++
        }
        return unreadMessageCount
    }

    fun getReadDateOfChannelLastMessage(userId: String?): Date? {
        if (reads == null || reads!!.isEmpty()) return null
        var lastReadDate: Date? = null
        try {
            for (i in reads!!.indices.reversed()) {
                val channelUserRead: ChannelUserRead = reads!![i]
                if (channelUserRead.getUser().getId().equals(userId)) {
                    lastReadDate = channelUserRead.getLastRead()
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
        readDate: Date?
    ) {
        for (i in getReads()!!.indices) {
            val current: ChannelUserRead = reads!![i]
            if (current.getUserId().equals(user.id)) {
                reads!!.removeAt(i)
                current.setLastRead(readDate)
                reads!!.add(current)
                return
            }
        }
        val channelUserRead = ChannelUserRead(user, readDate)
        reads!!.add(channelUserRead)
    }

    // if user read the last message returns true, else false.
    fun readLastMessage(): Boolean {
        val client= channel.client
        val userID: String = client.getUserId()
        val myReadDate: Date? = getReadDateOfChannelLastMessage(userID)
        return if (myReadDate == null) {
            false
        } else if (getLastMessage() == null) {
            true
        } else myReadDate.getTime() > getLastMessage().getCreatedAt().getTime()
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
        if (channel == null || channel.getChannelState() == null) {
            messages = ArrayList()
            reads = ArrayList()
            members = ArrayList()
        } else {
            messages = channel.getChannelState().messages
            reads = channel.getChannelState().reads
            members = channel.getChannelState().members
        }
    }
}

