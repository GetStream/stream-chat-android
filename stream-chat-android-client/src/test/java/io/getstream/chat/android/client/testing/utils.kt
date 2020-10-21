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
    return convert(resClass.javaClass.getResourceAsStream(path))
}

internal fun convert(inputStream: InputStream): String {
    BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset())).use { br ->
        return br.lines().collect(Collectors.joining(System.lineSeparator()))
    }
}

private class ResClass
