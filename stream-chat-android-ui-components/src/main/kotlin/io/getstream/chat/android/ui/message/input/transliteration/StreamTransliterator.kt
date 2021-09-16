package io.getstream.chat.android.ui.message.input.transliteration

public interface StreamTransliterator {

    public fun transliterate(text: String): String
}
