/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.logging.android.file

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import io.getstream.logging.CompositeStreamLogger
import io.getstream.logging.StreamLog
import io.getstream.logging.android.AndroidStreamLogger
import io.getstream.logging.android.file.impl.LifecycleAwareLogFileManager
import io.getstream.logging.file.FileStreamLogger

/**
 * Initializes [StreamLog] at app startup time and represents a [FileProvider] for log file sharing.
 */
public class StreamLogFileProvider : FileProvider() {

    private val application: Application? get() = context as? Application

    /**
     * Called before [Application.onCreate].
     */
    override fun onCreate(): Boolean {

        val context = context ?: return super.onCreate()

        val fileLoggerConfig = FileStreamLogger.Config(
            filesDir = context.filesDir,
            externalFilesDir = context.getExternalFilesDir(null),
            app = FileStreamLogger.Config.App(
                versionCode = context.getVersionCode(),
                versionName = context.getVersionName()
            ),
            device = FileStreamLogger.Config.Device(
                model = "%s %s".format(Build.MANUFACTURER, Build.DEVICE),
                androidApiLevel = Build.VERSION.SDK_INT
            )
        )
        val fileLogger = FileStreamLogger(fileLoggerConfig)

        val androidLogger = AndroidStreamLogger()

        val compositeLogger = CompositeStreamLogger(androidLogger, fileLogger)
        val fileManager = LifecycleAwareLogFileManager(fileLogger)
        StreamLog.init(compositeLogger) { _, _ -> true }

        StreamLogFileManager.init(fileManager, fileManager)
        application?.registerActivityLifecycleCallbacks(fileManager)

        return super.onCreate()
    }

    private fun Context.getVersionName(): String = try {
        packageManager?.getPackageInfo(packageName, 0)?.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }

    private fun Context.getVersionCode(): Long = try {
        packageManager?.getPackageInfo(packageName, 0)?.getSupportVersionCode() ?: -1L
    } catch (e: PackageManager.NameNotFoundException) {
        -1L
    }

    private fun PackageInfo.getSupportVersionCode(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode
        else versionCode.toLong()
}
