package io.getstream.chat.android.client.user.storage

import io.getstream.chat.android.client.user.CredentialConfig

/**
 * Storage for [CredentialConfig].
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
