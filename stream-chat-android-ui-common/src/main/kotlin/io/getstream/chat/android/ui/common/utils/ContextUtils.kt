package io.getstream.chat.android.ui.common.utils

import android.content.Context
import android.content.pm.PackageManager
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Returns if we need to check for the given permission or not.
 *
 * @param permission The permission to check.
 * @return If the given permission is declared in the manifest or not.
 */
@InternalStreamChatApi
public fun Context.isPermissionDeclared(permission: String): Boolean {
    return packageManager
        .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions
        .contains(permission)
}