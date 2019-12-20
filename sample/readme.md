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
## Setup custom user and api properties (optional)
To build and run sample with custom api key, endpoint and user(s) add json config `/sample/app-config-custom.json`:
```json
{
  "api_key": "qk4nn7rpcn75",
  "api_endpoint": "chat-us-east-1.stream-io-api.com",
  "api_timeout": 6000,
  "cdn_timeout": 30000,
  "users": [
    {
      "id": "bender",
      "name": "Bender",
      "image": "https://bit.ly/321RmWb",
      "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"
    },
    {
      "id": "broken-waterfall-5",
      "name": "Jon Snow",
      "image": "https://bit.ly/2u9Vc0r",
      "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg"
    },
    {
      "id": "steep-moon-9",
      "name": "Steep moon",
      "image": "https://i.imgur.com/EgEPqWZ.jpg",
      "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoic3RlZXAtbW9vbi05In0.K7uZEqKmiVb5_Y7XFCmlz64SzOV34hoMpeqRSz7g4YI"
    }
  ]
}
```
User token is typically provided by your server when the user authenticates.
Then in the sample app these values are passed as `BuildConfig` values:
```java
BuildConfig.USERS_CONFIG
```