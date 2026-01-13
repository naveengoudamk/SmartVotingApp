#!/bin/bash

# Auto-sync APK to Web Project
# This script copies the latest APK from the app build to the web public folder

echo "🚀 Starting APK sync to web project..."

# Define paths
APP_APK_PATH="/Users/naveennavi/Desktop/projects/SmartVotingApp/app/build/outputs/apk/release/app-release.apk"
APP_APK_UNSIGNED_PATH="/Users/naveennavi/Desktop/projects/SmartVotingApp/app/build/outputs/apk/release/app-release-unsigned.apk"
APP_APK_DEBUG_PATH="/Users/naveennavi/Desktop/projects/SmartVotingApp/app/build/outputs/apk/debug/app-debug.apk"
WEB_PUBLIC_PATH="/Users/naveennavi/Desktop/projects/SmartVotingApp/smartvotingweb/public/SmartVotingApp.apk"

# Check if APK exists (prioritize debug for valid signature in dev env)
if [ -f "$APP_APK_DEBUG_PATH" ]; then
    SOURCE_APK="$APP_APK_DEBUG_PATH"
    echo "✅ Found debug APK (Signed)"
elif [ -f "$APP_APK_PATH" ]; then
    SOURCE_APK="$APP_APK_PATH"
    echo "✅ Found signed release APK"
elif [ -f "$APP_APK_UNSIGNED_PATH" ]; then
    SOURCE_APK="$APP_APK_UNSIGNED_PATH"
    echo "⚠️  Found unsigned release APK (Device installation may fail)"
else
    echo "❌ Error: APK not found"
    echo "Tried:"
    echo "  - $APP_APK_DEBUG_PATH"
    echo "  - $APP_APK_PATH"
    echo "  - $APP_APK_UNSIGNED_PATH"
    echo "Please build the APK first using: ./gradlew assembleDebug"
    exit 1
fi

# Copy APK to web public folder
echo "📦 Copying APK to web project..."
cp "$SOURCE_APK" "$WEB_PUBLIC_PATH"

if [ $? -eq 0 ]; then
    echo "✅ APK successfully copied to web project!"
    
    # Get APK size
    APK_SIZE=$(du -h "$WEB_PUBLIC_PATH" | cut -f1)
    echo "📊 APK Size: $APK_SIZE"
    
    # Optional: Auto-commit and push to git
    # interactive logic removed for automation safely
    echo "✅ APK sync successful. Ready for deployment."
else
    echo "❌ Error: Failed to copy APK"
    exit 1
fi

echo "🎉 Sync complete!"
