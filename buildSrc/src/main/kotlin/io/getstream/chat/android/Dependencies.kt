@file:Suppress("RedundantVisibilityModifier")

package io.getstream.chat.android

object Dependencies {

    @JvmStatic
    fun isNonStable(version: String): Boolean = isStable(version).not()

    @JvmStatic
    fun isStable(version: String): Boolean = ("^[0-9.-]+$").toRegex().matches(version)
}
