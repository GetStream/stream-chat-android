package io.getstream.chat.android.ui.message.input.mention

import com.ibm.icu.text.Transliterator
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import java.text.Normalizer
import kotlin.math.min

private const val MAX_DISTANCE = 3
private val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()

public class DefaultUserLookupHandler(
    public var users: List<User>,
    transliterationId: String? = null,
) : UserLookupHandler {

    private var transliterator: Transliterator? = null
    private val logger = ChatLogger.get("DefaultUserLookupHandler")

    init {
        transliterationId.let { id ->
            if (Transliterator.getAvailableIDs().asSequence().contains(id)) {
                this.transliterator = Transliterator.getInstance(id)
            } else {
                logger.logD("The id: $transliterationId for transliteration is not available")
            }
        }
    }

    override suspend fun handleUserLookup(query: String): List<User> {
        return users.filter { user ->
            val formattedQuery = query
                .lowercase()
                .unaccent()
                .let {
                    transliterator?.transliterate(it) ?: it
                }

            val formattedName = user.name.lowercase().unaccent()

            formattedName.contains(formattedQuery) || levenshtein(formattedQuery, formattedName) <= MAX_DISTANCE
        }
    }
}

private fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return regexUnaccent.replace(temp, "")
}

private fun levenshtein(search: CharSequence, target: CharSequence): Int {
    when {
        search == target -> return 0
        search.isEmpty() -> return target.length
        target.isEmpty() -> return search.length
    }

    val searchLength = search.length + 1
    val targetLength = target.length + 1

    var cost = Array(searchLength) { it }
    var newCost = Array(searchLength) { 0 }

    for (i in 1 until targetLength) {
        newCost[0] = i

        for (j in 1 until searchLength) {
            val match = if (search[j - 1] == target[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[searchLength - 1]
}
