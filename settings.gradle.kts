import java.util.Properties

pluginManagement {
	repositories {
		// fetch plugins from google maven (https://maven.google.com)
		google {
			content {
				includeGroupByRegex("androidx\\..*")
				includeGroupByRegex("com\\.android(\\..*|)")
				includeGroupByRegex("com\\.google\\.android\\..*")
				includeGroupByRegex("com\\.google\\.firebase(\\..*|)")
				includeGroupByRegex("com\\.google\\.gms(\\..*|)")
				includeGroupByRegex("com\\.google\\.mlkit")
				includeGroupByRegex("com\\.google\\.oboe")
				includeGroupByRegex("com\\.google\\.prefab")
				includeGroupByRegex("com\\.google\\.testing\\.platform")
			}
			mavenContent {
				releasesOnly()
			}
		}

		maven(url = "https://jitpack.io/") {
			content {
				includeModule("com.github.passsy", "gradle-gitVersioner-plugin")
			}
		}

		// fetch plugins from gradle plugin portal (https://plugins.gradle.org)
		gradlePluginPortal()

		// Fallback for the rest of the dependencies
		mavenCentral()
	}
	resolutionStrategy {
		eachPlugin {
			when(requested.id.id) {
				"shot" -> { useModule("com.karumi:shot:${requested.version}") }
				"com.pascalwelsch.gitversioner" -> { useModule("com.github.passsy:gradle-gitVersioner-plugin:${requested.version}") }
			}
		}
	}
}

plugins {
	id("com.github.burrunan.s3-build-cache") version "1.8.4"
	id("com.gradle.enterprise") version "3.7"
}
include (
		":stream-chat-android-ui-utils",
		":stream-chat-android-ui-common",
		":stream-chat-android-ui-components",
		":stream-chat-android-ui-components-sample",
		":stream-chat-android-ui-guides",
		":stream-chat-android-state",
		":stream-chat-android-offline",
		":stream-chat-android-client",
		":stream-chat-android-client-test",
		":stream-chat-android-core",
		":stream-chat-android-e2e-test",
		":stream-chat-android-test",
		":stream-chat-android-docs",
		":stream-chat-android-previewdata",
		":stream-chat-android-compose",
		":stream-chat-android-compose-sample",
		":stream-chat-android-markdown-transformer",
		":stream-chat-android-ui-uitests",
		":stream-chat-android-benchmark",
		":stream-chat-android-ai-assistant",
		":metrics:stream-chat-android-metrics",
)

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		// fetch plugins from google maven (https://maven.google.com)
		google() {
			content {
				includeGroupByRegex("androidx\\..*")
				includeGroupByRegex("com\\.android(\\..*|)")
				includeGroupByRegex("com\\.google\\.android\\..*")
				includeGroupByRegex("com\\.google\\.firebase(\\..*|)")
				includeGroupByRegex("com\\.google\\.gms(\\..*|)")
				includeGroupByRegex("com\\.google\\.mlkit")
				includeGroupByRegex("com\\.google\\.oboe")
				includeGroupByRegex("com\\.google\\.prefab")
				includeGroupByRegex("com\\.google\\.testing\\.platform")
			}
			mavenContent {
				releasesOnly()
			}
		}

		maven(url = "https://jitpack.io") {
			content {
				includeModule("com.github.jeziellago", "compose-markdown")
				includeModule("com.github.jeziellago", "Markwon")
			}
		}

		maven(url = "https://developer.huawei.com/repo/") {
			content {
				includeGroup("com.huawei.agconnect")
				includeGroup("com.huawei.android.hms")
				includeGroup("com.huawei.hms")
				includeGroup("com.huawei.hmf")
			}
		}

		// Fallback for the rest of the dependencies
		mavenCentral()
	}
}

buildCache {
	local {
		isEnabled = !System.getenv().containsKey("CI")
	}
	val localProperties = Properties()
	val file = File("local.properties")
	if (file.exists()) {
		file.inputStream().use { localProperties.load(it) }
	}
	val streamAWSRegion: String = localProperties.get("buildCache.AWSRegion") as? String ?: System.getenv("BUILD_CACHE_AWS_REGION") ?: ""
	val streamAWSBucket: String = localProperties.get("buildCache.AWSBucket") as? String ?: System.getenv("BUILD_CACHE_AWS_BUCKET") ?: ""
	val streamAWSAccessKeyId: String = localProperties.get("buildCache.AWSAccessKeyId") as? String ?: System.getenv("BUILD_CACHE_AWS_ACCESS_KEY_ID") ?: ""
	val streamAWSSecretKey: String = localProperties.get("buildCache.AWSSecretKey") as? String ?: System.getenv("BUILD_CACHE_AWS_SECRET_KEY") ?: ""
	val streamBuildCacheDisabled: Boolean = localProperties.get("buildCache.disabled") == true
	if (!streamBuildCacheDisabled &&
			!streamAWSRegion.isBlank() &&
			!streamAWSBucket.isBlank() &&
			!streamAWSAccessKeyId.isBlank() &&
			!streamAWSSecretKey.isBlank()
	) {
		remote<com.github.burrunan.s3cache.AwsS3BuildCache> {
			region = streamAWSRegion
			bucket = streamAWSBucket
			prefix = "cache/"
			awsAccessKeyId = streamAWSAccessKeyId
			awsSecretKey = streamAWSSecretKey
			isPush = System.getenv().containsKey("CI")

		}
	}
}

gradleEnterprise {
	buildScan {
		termsOfServiceUrl = "https://gradle.com/terms-of-service"
		termsOfServiceAgree = "yes"
	}
}
