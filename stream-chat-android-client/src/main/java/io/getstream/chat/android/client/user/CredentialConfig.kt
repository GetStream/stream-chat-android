package io.getstream.chat.android.client.user

/**
 * Data class that contain credential of current user.
 */
public class CredentialConfig(
    /**
     * Id of the current user.
     */
    public val userId: String,
    /**
     * Api token of the current user.
     */
    public val userToken: String,
    /**
     * Name of the current user.
     */
    public val userName: String,
) {
    internal fun isValid(): Boolean = userId.isNotEmpty() && userToken.isNotEmpty() && userName.isNotEmpty()
}
