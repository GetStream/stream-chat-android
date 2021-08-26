package io.getstream.chat.android

object Configuration {
    const val compileSdkVersion = 30
    const val targetSdkVersion = 30
    const val minSdkVersion = 21
    const val majorVersion = 4
    const val minorVersion = 17
    const val pathVersion = 0
    const val versionName = "$majorVersion.$minorVersion.$pathVersion"
    const val snapshotVersionName = "$majorVersion.$minorVersion.${pathVersion + 1}-SNAPSHOT"
    const val artifactGroup = "io.getstream"
}
