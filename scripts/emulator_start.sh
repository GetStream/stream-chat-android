#!/bin/bash

if [ ! -d "$ANDROID_HOME" ] ; then
  echo "\$ANDROID_HOME is not set."
  exit
fi

if $ANDROID_HOME/platform-tools/adb devices -l | grep -q emulator; then
    echo "Emulator already running."
    exit 0
fi

echo "Starting emulator..."
echo "no" | $ANDROID_HOME/emulator/emulator "-avd" "Pixel_API_26" "-no-audio" "-no-boot-anim" "-gpu" "swiftshader_indirect" "-camera-back" "none" "-camera-front" "none" "-gpu" "host" &
echo "Emulator started."

echo "Waiting for emulator..."
while [ "$(adb shell getprop sys.boot_completed | tr -d '\r' )" != "1" ];
    do sleep 1;
done
echo "Emulator is ready."
