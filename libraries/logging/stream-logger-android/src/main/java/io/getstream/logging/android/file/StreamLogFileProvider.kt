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

public class StreamLogFileProvider : FileProvider() {

    private val application: Application? get() = context as? Application

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
        StreamLog.init(compositeLogger)

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
