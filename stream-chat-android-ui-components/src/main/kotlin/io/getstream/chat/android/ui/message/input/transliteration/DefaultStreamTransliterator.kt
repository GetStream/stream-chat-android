package io.getstream.chat.android.ui.message.input.transliteration

import android.icu.text.Transliterator
import android.os.Build
import androidx.annotation.RequiresApi
import io.getstream.chat.android.client.logger.ChatLogger

public class DefaultStreamTransliterator(transliterationId: String? = null) : StreamTransliterator {

    private var transliterator: Transliterator? = null
    private val logger = ChatLogger.get("DefaultStreamTransliterator")

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            transliterationId?.let(::setTransliterator)
        } else {
            logger.logD("This android version: ${Build.VERSION.SDK_INT} doesn't support transliteration natively. User a custom StreamTransliterator to add transliteration.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setTransliterator(id: String) {
        if (Transliterator.getAvailableIDs().asSequence().contains(id)) {
            this.transliterator = Transliterator.getInstance(id)
        } else {
            logger.logD("The id: $id for transliteration is not available")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun transliterate(text: String): String {
        return transliterator?.transliterate(text) ?: text
    }
}
