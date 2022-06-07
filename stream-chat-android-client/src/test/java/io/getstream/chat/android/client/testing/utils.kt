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

package io.getstream.chat.android.client.testing

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.stream.Collectors

private val resClass = ResClass()

/**
 * Loads files under /resources directory
 * [path] "/model.member.json"
 */
internal fun loadResource(path: String): String {
    return convert(requireNotNull(resClass.javaClass.getResourceAsStream(path)))
}

internal fun convert(inputStream: InputStream): String {
    BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset())).use { br ->
        return br.lines().collect(Collectors.joining(System.lineSeparator()))
    }
}

private class ResClass
