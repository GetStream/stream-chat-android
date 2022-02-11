package io.getstream.chat.android.client.experimental.plugin.factory

import io.getstream.chat.android.client.experimental.plugin.Plugin

/**
 * Interface used to add new plugins to the SDK. Use this to provide a [Plugin] that will be used to cause side effects
 * in certain API calls.
 */
public interface PluginFactory {
    /**
     * Gets [Plugin] if it was previously created or creates it and returns it if not.
     */
    public fun getOrCreate(): Plugin
}
