package io.getstream.chat.android.ui.message.input.mention

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.input.transliteration.StreamTransliterator
import java.text.Normalizer
import kotlin.math.min

private const val MAX_DISTANCE = 3
private val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()

public fun searchUsers(users: List<User>, query: String, streamTransliterator: StreamTransliterator): List<User> {
    return users
        .asSequence()
        .map { user ->
            val formattedQuery = query
                .lowercase()
                .unaccent()
                .let(streamTransliterator::transliterate)

            val formattedName = user.name.lowercase().unaccent()

            if (formattedName.contains(formattedQuery)) {
                user to 0
            } else {
                user to levenshtein(formattedQuery, formattedName)
            }
        }
        .filter { (_, distance) -> distance < MAX_DISTANCE }
        .sortedBy { (_, distance) -> distance }
        .map { (user, _) -> user }
        .toList()
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
