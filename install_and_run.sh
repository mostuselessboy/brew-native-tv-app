#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status, except in conditional statements
set -e

# Configuration
PACKAGE_NAME="com.google.jetstream"
ACTIVITY_NAME=".MainActivity"
APK_PATH="jetstream/build/outputs/apk/debug/jetstream-debug.apk"

# Initialize variables
BUILD_APP=true
DEVICE_ID=""

# Parse arguments
for arg in "$@"; do
    if [ "$arg" = "--no-build" ] || [ "$arg" = "-n" ]; then
        BUILD_APP=false
    else
        DEVICE_ID="$arg"
    fi
done

# Locate ADB
if command -v adb >/dev/null 2>&1; then
    ADB_CMD="adb"
elif [ -n "$ANDROID_HOME" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
    ADB_CMD="$ANDROID_HOME/platform-tools/adb"
elif [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB_CMD="$HOME/Library/Android/sdk/platform-tools/adb"
else
    echo "❌ Error: 'adb' command not found."
    echo "Please add 'adb' to your PATH or set the ANDROID_HOME environment variable."
    exit 1
fi

echo "🔍 Locating Android devices..."
# Get list of active devices (filter by 'device' state to skip offline/unauthorized)
devices=($($ADB_CMD devices | awk 'NR>1 && $2=="device" {print $1}'))

if [ ${#devices[@]} -eq 0 ]; then
    echo "❌ Error: No active Android devices/emulators connected via ADB."
    echo "Please connect a device or start an emulator, ensure USB debugging is enabled, and try again."
    exit 1
fi

# Select device
if [ -n "$DEVICE_ID" ]; then
    # Verify the specified device is connected
    found=false
    for dev in "${devices[@]}"; do
        if [ "$dev" = "$DEVICE_ID" ]; then
            found=true
            break
        fi
    done
    if [ "$found" = false ]; then
        echo "⚠️ Warning: Specified device '$DEVICE_ID' not found in active device list."
        echo "Will attempt to use it anyway..."
    else
        echo "📱 Using specified device: $DEVICE_ID"
    fi
else
    if [ ${#devices[@]} -eq 1 ]; then
        DEVICE_ID=${devices[0]}
        echo "📱 Using connected device: $DEVICE_ID"
    else
        echo "Multiple Android devices detected:"
        for i in "${!devices[@]}"; do
            dev_id=${devices[$i]}
            # Try to get the product model (timeout/silently handle failure)
            model=$($ADB_CMD -s "$dev_id" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
            if [ -n "$model" ]; then
                echo "  [$i] $dev_id ($model)"
            else
                echo "  [$i] $dev_id"
            fi
        done
        
        # Read user selection
        read -p "Select a device [0-$((${#devices[@]} - 1))]: " choice
        if [[ "$choice" =~ ^[0-9]+$ ]] && [ "$choice" -ge 0 ] && [ "$choice" -lt ${#devices[@]} ]; then
            DEVICE_ID=${devices[$choice]}
            echo "📱 Selected device: $DEVICE_ID"
        else
            echo "❌ Invalid selection. Exiting."
            exit 1
        fi
    fi
fi

# Build app if requested
if [ "$BUILD_APP" = true ]; then
    echo "🔨 Building the application..."
    chmod +x ./gradlew
    ./gradlew :jetstream:assembleDebug
else
    echo "⏭️ Skipping build as requested..."
fi

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ Error: APK not found at $APK_PATH"
    if [ "$BUILD_APP" = false ]; then
        echo "Please run without the --no-build/-n flag to compile it first."
    fi
    exit 1
fi

# Install APK
echo "📥 Installing the APK onto device '$DEVICE_ID'..."
$ADB_CMD -s "$DEVICE_ID" install -r "$APK_PATH"

# Run App
echo "🚀 Launching $PACKAGE_NAME/$ACTIVITY_NAME on '$DEVICE_ID'..."
$ADB_CMD -s "$DEVICE_ID" shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME"

echo "🎉 Done! The app should be running now on your device."
