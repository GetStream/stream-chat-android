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
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.ChatClient.Companion.OFFLINE_SUPPORT_ENABLED
import io.getstream.chat.android.client.ChatClient.Companion.VERSION_PREFIX_HEADER
import java.text.Normalizer

/**
 * Utility class for retrieving application-related information.
 *
 * This class provides methods to fetch:
 * - Application name
 * - Application version name and version code
 * - Installer package name
 *
 * @param context The application context used to retrieve package-related data.
 * @param appName The application name which is using the Chat SDK
 * @param appVersion The application version which is using the Chat SDK. Eg: 1.0.0
 */
internal class HeadersUtil(var context: Context, private var appName: String?, private var appVersion: String?) {

    /**
     * Retrieves the version name of the application.
     *
     * @return The version name (e.g., "1.2.3") or `"nameNotFound"` if retrieval fails.
     */
    private fun getAppVersionName(): String {
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
    private fun getAppVersionCode(): String {
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
    private fun getAppName(): String {
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
    private fun getInstallerName(): String {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        }.getOrNull() ?: "StandAloneInstall"
    }

    /**
     * Builds a detailed header of information we track around the SDK, Android OS, API Level, device name and
     * vendor and more.
     *
     * @return String formatted header that contains all the information.
     */
    internal fun buildSdkTrackingHeaders(): String {
        val clientInformation = VERSION_PREFIX_HEADER.prefix + BuildConfig.STREAM_CHAT_VERSION
        val buildModel = Build.MODEL
        val deviceManufacturer = Build.MANUFACTURER
        val apiLevel = Build.VERSION.SDK_INT
        val osName = "Android ${Build.VERSION.RELEASE}"
        val appNameValue = appName ?: getAppName()
        val appVersionValue = appVersion ?: getAppVersionName()

        return buildString {
            append(clientInformation)
            append("|os=$osName")
            append("|api_version=$apiLevel")
            append("|device_model=$deviceManufacturer $buildModel")
            append("|offline_enabled=$OFFLINE_SUPPORT_ENABLED")
            append("|app=$appNameValue")
            append("|app_version=$appVersionValue")
        }.sanitize()
    }

    /**
     * Builds a User-Agent string containing app and device information.
     *
     * The User-Agent string follows this format:
     * ```
     * AppName / VersionName(VersionCode); InstallerName; (Manufacturer; Model; SDK Version; Android Version)
     * ```
     *
     * Example output:
     * ```
     * MyApp / 1.2.3(123); Google Play Store; (Samsung; Galaxy S21; SDK 30; Android 11)
     * ```
     *
     * @return A sanitized string containing app version, device manufacturer, model, OS version, and installer
     * information.
     */
    fun buildUserAgent(): String {
        with(context.packageManager) {
            val versionName = appVersion ?: getAppVersionName()
            val versionCode = getAppVersionCode()
            val appName = appName ?: getAppName()

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            val installerName = getInstallerName()

            return (
                "$appName / $versionName($versionCode); $installerName; ($manufacturer; " +
                    "$model; SDK $version; Android $versionRelease)"
                )
                .sanitize()
        }
    }

    private fun String.sanitize(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")
    }
}
