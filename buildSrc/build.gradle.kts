plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("UnitTestsPlugin") {
            id = "io.getstream.chat.UnitTestsPlugin"
            implementationClass = "io.getstream.chat.android.command.unittest.plugin.UnitTestsPlugin"
            version = "1.0.0"
        }
        create("ReleasePlugin") {
            id = "io.getstream.chat.ReleasePlugin"
            implementationClass = "io.getstream.chat.android.command.release.plugin.ReleasePlugin"
            version = "1.0.0"
        }
        create("ChangelogReleaseSectionPlugin") {
            id = "io.getstream.chat.ChangelogReleaseSectionPlugin"
            implementationClass = "io.getstream.chat.android.command.changelog.plugin.ChangelogReleaseSectionPlugin"
            version = "1.0.0"
        }
        create("VersionBumpPlugin") {
            id = "io.getstream.chat.VersionBumpPlugin"
            implementationClass = "io.getstream.chat.android.command.version.plugin.VersionBumpPlugin"
            version = "1.0.0"
        }
        create("MinorBumpPlugin") {
            id = "io.getstream.chat.MinorBumpPlugin"
            implementationClass = "io.getstream.chat.android.command.version.plugin.MinorBumpPlugin"
            version = "1.0.0"
        }
        create("VersionPrintPlugin") {
            id = "io.getstream.chat.VersionPrintPlugin"
            implementationClass = "io.getstream.chat.android.command.version.plugin.VersionPrintPlugin"
            version = "1.0.0"
        }
        create("ChangelogAddModelSectionPlugin") {
            id = "io.getstream.chat.ChangelogAddModelSectionPlugin"
            implementationClass = "io.getstream.chat.android.command.changelog.plugin.ChangelogAddModelSectionPlugin"
            version = "1.0.0"
        }
    }
}

dependencies {
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
}
