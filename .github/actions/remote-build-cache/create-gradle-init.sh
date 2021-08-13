mkdir -p $HOME/.gradle/init.d

echo "gradle.settingsEvaluated { settings ->
        settings.buildCache {
          remote(HttpBuildCache) {
            url = '$BUILD_CACHE_URL'
            push = true
            allowInsecureProtocol = true
            credentials {
              username = '$BUILD_CACHE_USER'
              password = '$BUILD_CACHE_PASSWORD'
            }
          }
        }
      }" > $HOME/.gradle/init.d/remoteBuildCache.gradle