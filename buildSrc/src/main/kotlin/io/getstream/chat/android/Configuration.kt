package io.getstream.chat.android

object Configuration {
    const val compileSdk = 30
    const val targetSdk = 30
    const val minSdk = 21
    const val majorVersion = 4
    const val minorVersion = 17
    const val patchVersion = 3
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "io.getstream"
}
