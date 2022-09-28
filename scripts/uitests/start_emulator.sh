#!/bin/bash

if [ ! -d "$ANDROID_HOME" ] ; then
  echo "\$ANDROID_HOME is not set."
  exit
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager list avd | grep -q Pixel_API_26; then
    echo "No emulator for screenshot tests found, creating one..."
    $DIR/create_emulator.sh
fi

if $ANDROID_HOME/platform-tools/adb devices -l | grep -q emulator; then
    echo "Emulator already running"
    exit 0
fi

echo "Starting emulator..."
echo "no" | $ANDROID_HOME/emulator/emulator "-avd" "Pixel_API_26" "-no-window" "-no-audio" "-no-boot-anim" "-gpu" "swiftshader_indirect" "-camera-back" "none" "-camera-front" "none" &
echo "Emulator started!"

echo "Waiting for emulator..."
$DIR/wait_for_emulator.sh
echo "Emulator is ready!"

echo "Disabling animations..."
$DIR/disable_animations.sh
echo "Animations disabled!"