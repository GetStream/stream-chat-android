package io.getstream.chat.android.client.parser2.testdata

internal fun String.withoutWhitespace() = filterNot(Char::isWhitespace)
