package com.getstream.sdk.chat

import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
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
		extraData: MutableMap<String, Any> = mutableMapOf()
): User = User(id, role, invisible, banned, devices, online, createdAt, updatedAt, lastActive,
		totalUnreadCount, unreadChannels, unreadCount, mutes, extraData)

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