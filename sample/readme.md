# Sample Stream chat app
## Setup (optional)
To build and run sample with custom user define next environmental variables:
```bash
export API_KEY='...'
export STREAM_CHAT_USER_ID='...'
export STREAM_CHAT_USER_TOKEN='...'
export STREAM_CHAT_USER_NAME='...'
export STREAM_CHAT_USER_IMAGE='...'
```
User token is typically provided by your server when the user authenticates.
## Build
```bash
./gradlew assembleRelease
./gradlew assembleDebug
```