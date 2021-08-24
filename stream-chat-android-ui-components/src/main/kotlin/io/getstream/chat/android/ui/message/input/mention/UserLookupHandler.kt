package io.getstream.chat.android.ui.message.input.mention

import io.getstream.chat.android.client.models.User

/**
 * Users lookup functional interface. Used to create custom users lookup algorithm.
 */
public interface UserLookupHandler {
    /**
     * Performs users lookup by given [query] in suspend way. It's executed on background, so it can perform heavy operations.
     *
     * @param query String as user input for lookup algorithm.
     * @return List of users as result of lookup.
     */
    public suspend fun handleUserLookup(query: String): List<User>
}
