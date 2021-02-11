package io.getstream.chat.android.ui.common.extensions.internal

internal fun <ElementT> Collection<ElementT>.firstOrDefault(default: ElementT): ElementT =
    firstOrNull() ?: default

internal fun <ElementT> Collection<ElementT>.firstOrDefault(
    element: ElementT,
    predicate: (ElementT) -> Boolean
): ElementT = firstOrNull(predicate) ?: element
