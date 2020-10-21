package io.getstream.chat.android.client.models

public interface UserEntity {

    public var user: User

    public fun getUserId(): String {
        return user.id
    }
}
