#!/bin/bash

if [ ! -d "$ANDROID_HOME" ] ; then
  echo "\$ANDROID_HOME is not set."
  exit
fi

if [ ! -d "$ANDROID_HOME/cmdline-tools" ]; then
  echo "Install Command-line Tools via SDK Manager > SDK Tools."
  exit
fi

if $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager list avd | grep -q Pixel_API_26; then
    echo "Emulator is already created."
    exit 0;
fi

if [[ $(uname -m) == 'arm64' ]]; then
  ARCHITECTURE='arm64-v8a'
else
  ARCHITECTURE=$(uname -m)
fi

echo "Downloading emulator image..."
echo no | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "system-images;android-26;google_apis;$ARCHITECTURE"
echo "Image downloaded!"

echo "Creating emulator..."
echo no | $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd --name "Pixel_API_26" --package "system-images;android-26;google_apis;$ARCHITECTURE" --force --device "pixel" --sdcard "512M"
echo "Emulator created!"
