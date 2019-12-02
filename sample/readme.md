# Sample Stream chat app
## Build
```bash
./gradlew sample:assembleDebug
```
## Run
```bash
adb install sample/build/outputs/apk/debug/*.apk
```
## Release app signing config (optional)
Define signing key environmental variables:
```bash
export STREAM_CHAT_RELEASE_SIGNING_STORE_FILE_PATH='...'
export STREAM_CHAT_RELEASE_SIGNING_STORE_PASSWORD='...'
export STREAM_CHAT_RELEASE_SIGNING_KEY_ALIAS='...'
export STREAM_CHAT_RELEASE_SIGNING_KEY_PASSWORD='...'
```
## Setup custom user and api key (optional)
To build and run sample with custom user define next environmental variables:
```bash
export API_KEY='...'
export STREAM_CHAT_USER_ID='...'
export STREAM_CHAT_USER_TOKEN='...'
export STREAM_CHAT_USER_NAME='...'
export STREAM_CHAT_USER_IMAGE='...'
```
User token is typically provided by your server when the user authenticates.
