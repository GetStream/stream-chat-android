package io.getstream.chat.android.client.utils

import org.mockito.ArgumentMatchers.argThat

/**
 * Calls ArgumentMatcher.argThat() and returns result to avoid nulls when using with non-nullable parameters
 */
internal fun <T> safeArgThat(result: T, matcher: ((T) -> Boolean)): T {
    argThat(matcher)
    return result
}
