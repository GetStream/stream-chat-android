---
id: uiGlobalCustomizationsAvatarBitmapFactory
title: Avatar Bitmap Factory
sidebar_position: 6
---

It is possible to customize AvatarBitmapFactory so the avatars from the users will
be generated accordingly to the new configuration. It is possible to configure
the user bitmap, user default bitmap, channel bitmap, channel default bitmap, also choose
between blocking and non blocking options and configure the keys for easy bitmap to be used
in the cache system.

To change the default behaviour of this factory, a user needs to extend `AvatarBitmapFactory`,
which is an open class, and set the desired behaviour. As the example:

```kotlin
val factory: AvatarBitmapFactory = object: AvatarBitmapFactory(requireContext()) {
    override suspend fun createUserBitmap(
        user: User,
        style: AvatarStyle,
        avatarSize: Int,
    ): Bitmap? {
        //Return your version of bitmap here!
        return super.createUserBitmap(user, style, avatarSize)
    }
}

ChatUI.avatarBitmapFactory = factory
```
