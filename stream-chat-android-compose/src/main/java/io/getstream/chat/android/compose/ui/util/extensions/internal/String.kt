package io.getstream.chat.android.compose.ui.util.extensions.internal

/**
 * Small extension function designed to add a
 * scheme to URLs that do not have one so that
 * they can be opened using [android.content.Intent.ACTION_VIEW]
 */
internal fun String.addSchemeToUrlIfNeeded(): String = when {
    this.startsWith("http://") -> this
    this.startsWith("https://") -> this
    else -> "http://$this"
}