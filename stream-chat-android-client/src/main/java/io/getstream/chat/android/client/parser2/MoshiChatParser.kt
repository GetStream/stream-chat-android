package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.parser.ChatParser

internal class MoshiChatParser(
    private val legacyParserDelegate: ChatParser
) : ChatParser by legacyParserDelegate
