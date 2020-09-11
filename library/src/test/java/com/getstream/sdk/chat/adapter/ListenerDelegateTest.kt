package com.getstream.sdk.chat.adapter

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ListenerDelegateTest {

    interface TestListener {
        fun call(): Int
    }

    @Test
    fun test() {
        val listener1 = object : TestListener {
            override fun call() = 1
        }
        val listener2 = object : TestListener {
            override fun call() = 2
        }

        var delegate: TestListener by ListenerDelegate<TestListener>(
            initialValue = listener1,
            wrap = { realListener ->
                object : TestListener {
                    override fun call() = realListener().call()
                    override fun toString() = "wrapper"
                }
            }
        )

        // Save reference locally
        val localProp = delegate
        localProp.call() shouldBeEqualTo 1

        // Swap underlying listener back and forth, wrapper always calls the correct one
        delegate = listener2
        localProp.call() shouldBeEqualTo 2
        delegate = listener1
        localProp.call() shouldBeEqualTo 1

        localProp.toString() shouldBeEqualTo "wrapper"
    }
}
