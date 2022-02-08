package io.getstream.chat.android.client.experimental.plugin.factory

import io.getstream.chat.android.client.experimental.plugin.Plugin

public interface PluginFactory {
    public fun getOrCreate(): Plugin
}
