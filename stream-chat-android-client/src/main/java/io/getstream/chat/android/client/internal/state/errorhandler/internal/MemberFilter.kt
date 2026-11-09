/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.errorhandler.internal

import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.AutocompleteFilterObject
import io.getstream.chat.android.models.ContainsFilterObject
import io.getstream.chat.android.models.DistinctFilterObject
import io.getstream.chat.android.models.EqualsFilterObject
import io.getstream.chat.android.models.ExistsFilterObject
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.GreaterThanFilterObject
import io.getstream.chat.android.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.models.InFilterObject
import io.getstream.chat.android.models.LessThanFilterObject
import io.getstream.chat.android.models.LessThanOrEqualsFilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.NorFilterObject
import io.getstream.chat.android.models.NotEqualsFilterObject
import io.getstream.chat.android.models.NotExistsFilterObject
import io.getstream.chat.android.models.NotInFilterObject
import io.getstream.chat.android.models.OrFilterObject
import io.getstream.chat.android.models.querysort.ComparableFieldProvider
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.models.querysort.SortDirection
import java.text.Collator
import java.text.Normalizer
import java.util.Date
import java.util.Locale

private const val NAME_FIELD = "name"
private const val INVITE_PENDING = "pending"
private const val INVITE_ACCEPTED = "accepted"
private const val INVITE_REJECTED = "rejected"

private val nonWordCharacters = Regex("[^\\p{L}\\p{N}]+")
private val combiningMarks = Regex("\\p{Mn}+")
private val dateFormatter = StreamDateFormatter()

/**
 * Whether [member] of the channel [cid] matches this member query filter, evaluated on the device.
 *
 * Supports the member fields the backend filters on and that a stored [Member] holds: `id`, `user_id`, `name`,
 * `user.email`, `channel_role`, `banned`, `notifications_muted`, `invite`, `joined`, `created_at`, `updated_at`,
 * `last_active`, `cid` and member custom fields. `name` autocomplete matches when every typed word starts a word of the
 * user name, ignoring case and accents. Any other field is read from the member custom data. A comparison with a field
 * the member does not hold, or with a value of another type, does not match.
 */
internal fun FilterObject.matchesMember(member: Member, cid: String): Boolean = when (this) {
    is NeutralFilterObject -> true
    is AndFilterObject -> filterObjects.all { it.matchesMember(member, cid) }
    is OrFilterObject -> filterObjects.any { it.matchesMember(member, cid) }
    is NorFilterObject -> filterObjects.none { it.matchesMember(member, cid) }
    is EqualsFilterObject -> member.fieldValue(fieldName, cid).isEqualTo(value)
    is NotEqualsFilterObject ->
        member.fieldValue(fieldName, cid).let { field -> field != null && !field.isEqualTo(value) }
    is InFilterObject -> member.fieldValue(fieldName, cid).let { field -> values.any(field::isEqualTo) }
    is NotInFilterObject ->
        member.fieldValue(fieldName, cid).let { field -> field != null && values.none(field::isEqualTo) }
    is GreaterThanFilterObject -> member.fieldValue(fieldName, cid).compareWith(value)?.let { it > 0 } == true
    is GreaterThanOrEqualsFilterObject -> member.fieldValue(fieldName, cid).compareWith(value)?.let { it >= 0 } == true
    is LessThanFilterObject -> member.fieldValue(fieldName, cid).compareWith(value)?.let { it < 0 } == true
    is LessThanOrEqualsFilterObject -> member.fieldValue(fieldName, cid).compareWith(value)?.let { it <= 0 } == true
    is ExistsFilterObject -> member.fieldValue(fieldName, cid) != null
    is NotExistsFilterObject -> member.fieldValue(fieldName, cid) == null
    is ContainsFilterObject ->
        (member.fieldValue(fieldName, cid) as? Collection<*>)?.any { it.isEqualTo(value) } == true
    is AutocompleteFilterObject -> member.fieldValue(fieldName, cid).autocompletes(value)
    is DistinctFilterObject -> false
}

/**
 * The comparator for this member query sort on the device. A `name` sort compares the user names in US English
 * collation order, where case and accents only decide between otherwise equal names, as the backend sorts them. Any
 * other sort keeps [QuerySorter.comparator].
 */
internal val QuerySorter<Member>.memberComparator: Comparator<in Member>
    get() {
        if (sortSpecifications.none { it.sortAttribute.name == NAME_FIELD }) return comparator
        val sort = sortSpecifications.fold(QuerySortByField<UserNameSortFields>()) { sort, specification ->
            when (specification.sortDirection) {
                SortDirection.ASC -> sort.asc(specification.sortAttribute.name)
                SortDirection.DESC -> sort.desc(specification.sortAttribute.name)
            }
        }
        val collator = Collator.getInstance(Locale.US)
        return compareBy(sort.comparator) { member: Member -> UserNameSortFields(member, collator) }
    }

private class UserNameSortFields(private val member: Member, private val collator: Collator) : ComparableFieldProvider {
    override fun getComparableField(fieldName: String): Comparable<*>? = when (fieldName) {
        NAME_FIELD -> collator.getCollationKey(member.user.name)
        else -> member.getComparableField(fieldName)
    }
}

private fun Member.fieldValue(fieldName: String, cid: String): Any? = when (fieldName) {
    "id", "user_id" -> user.id
    NAME_FIELD -> user.name
    "user.email" -> user.extraData["email"]
    "channel_role" -> channelRole
    "banned" -> banned
    "notifications_muted" -> notificationsMuted == true
    "invite" -> inviteStatus
    "joined" -> isInvited != true || inviteAcceptedAt != null
    "created_at" -> createdAt
    "updated_at" -> updatedAt
    "last_active" -> user.lastActive
    "cid" -> cid
    else -> extraData[fieldName]
}

private val Member.inviteStatus: String?
    get() = when {
        isInvited != true -> null
        inviteAcceptedAt != null -> INVITE_ACCEPTED
        inviteRejectedAt != null -> INVITE_REJECTED
        else -> INVITE_PENDING
    }

private fun Any?.isEqualTo(value: Any?): Boolean = compareWith(value) == 0

/**
 * Compares this field value with a filter [value]: dates with dates or date strings, numbers as numbers, and strings
 * and booleans as they are. Returns null when the two cannot be compared.
 */
private fun Any?.compareWith(value: Any?): Int? = when {
    this == null || value == null -> null
    this is Date -> value.toDateOrNull()?.let { compareTo(it) }
    this is Number && value is Number -> toDouble().compareTo(value.toDouble())
    this is String && value is String -> compareTo(value)
    this is Boolean && value is Boolean -> compareTo(value)
    else -> null
}

private fun Any.toDateOrNull(): Date? = when (this) {
    is Date -> this
    is String -> dateFormatter.parse(this)
    else -> null
}

private fun Any?.autocompletes(query: String): Boolean {
    val words = (this as? String)?.searchWords() ?: return false
    val terms = query.searchWords().filter(String::isNotEmpty)
    return terms.isNotEmpty() && terms.all { term -> words.any { it.startsWith(term) } }
}

private fun String.searchWords(): List<String> =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(combiningMarks, "").split(nonWordCharacters)
