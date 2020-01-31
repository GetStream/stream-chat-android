package io.getstream.chat.android.client

import java.util.concurrent.locks.ReentrantLock


/**
 * A thread safe registry of subscribers.
 * This ensures that you can't miss a message event while a handler is being added
 *
 * As an example:
 * - You add a handler, which does something slow and takes 3ms ---
 * - Next an event is fired on a different thread in 2ms        --
 * Without locking the handler would never receive the event
 */
class EventSubscriberRegistry<T> {
    // the mapping of subscriberID to chat event handler
    private var subscribers: MutableMap<Number, T>
    // subscriberSequence, starts at 1
    private var subscriberSequence = 0
    // the lock used to make sure you cant read while someone else is adding a handler
    private val lock: ReentrantLock

    /**
     * Gets the list of subscribers
     * @return
     */
    fun getSubscribers(): List<T> {
        lock.lock()
        val subs: MutableList<T> = ArrayList()
        for (i in subscriberSequence downTo 1) {
            val sub = subscribers[i]
            if (sub != null) { // subs can be removed so check for null
                subs.add(sub)
            }
        }
        lock.unlock()
        return subs
    }

    /**
     * Adds a subscription
     * @param handler the chat event handler
     * @return
     */
    fun addSubscription(handler: T): Int {
        lock.lock()
        val id = ++subscriberSequence
        subscribers[id] = handler
        lock.unlock()
        return id
    }

    /**
     * Removed the subscription by the subscription id returned by the addSubscription call
     * @param subId
     */
    fun removeSubscription(subId: Int) {
        lock.lock()
        subscribers.remove(subId)
        lock.unlock()
    }

    /**
     * Resets the list of subscribers and the subscriber sequence
     */
    fun clear() {
        lock.lock()
        subscribers = HashMap()
        subscriberSequence = 0
        lock.unlock()
    }

    init {
        subscribers = HashMap()
        lock = ReentrantLock()
    }
}
