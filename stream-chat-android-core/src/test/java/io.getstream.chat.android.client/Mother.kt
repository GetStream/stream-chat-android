/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client

import kotlin.random.Random

private val charPool: CharArray = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()

public fun positiveRandomInt(maxInt: Int = Int.MAX_VALUE - 1): Int =
    Random.nextInt(1, maxInt + 1)

public fun positiveRandomLong(maxLong: Long = Long.MAX_VALUE - 1): Long =
    Random.nextLong(1, maxLong + 1)

public fun randomInt(): Int = Random.nextInt()
public fun randomIntBetween(min: Int, max: Int): Int = Random.nextInt(min, max + 1)
public fun randomLong(): Long = Random.nextLong()
public fun randomLongBetween(min: Long, max: Long = Long.MAX_VALUE - 1): Long = Random.nextLong(min, max + 1)
public fun randomBoolean(): Boolean = Random.nextBoolean()
public fun randomString(size: Int = 20): String = buildString(capacity = size) {
    repeat(size) {
        append(charPool.random())
    }
}
