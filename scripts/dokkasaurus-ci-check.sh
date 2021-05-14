set -e

./gradlew dokkaHtmlMultiModule
python temp/ci.py \
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

diff_output=$(git status)

if [[ $diff_output != *"nothing to commit, working tree clean"* ]]; then
  echo "Error! looks like there are changes in your documentation"
  exit 1
fi
