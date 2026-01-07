# App Update System

This document explains how the automatic app update system works for the Smart Voting App.

## Overview

The update system consists of two main components:
1. **In-App Update Checker** - Checks for updates when the app starts
2. **APK Sync Script** - Automatically syncs the latest APK to the web project

## How It Works

### 1. In-App Update Checker

The `AppUpdateChecker` class checks Firebase for the latest app version and compares it with the current installed version.

**Firebase Structure:**
```
app_version/
  ├── version_code: 2
  ├── version_name: "2.0"
  ├── update_message: "New features and bug fixes available!"
  ├── force_update: false
  └── last_updated: 1234567890
```

**When an update is available:**
- A dialog is shown to the user
- User can click "Update Now" to be redirected to the web download page
- If `force_update` is true, the user must update to continue using the app

### 2. APK Sync to Web

After building a new APK, run the sync script to automatically copy it to the web project:

```bash
./sync-apk.sh
```

This script:
- Copies the latest release APK to `smartvotingweb/public/SmartVotingApp.apk`
- Shows the APK size
- Optionally commits and pushes to GitHub

## Step-by-Step Update Process

### Step 1: Update Version in build.gradle

Edit `app/build.gradle`:
```gradle
defaultConfig {
    versionCode 2        // Increment this
    versionName "2.0"    // Update version name
}
```

### Step 2: Build Release APK

```bash
cd /Users/naveennavi/Desktop/projects/SmartVotingApp
./gradlew assembleRelease
```

### Step 3: Sync APK to Web

```bash
./sync-apk.sh
```

### Step 4: Update Firebase Version Info

You have two options:

**Option A: Using Firebase Console**
1. Go to Firebase Console → Realtime Database
2. Navigate to `app_version` node
3. Update the values:
   - `version_code`: 2
   - `version_name`: "2.0"
   - `update_message`: "Your update message here"
   - `force_update`: true/false

**Option B: Programmatically (Admin Only)**
```java
AppUpdateChecker.setLatestVersion(
    2,                                      // version code
    "2.0",                                  // version name
    "New features and bug fixes!",          // update message
    false                                   // force update
);
```

### Step 5: Deploy Web Changes

```bash
cd smartvotingweb
git add .
git commit -m "Update APK to version 2.0"
git push
```

## Testing the Update System

1. Install the old version of the app (v1.0)
2. Update Firebase with version 2
3. Open the app
4. You should see the update dialog
5. Click "Update Now" to be redirected to the download page

## Configuration

### Change Download URL

Edit `AppUpdateChecker.java`:
```java
private static final String DOWNLOAD_URL = "https://your-website.com/";
```

### Disable Update Checks

Comment out the update check in `MainActivity.java`:
```java
// AppUpdateChecker updateChecker = new AppUpdateChecker(this);
// updateChecker.checkForUpdate();
```

## Firebase Rules

Ensure your Firebase Realtime Database rules allow read access to `app_version`:

```json
{
  "rules": {
    "app_version": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

## Troubleshooting

**Update dialog not showing:**
- Check Firebase connection
- Verify `app_version` node exists in Firebase
- Check logs for errors: `adb logcat | grep AppUpdateChecker`

**APK not copying:**
- Ensure the release APK is built first
- Check file paths in `sync-apk.sh`
- Verify you have write permissions

## Best Practices

1. **Always increment versionCode** when releasing updates
2. **Use semantic versioning** for versionName (e.g., 1.0, 1.1, 2.0)
3. **Test updates** on a device before releasing
4. **Use force_update sparingly** - only for critical security updates
5. **Write clear update messages** explaining what's new
6. **Keep APK size optimized** - users download it over mobile data

## Automation Ideas

For future improvements, you could:
- Set up GitHub Actions to auto-build and deploy APK
- Create a web admin panel to manage version updates
- Add analytics to track update adoption rates
- Implement delta updates to reduce download size
