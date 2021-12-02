package io.getstream.chat.android.ui.common.markdown

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ItalicFixTest {

    @Test
    fun italicShouldHaveSpaceAddedToIt() {
        val italicText = "*ha*"
        val expected = "$italicText&#x200A;"
        val response = fixItalicAtEnd(italicText)

        assertEquals(expected, response)
    }

    @Test
    fun italicShouldHaveSpaceAddedToIt_ComplexScenario_Positive() {
        val italicText = "*_ha_ llalal _heey_ *ha!* *"
        val expected = "$italicText&#x200A;"
        val response = fixItalicAtEnd(italicText)

        assertEquals(expected, response)
    }

    @Test
    fun italicShouldHaveSpaceAddedToIt_ComplexScenario_Negative() {
        val italicText = "*_ha_ llalal _heey_ *ha!"
        val expected = italicText
        val response = fixItalicAtEnd(italicText)

        assertEquals(expected, response)
    }

    @Test
    fun emptyStringsShouldNotBeAffected() {
        val text = ""
        val response = fixItalicAtEnd(text)

        assertEquals(text, response)
    }
}
