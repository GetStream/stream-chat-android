package com.getstream.sdk.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread safe registry of subscribers.
 * This ensures that you can't miss a message event while a handler is being added
 *
 * As an example:
 * - You add a handler, which does something slow and takes 3ms ---
 * - Next an event is fired on a different thread in 2ms        --
 * Without locking the handler would never receive the event
 */
public class EventSubscriberRegistry<T> {
    // the mapping of subscriberID to chat event handler
    private Map<Number, T> subscribers;
    // subscriberSequence, starts at 1
    private int subscriberSequence;
    // the lock used to make sure you cant read while someone else is adding a handler
    private ReentrantLock lock;

    public EventSubscriberRegistry() {
        this.subscribers = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    /**
     * Gets the list of subscribers
     * @return
     */
    public List<T> getSubscribers() {
        lock.lock();
        List<T> subs = new ArrayList<>();
        for (int i = subscriberSequence; i >= 1; i--) {
            T sub = subscribers.get(i);
            if (sub != null) {
                // subs can be removed so check for null
                subs.add(sub);
            }
        }
        lock.unlock();
        return subs;
    }

    /**
     * Adds a subscription
     * @param handler the chat event handler
     * @return
     */
    public int addSubscription(T handler) {
        lock.lock();
        int id = ++subscriberSequence;
        subscribers.put(id, handler);
        lock.unlock();
        return id;
    }

    /**
     * Removed the subscription by the subscription id returned by the addSubscription call
     * @param subId
     */
    public void removeSubscription(int subId) {
        lock.lock();
        subscribers.remove(subId);
        lock.unlock();
    }

    /**
     * Resets the list of subscribers and the subscriber sequence
     */
    public void clear() {
        lock.lock();
        this.subscribers = new HashMap<>();
        subscriberSequence = 0;
        lock.unlock();
    }
}
