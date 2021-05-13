./gradlew dokkaHtmlMultiModule
dokkasaurus build/dokka/htmlMultiModule Dokka --root-position 5 --auto-generated --multi-module --filter-file dokkasaurus_filter.json

mkdir documentation/docs/dokka
mv ./build/dokka/htmlMultiModule/_category_.json documentation/docs/dokka/_category_.json
mv ./build/dokka/htmlMultiModule/stream-chat-android-client documentation/docs/dokka
mv ./build/dokka/htmlMultiModule/stream-chat-android-offline documentation/docs/dokka
mv ./build/dokka/htmlMultiModule/stream-chat-android-ui-components documentation/docs/dokka
