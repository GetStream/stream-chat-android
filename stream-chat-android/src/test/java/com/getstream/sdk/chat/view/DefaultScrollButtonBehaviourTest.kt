package com.getstream.sdk.chat.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

private const val NEW_MESSAGE_SINGULAR: String = "%s New Message"
private const val NEW_MESSAGES_PLURAL: String = "%s New Messages"

internal class DefaultScrollButtonBehaviourTest {

    private val unseenBottomBtn: ViewGroup = mock()
    private val newMessagesTextTV: TextView = mock()

    private val behaviour = MessageListView.DefaultScrollButtonBehaviour(
        unseenBottomBtn,
        newMessagesTextTV,
        NEW_MESSAGE_SINGULAR,
        NEW_MESSAGES_PLURAL
    )

    @Test
    fun `proves that button becomes visible when user has scrolled up`() {
        whenever(unseenBottomBtn.isShown) doReturn false

        behaviour.userScrolledUp()

        whenever(unseenBottomBtn.isShown) doReturn true

        behaviour.userScrolledUp()
        behaviour.userScrolledUp()

        verify(unseenBottomBtn, times(1)).visibility = View.VISIBLE
    }

    @Test
    fun `proves that button becomes NOT visible when user has scrolled down`() {
        whenever(unseenBottomBtn.isShown) doReturn true

        behaviour.userScrolledToTheBottom()

        whenever(unseenBottomBtn.isShown) doReturn false

        behaviour.userScrolledToTheBottom()
        behaviour.userScrolledToTheBottom()

        verify(unseenBottomBtn, times(1)).visibility = View.GONE
    }

    @Test
    fun `proves that the correct message is shown for a single new message`() {
        val newMessagesCount = 1

        behaviour.onUnreadMessageCountChanged(newMessagesCount)

        verify(newMessagesTextTV).text = "$newMessagesCount New Message"
    }

    @Test
    fun `proves that the correct message is shown for a many new messages`() {
        val newMessagesCount = 12
        behaviour.onUnreadMessageCountChanged(newMessagesCount)

        verify(newMessagesTextTV).text = "$newMessagesCount New Messages"
    }

    @Test
    fun `proves that the correct message is shown for null message passed - singular`() {
        testCorrectMessageForNullTexts(1)
    }

    @Test
    fun `proves that the correct message is shown for null message passed - plural`() {
        testCorrectMessageForNullTexts(12)
    }

    private fun testCorrectMessageForNullTexts(count: Int) {
        val behaviour = MessageListView.DefaultScrollButtonBehaviour(
            unseenBottomBtn,
            newMessagesTextTV,
            null,
            null
        )

        behaviour.onUnreadMessageCountChanged(count)

        verify(newMessagesTextTV).text = count.toString()
    }
}
