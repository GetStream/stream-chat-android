package com.getstream.sdk.chat

import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import java.io.File
import java.time.Instant
import java.util.Date
import java.util.concurrent.ThreadLocalRandom

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
val random
	get() = ThreadLocalRandom.current()

fun positveRandomInt(maxInt: Int = Int.MAX_VALUE - 1): Int =
		random.nextInt(maxInt + 1).takeIf { it > 0 } ?: positveRandomInt(maxInt)

fun positveRandomLong(maxLong: Long = Long.MAX_VALUE - 1): Long =
		random.nextLong(maxLong + 1).takeIf { it > 0 } ?: positveRandomLong(maxLong)

fun randomInt() = random.nextInt()
fun randomIntBetween(min: Int, max: Int) = random.nextInt(max - min) + min
fun randomLong() = random.nextLong()
fun randomBoolean() = random.nextBoolean()
fun randomString(size: Int = 20): String = (0..size)
		.map { charPool[random.nextInt(0, charPool.size)] }
		.joinToString("")

fun createUser(
		id: String = randomString(),
		role: String = randomString(),
		invisible: Boolean = randomBoolean(),
		banned: Boolean = randomBoolean(),
		devices: List<Device> = mutableListOf(),
		online: Boolean = randomBoolean(),
		createdAt: Date? = null,
		updatedAt: Date? = null,
		lastActive: Date? = null,
		totalUnreadCount: Int = positveRandomInt(),
		unreadChannels: Int = positveRandomInt(),
		unreadCount: Int = positveRandomInt(),
		mutes: List<Mute> = mutableListOf(),
		teams: List<String> = listOf(),
		extraData: MutableMap<String, Any> = mutableMapOf()
): User = User(id, role, invisible, banned, devices, online, createdAt, updatedAt, lastActive,
		totalUnreadCount, unreadChannels, unreadCount, mutes, teams, extraData)

fun createMember(
		user: User = createUser(),
		role: String = randomString(),
		createdAt: Date? = null,
		updatedAt: Date? = null,
		isInvited: Boolean = randomBoolean(),
		inviteAcceptedAt: Date? = null,
		inviteRejectedAt: Date? = null
): Member = Member(user, role, createdAt, updatedAt, isInvited, inviteAcceptedAt, inviteRejectedAt)

fun createMembers(
		size: Int = positveRandomInt(10),
		creationFunction: (Int) -> Member = { createMember() }
): List<Member> = (1..size).map { creationFunction(it) }

fun createChannel(cid: String): Channel = Channel(cid = cid)

fun createAttachmentMetaDataWithFile(
		file: File = createFile(),
		forceMimeType: String? = null
): AttachmentMetaData = AttachmentMetaData(file).apply { forceMimeType?.let { mimeType = it } }

fun createAttachmentMetaDataWithAttachment(attachment: Attachment = createAttachment()): AttachmentMetaData = AttachmentMetaData(attachment)

fun createFile(path: String = randomString()): File = File(path)

fun createAttachment(
		authorName: String? = randomString(),
		titleLink: String? = randomString(),
		thumbUrl: String? = randomString(),
		imageUrl: String? = randomString(),
		assetUrl: String? = randomString(),
		ogUrl: String? = randomString(),
		mimeType: String? = randomString(),
		fileSize: Int = randomInt(),
		title: String? = randomString(),
		text: String? = randomString(),
		type: String? = randomString(),
		image: String? = randomString(),
		url: String? = randomString(),
		name: String? = randomString(),
		fallback: String? = randomString(),
		extraData: MutableMap<String, Any> = mutableMapOf()
): Attachment = Attachment(authorName, titleLink, thumbUrl, imageUrl, assetUrl, ogUrl, mimeType,
		fileSize, title, text, type, image, url, name, fallback, extraData)

fun createMessage(
		id: String = randomString(),
		cid: String = randomString(),
		text: String = randomString(),
		createdAt: Date? = Date.from(Instant.now()),
		parentId: String? = null
): Message = Message(id, cid, text, createdAt = createdAt, parentId = parentId)

fun createMessageList(size: Int = 10) = (0..size).map { createMessage() }.toList()

fun createThreadMessageList(size: Int = 10, parentMessageId: String) =
		(0 until size).map { createMessage(parentId = parentMessageId) }.toList()

fun createChannelUserRead(user: User = createUser(),
						  lastReadDate: Date = Date.from(Instant.now()),
						  unreadMessages: Int = 0) = ChannelUserRead(user, lastReadDate, unreadMessages)