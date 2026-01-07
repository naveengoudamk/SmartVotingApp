# Quick Start Guide - App Update System

## 🚀 For Developers: Releasing a New Version

### 1. Update Version Numbers
Edit `app/build.gradle`:
```gradle
versionCode 2        // Increment by 1
versionName "2.0"    // Update version
```

### 2. Build Release APK
```bash
cd /Users/naveennavi/Desktop/projects/SmartVotingApp
./gradlew assembleRelease
```

### 3. Sync APK to Web
```bash
./sync-apk.sh
```
- Follow the prompts to auto-commit and push

### 4. Update Firebase
Go to Firebase Console → Realtime Database → `app_version`:
```json
{
  "version_code": 2,
  "version_name": "2.0",
  "update_message": "🎉 New features: ...",
  "force_update": false
}
```

### 5. Done!
Users will see the update dialog next time they open the app.

---

## 📱 For Users: What Happens

1. **Open the app** → Update check runs automatically
2. **Update available** → Dialog appears with update message
3. **Click "Update Now"** → Browser opens to download page
4. **Download & Install** → New version ready!

---

## 🔧 Quick Commands

**Build APK:**
```bash
./gradlew assembleRelease
```

**Sync to Web:**
```bash
./sync-apk.sh
```

**Push to GitHub:**
```bash
git add . && git commit -m "Update to v2.0" && git push
```

---

## 📊 Version Tracking

| Version | Code | Date | Changes |
|---------|------|------|---------|
| 1.0 | 1 | Initial | First release |
| 2.0 | 2 | TBD | Update system added |

---

## ⚠️ Important Notes

- **Always test** before releasing
- **Increment versionCode** for every release
- **Use force_update** only for critical fixes
- **APK location**: `app/build/outputs/apk/release/app-release.apk`
- **Web download**: `https://smart-voting-web.vercel.app/`

---

## 🆘 Need Help?

See full documentation: `UPDATE_SYSTEM.md`
