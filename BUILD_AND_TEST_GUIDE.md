# 🚀 Build & Test Update System - Complete Guide

## 📋 Step-by-Step Instructions

Follow these steps to build the APK and test the update dialog.

---

## Step 1: Build the Release APK

### **Option A: Using Android Studio (Recommended)**

1. **Open Android Studio**
2. **Open your project**: `SmartVotingApp`
3. **Build the APK**:
   - Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
   - Wait for build to complete
   - Click **"locate"** in the notification

4. **Find your APK**:
   - Location: `app/build/outputs/apk/release/app-release.apk`

### **Option B: Using Command Line**

```bash
cd /Users/naveennavi/Desktop/projects/SmartVotingApp

# Build release APK
./gradlew assembleRelease

# APK will be at:
# app/build/outputs/apk/release/app-release.apk
```

---

## Step 2: Upload APK to Web

### **Using the Sync Script**

```bash
cd /Users/naveennavi/Desktop/projects/SmartVotingApp

# Run the sync script
./sync-apk.sh

# Follow the prompts:
# - It will copy APK to smartvotingweb/public/
# - Ask if you want to commit
# - Ask if you want to push to GitHub
```

### **Manual Method**

```bash
# Copy APK to web public folder
cp app/build/outputs/apk/release/app-release.apk \
   smartvotingweb/public/SmartVotingApp.apk

# Commit and push
cd smartvotingweb
git add public/SmartVotingApp.apk
git commit -m "Update APK to version 1.0"
git push
```

---

## Step 3: Deploy to Vercel

### **Automatic Deployment**

Vercel will automatically deploy when you push to GitHub:

1. **Push changes** (done in Step 2)
2. **Wait 1-2 minutes** for Vercel to build
3. **Check deployment**: https://smart-voting-app-web.vercel.app/

### **Manual Deployment (if needed)**

```bash
cd smartvotingweb

# Deploy to Vercel
vercel --prod

# Or use Vercel dashboard
# https://vercel.com/dashboard
```

---

## Step 4: Test the Update System

### **Test 1: No Update (Current State)**

**Setup:**
- Firebase: `version_code: 1`
- App installed: `versionCode: 1`

**Steps:**
1. Install the APK on your device
2. Open the app
3. **Expected**: No update dialog, app works normally ✅

---

### **Test 2: Update Required**

**Setup:**
1. **Keep the old app installed** (version 1.0)
2. **Update Firebase**:
   - Go to Firebase Console
   - Realtime Database
   - Click on `app_version` → `version_code`
   - Change to **`2`**
   - Click on `version_name`
   - Change to **`"2.0"`**
   - Click on `update_message`
   - Change to **`"🎉 New features available! Update now to enjoy the latest version."`**

**Steps:**
1. **Open the app** (old version 1.0)
2. **Splash screen** appears
3. **Login screen** loads
4. **✨ Beautiful update dialog appears!**

**What you should see:**
```
┌─────────────────────────────────┐
│  Semi-transparent overlay       │
│                                 │
│     ┌───────────────────┐      │
│     │                   │      │
│     │   🚀 (Gradient)   │      │
│     │                   │      │
│     │ Update Available  │      │
│     │                   │      │
│     │  v1.0  →  v2.0   │      │
│     │                   │      │
│     │  Update message   │      │
│     │                   │      │
│     │  ✨ What's New:   │      │
│     │  • Features...    │      │
│     │                   │      │
│     │  [Update Now]     │      │
│     │  [Go Back]        │      │
│     │                   │      │
│     └───────────────────┘      │
│                                 │
└─────────────────────────────────┘
```

5. **Click "Update Now"**:
   - Browser opens to: https://smart-voting-app-web.vercel.app/#features
   - App closes
   - You can download the new APK

6. **Click "Go Back"**:
   - App closes
   - Cannot use app

---

## Step 5: Update to Version 2.0 (For Next Release)

When you want to release version 2.0:

### **1. Update build.gradle**

```gradle
// In app/build.gradle
defaultConfig {
    versionCode 2        // Change from 1 to 2
    versionName "2.0"    // Change from "1.0" to "2.0"
}
```

### **2. Build new APK**

```bash
./gradlew assembleRelease
```

### **3. Upload to web**

```bash
./sync-apk.sh
```

### **4. Update Firebase**

```json
{
  "app_version": {
    "version_code": 2,
    "version_name": "2.0",
    "update_message": "🎉 New features available!",
    "force_update": true
  }
}
```

### **5. Test**

- Users with v1.0 will see update dialog
- Users with v2.0 will not see dialog

---

## 📱 Quick Test Checklist

### **Before Testing:**
- [ ] APK built successfully
- [ ] APK uploaded to web
- [ ] Web deployed to Vercel
- [ ] Firebase configured with version info

### **Test Scenarios:**

**Scenario 1: Same Version**
- [ ] Firebase version_code = 1
- [ ] App version = 1
- [ ] Result: No dialog ✅

**Scenario 2: Update Available**
- [ ] Firebase version_code = 2
- [ ] App version = 1
- [ ] Result: Dialog appears ✅

**Scenario 3: Update Now Button**
- [ ] Click "Update Now"
- [ ] Browser opens ✅
- [ ] App closes ✅
- [ ] Can download APK ✅

**Scenario 4: Go Back Button**
- [ ] Click "Go Back"
- [ ] App closes ✅

---

## 🔧 Troubleshooting

### **Issue: APK not building**

```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleRelease
```

### **Issue: Update dialog not showing**

**Check:**
1. Firebase `version_code` is higher than app version
2. Internet connection works
3. Firebase rules allow reading `app_version`
4. Check logs: `adb logcat | grep LoginActivity`

### **Issue: Web not updating**

```bash
# Force push to trigger Vercel deployment
cd smartvotingweb
git commit --allow-empty -m "Trigger deployment"
git push
```

### **Issue: APK not downloading from web**

1. Check file exists: `smartvotingweb/public/SmartVotingApp.apk`
2. Check Vercel deployment logs
3. Try direct URL: `https://smart-voting-app-web.vercel.app/SmartVotingApp.apk`

---

## 📊 Version Management

| Version | Code | Firebase | Status |
|---------|------|----------|--------|
| 1.0 | 1 | 1 | Current (No dialog) |
| 2.0 | 2 | 2 | Next release |
| 3.0 | 3 | 3 | Future |

**Rule**: Firebase `version_code` > App `versionCode` = Update dialog shows

---

## 🎯 Summary

### **To Test Update Dialog:**

1. ✅ Build APK (version 1.0)
2. ✅ Upload to web
3. ✅ Deploy to Vercel
4. ✅ Install on device
5. ✅ Set Firebase version_code to 2
6. ✅ Open app
7. ✅ See beautiful update dialog!

### **Files to Check:**

- **APK**: `app/build/outputs/apk/release/app-release.apk`
- **Web APK**: `smartvotingweb/public/SmartVotingApp.apk`
- **Version**: `app/build.gradle` (versionCode & versionName)
- **Firebase**: Realtime Database → `app_version`

---

## 🚀 Ready to Test!

Run these commands to get started:

```bash
# 1. Build APK
cd /Users/naveennavi/Desktop/projects/SmartVotingApp
./gradlew assembleRelease

# 2. Sync to web
./sync-apk.sh

# 3. Check if deployed
open https://smart-voting-app-web.vercel.app/

# 4. Update Firebase (manually in console)
# 5. Install APK and test!
```

Good luck! 🎊
