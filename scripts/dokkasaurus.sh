./gradlew dokkaHtmlMultiModule
git clone git@github.com:GetStream/DokkasaurusSidebar.git
python DokkasaurusSidebar/ci.py \
./build/dokka/htmlMultiModule \
Dokka \
 5 \
 ./ \
 True \
 True \
./dokkasaurus_filter.json

mkdir documentation/docs/dokka
mv ./build/dokka/htmlMultiModule/_category_.json documentation/docs/dokka/_category_.json
mv ./build/dokka/htmlMultiModule/stream-chat-android-client documentation/docs/dokka
mv ./build/dokka/htmlMultiModule/stream-chat-android-offline documentation/docs/dokka
mv ./build/dokka/htmlMultiModule/stream-chat-android-ui-components documentation/docs/dokka
