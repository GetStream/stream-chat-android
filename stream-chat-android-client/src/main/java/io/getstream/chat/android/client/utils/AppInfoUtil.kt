/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.utils

import android.content.Context
import android.os.Build

/**
 * Utility class for retrieving application-related information.
 *
 * This class provides methods to fetch:
 * - Application name
 * - Application version name and version code
 * - Installer package name
 *
 * @param context The application context used to retrieve package-related data.
 */
public class AppInfoUtil(public var context: Context) {

    /**
     * Retrieves the version name of the application.
     *
     * @return The version name (e.g., "1.2.3") or `"nameNotFound"` if retrieval fails.
     */
    public fun getAppVersionName(): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "nameNotFound"
    }

    /**
     * Retrieves the version code of the application.
     *
     * - On **Android P (API 28) and above**, it returns `longVersionCode` as a string.
     * - On **older Android versions**, it returns `versionCode` as a string.
     *
     * @return The version code as a string or `"versionCodeNotFound"` if retrieval fails.
     */
    public fun getAppVersionCode(): String {
        return runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                packageInfo.versionCode.toString()
            }
        }.getOrNull() ?: "versionCodeNotFound"
    }

    /**
     * Retrieves the application's name as displayed in the launcher.
     *
     * - If the application label is not available, it falls back to `nonLocalizedLabel`.
     * - If both are unavailable, it returns `"UnknownApp"`.
     *
     * @return The application name or `"UnknownApp"` if retrieval fails.
     */
    public fun getAppName(): String {
        val applicationInfo = context.applicationInfo
        return if (applicationInfo != null) {
            val stringId = applicationInfo.labelRes
            if (stringId == 0) {
                applicationInfo.nonLocalizedLabel?.toString() ?: "UnknownApp"
            } else {
                context.getString(stringId) ?: "UnknownApp"
            }
        } else {
            "UnknownApp"
        }
    }

    /**
     * Retrieves the installer package name (i.e., the source from which the app was installed).
     *
     * - On **Android R (API 30) and above**, it uses `getInstallSourceInfo()`.
     * - On **older Android versions**, it uses `getInstallerPackageName()`.
     * - If the app was sideloaded or the installer is unknown, it returns `"StandAloneInstall"`.
     *
     * @return The installer package name or `"StandAloneInstall"` if unknown.
     */
    public fun getInstallerName(): String {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        }.getOrNull() ?: "StandAloneInstall"
    }
}
