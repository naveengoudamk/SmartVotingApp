#!/bin/bash

# Auto-sync APK to Web Project
# This script copies the latest APK from the app build to the web public folder

echo "🚀 Starting APK sync to web project..."

# Define paths
APP_APK_PATH="/Users/naveennavi/Desktop/projects/SmartVotingApp/app/build/outputs/apk/release/app-release.apk"
WEB_PUBLIC_PATH="/Users/naveennavi/Desktop/projects/SmartVotingApp/smartvotingweb/public/SmartVotingApp.apk"

# Check if APK exists
if [ ! -f "$APP_APK_PATH" ]; then
    echo "❌ Error: APK not found at $APP_APK_PATH"
    echo "Please build the release APK first using: ./gradlew assembleRelease"
    exit 1
fi

# Copy APK to web public folder
echo "📦 Copying APK to web project..."
cp "$APP_APK_PATH" "$WEB_PUBLIC_PATH"

if [ $? -eq 0 ]; then
    echo "✅ APK successfully copied to web project!"
    
    # Get APK size
    APK_SIZE=$(du -h "$WEB_PUBLIC_PATH" | cut -f1)
    echo "📊 APK Size: $APK_SIZE"
    
    # Optional: Auto-commit and push to git
    read -p "Do you want to commit and push to GitHub? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd /Users/naveennavi/Desktop/projects/SmartVotingApp/smartvotingweb
        git add public/SmartVotingApp.apk
        git commit -m "Update APK to latest version"
        git push
        echo "✅ Changes pushed to GitHub!"
    fi
else
    echo "❌ Error: Failed to copy APK"
    exit 1
fi

echo "🎉 Sync complete!"
