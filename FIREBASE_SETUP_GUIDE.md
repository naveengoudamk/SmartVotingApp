# Firebase Setup Guide for App Update System

## 📋 Step-by-Step Firebase Configuration

Follow these steps to set up the update system in Firebase Realtime Database.

---

## Step 1: Access Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Sign in with your Google account
3. Select your **SmartVotingApp** project
   - If you don't have a project, create one first

---

## Step 2: Open Realtime Database

1. In the left sidebar, click on **"Build"**
2. Click on **"Realtime Database"**
3. If not already created, click **"Create Database"**
   - Choose location (e.g., `us-central1`)
   - Start in **"Test mode"** for now (we'll secure it later)
   - Click **"Enable"**

---

## Step 3: Add App Version Data

### Method 1: Using Firebase Console (Recommended)

1. In Realtime Database, you'll see the root node
2. Click the **"+"** button next to the database URL
3. Add a new node called **`app_version`**

4. Inside `app_version`, add these fields:

   **Click "+" to add each field:**

   | Field Name | Type | Value | Description |
   |------------|------|-------|-------------|
   | `version_code` | Number | `1` | Current app version (integer) |
   | `version_name` | String | `"1.0"` | Display version |
   | `update_message` | String | `"App is up to date"` | Message to show users |
   | `force_update` | Boolean | `false` | Whether update is mandatory |
   | `last_updated` | Number | `1704628752000` | Timestamp (optional) |

5. Click **"Add"** after each field

### Your Firebase structure should look like this:

```
smartvotingapp-xxxxx (your database)
  └── app_version
       ├── version_code: 1
       ├── version_name: "1.0"
       ├── update_message: "App is up to date"
       ├── force_update: false
       └── last_updated: 1704628752000
```

---

## Step 4: Test the Setup

### Test 1: No Update Required

**Current Setup:**
- Firebase: `version_code: 1`
- App: `versionCode: 1` (in build.gradle)

**Expected Result:**
- Open app
- Login screen loads
- **No update dialog**
- Can login normally ✅

### Test 2: Update Required

**Change Firebase:**
1. Click on `version_code` in Firebase
2. Change value to **`2`**
3. Click on `version_name`
4. Change value to **`"2.0"`**
5. Click on `update_message`
6. Change value to **`"New features available! Please update."`**

**Expected Result:**
- Open app (with old version 1.0)
- Login screen loads
- **Update dialog appears** ✅
- Shows: "New Version: 2.0, Current Version: 1.0"
- Two buttons: "Update Now" and "Go Back"

---

## Step 5: Secure Your Database (Important!)

### Set Database Rules

1. In Realtime Database, click on **"Rules"** tab
2. Replace the rules with:

```json
{
  "rules": {
    "app_version": {
      ".read": true,
      ".write": "auth != null"
    },
    "feedback": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "elections": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

3. Click **"Publish"**

**What these rules do:**
- `app_version`: Anyone can **read** (check for updates), only authenticated users can **write**
- `feedback`: Only authenticated users can read/write
- `elections`: Anyone can read, only authenticated users can write

---

## Step 6: When You Release a New Version

### Update Process:

1. **Build new APK** with updated version:
   ```gradle
   // In app/build.gradle
   defaultConfig {
       versionCode 2        // Increment this
       versionName "2.0"    // Update this
   }
   ```

2. **Update Firebase**:
   - Go to Firebase Console → Realtime Database
   - Click on `app_version` → `version_code`
   - Change to **`2`**
   - Click on `version_name`
   - Change to **`"2.0"`**
   - Click on `update_message`
   - Change to your message (e.g., `"🎉 New features: Dark mode, Better UI, Bug fixes!"`)

3. **Upload APK to web**:
   - Use the sync script: `./sync-apk.sh`
   - Or manually copy to `smartvotingweb/public/SmartVotingApp.apk`
   - Push to GitHub
   - Deploy to Vercel

4. **Users with old app**:
   - Open app
   - See update dialog
   - Click "Update Now"
   - Download new APK
   - Install manually

---

## Step 7: Firebase Data Examples

### Example 1: Normal Update

```json
{
  "app_version": {
    "version_code": 2,
    "version_name": "2.0",
    "update_message": "New features and improvements available!",
    "force_update": true,
    "last_updated": 1704628752000
  }
}
```

### Example 2: Critical Security Update

```json
{
  "app_version": {
    "version_code": 3,
    "version_name": "2.1",
    "update_message": "🔒 Critical security update! Please update immediately for your safety.",
    "force_update": true,
    "last_updated": 1704715152000
  }
}
```

### Example 3: Feature Update

```json
{
  "app_version": {
    "version_code": 4,
    "version_name": "3.0",
    "update_message": "🎉 Major update!\n\n✨ New Features:\n• Dark Mode\n• Improved UI\n• Faster performance\n• Bug fixes",
    "force_update": true,
    "last_updated": 1704801552000
  }
}
```

---

## Quick Reference

### Firebase Console URLs

- **Main Console**: https://console.firebase.google.com/
- **Realtime Database**: https://console.firebase.google.com/project/YOUR_PROJECT_ID/database

### Version Management

| Action | Firebase | App (build.gradle) |
|--------|----------|-------------------|
| Initial Release | `version_code: 1` | `versionCode 1` |
| First Update | `version_code: 2` | `versionCode 2` |
| Second Update | `version_code: 3` | `versionCode 3` |

### Update Message Tips

✅ **Good Messages:**
- "🎉 New features available! Update now to enjoy the latest version."
- "🔒 Security update available. Please update for better protection."
- "✨ Improved performance and bug fixes. Update recommended!"

❌ **Avoid:**
- "Update available" (too vague)
- Long technical descriptions
- No emoji or formatting

---

## Troubleshooting

### Issue: Update dialog not showing

**Check:**
1. ✅ Firebase `version_code` is higher than app `versionCode`
2. ✅ Internet connection is working
3. ✅ Firebase rules allow reading `app_version`
4. ✅ App has Firebase configured correctly

**Solution:**
- Check logs: `adb logcat | grep LoginActivity`
- Verify Firebase connection
- Check `google-services.json` is in `app/` folder

### Issue: Can't update Firebase

**Check:**
1. ✅ You're signed in to Firebase Console
2. ✅ You have write permissions
3. ✅ Database rules allow writing

**Solution:**
- Sign in with project owner account
- Update database rules to allow writing

---

## Summary

### What You Need to Do in Firebase:

1. ✅ **Create/Open** Realtime Database
2. ✅ **Add** `app_version` node
3. ✅ **Set** initial values:
   - `version_code: 1`
   - `version_name: "1.0"`
   - `update_message: "App is up to date"`
   - `force_update: false`
4. ✅ **Secure** with database rules
5. ✅ **Update** version numbers when releasing new APK

### When Releasing Updates:

1. ✅ Increment `versionCode` in `build.gradle`
2. ✅ Build new APK
3. ✅ Update Firebase `version_code` and `version_name`
4. ✅ Upload APK to web
5. ✅ Users will see update dialog automatically

That's it! Your update system is now fully configured! 🎊
