package io.getstream.chat.android.client.experimental.plugin.factory

import io.getstream.chat.android.client.experimental.plugin.Plugin

/**
 * Interface used to add new plugins to the SDK. Use this to provide a [Plugin] that will be used to cause side effects
 * in certain API calls.
 */
public interface PluginFactory {
    public fun getOrCreate(): Plugin
}
