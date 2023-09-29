package io.getstream.chat.android.core.utils

import io.getstream.chat.android.core.internal.utils.MutableValue
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MutableValueTest {

    @Test
    fun `test mutable value`() {
        val value = MutableValue(0)

        value.set(1)
        value.get() `should be equal to` 1
        value.isModified() `should be equal to` true

        value.set(2)
        value.get() `should be equal to` 2
        value.isModified() `should be equal to` true

        value.modify { 3 }
        value.get() `should be equal to` 3
        value.isModified() `should be equal to` true
    }
}