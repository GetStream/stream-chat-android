package io.getstream.chat.android.client.user.storage

import io.getstream.chat.android.client.user.CredentialConfig

/**
 * Storage for [CredentialConfig].
 * SDK needs to store user credentials to restore SDK with user connected state. It is required for push notifications
 * for example. When a device receives push notification app with SDK might be killed or not run completely. SDK handles
 * it and restore state using data from [CredentialConfig].
 */
public interface UserCredentialStorage {
    /**
     * Save [credentialConfig] to this storage.
     */
    public fun put(credentialConfig: CredentialConfig)

    /**
     * Obtain [CredentialConfig] if it was stored before.
     */
    public fun get(): CredentialConfig?

    /**
     * Clear current storage.
     */
    public fun clear()
}
