package com.getstream.sdk.chat.adapter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD

@TestInstance(PER_METHOD)
internal class ListenerDelegateTest {

    interface TestListener {
        fun call()
    }

    private var listener1: TestListener = mock()
    private var listener2: TestListener = mock()

    private var delegate: TestListener by ListenerContainerImpl.ListenerDelegate(
        initialValue = listener1,
        wrap = { realListener ->
            object : TestListener {
                override fun call() = realListener().call()
                override fun toString() = "wrapper"
            }
        }
    )

    @Test
    fun `Given valid setup Then the accessible listener is the wrapper`() {
        val delegateRef = delegate
        delegateRef.toString() shouldBeEqualTo "wrapper"
    }

    @Test
    fun `Given valid setup When listener is called Then the initial listener is called`() {
        delegate.call()
        verify(listener1).call()
    }

    @Test
    fun `Given valid setup When underlying listener is changed Then the new listener is called`() {
        val delegateRef = delegate

        delegate = listener2
        delegateRef.call()
        verify(listener2).call()

        delegate = listener1
        delegateRef.call()
        verify(listener1).call()
    }
}
