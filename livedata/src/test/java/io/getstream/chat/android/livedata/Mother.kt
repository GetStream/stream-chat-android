package io.getstream.chat.android.livedata

import java.io.File
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

fun randomCID() = "${randomString()}:${randomString()}"

fun randomFile(extension: String = randomString(3)) = File("${randomString()}.$extension")
fun randomFiles(
	size: Int = positveRandomInt(10),
	creationFunction: (Int) -> File = { randomFile() }
): List<File> =(1..size).map(creationFunction)

fun randomImageFile() = randomFile(extension = "jpg")