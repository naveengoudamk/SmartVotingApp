# Feedback System Documentation

## Overview
The Smart Voting App now has a comprehensive Firebase-based feedback system that allows users to submit feedback and admins to manage it based on their jurisdiction.

## Features

### For Users
- ✅ Submit feedback with title and description
- ✅ View all submitted feedback
- ✅ See feedback status (Pending, In Progress, Resolved)
- ✅ Receive admin responses
- ✅ Get notifications for newly resolved feedback

### For Admins
- ✅ **State Admins**: View feedback only from their state
- ✅ **Super Admin (ECI-INDIA)**: View ALL feedback from all states
- ✅ Respond to feedback
- ✅ Mark feedback as resolved
- ✅ Delete feedback
- ✅ Filter by status (All, Pending, Resolved)

## How It Works

### User Submits Feedback

1. User opens Account tab
2. Clicks "Submit Feedback"
3. Enters title and description
4. Feedback is saved to:
   - Local storage (JSON file)
   - Firebase Realtime Database

**Firebase Structure:**
```
feedback/
  ├── {feedback_id_1}/
  │   ├── id: "uuid"
  │   ├── userId: "aadhaar_id"
  │   ├── userName: "User Name"
  │   ├── userAadhaar: "xxxxxxxxxxxx"
  │   ├── userState: "Karnataka"  // User's city/state
  │   ├── title: "App crashes on login"
  │   ├── description: "Detailed description..."
  │   ├── status: "pending"
  │   ├── adminResponse: ""
  │   ├── timestamp: 1234567890
  │   └── resolvedTimestamp: 0
  └── {feedback_id_2}/
      └── ...
```

### Admin Views Feedback

**State Admin (e.g., Karnataka Admin):**
- Sees only feedback from users in Karnataka
- Can respond and resolve feedback from their state

**Super Admin (ECI-INDIA):**
- Sees ALL feedback from ALL states
- Can respond and resolve any feedback

### Admin Resolves Feedback

1. Admin clicks "Resolve" on a feedback
2. Enters response message
3. Marks as resolved
4. User sees the response in their Account tab
5. User gets a notification badge for newly resolved feedback

## State-Based Filtering

The system uses the `userState` field to filter feedback:

```java
// For state admins
feedbackManager.getFeedbackByState("Karnataka", listener);

// For super admin
feedbackManager.getAllFeedback(listener);
```

## Admin State Detection

The system determines admin type from login data:

```java
SharedPreferences prefs = getContext().getSharedPreferences("AdminSession", Context.MODE_PRIVATE);
String adminState = prefs.getString("admin_state", "");

boolean isSuperAdmin = "ECI-INDIA".equals(adminState);
```

## Firebase Rules

Ensure your Firebase Realtime Database rules allow:

```json
{
  "rules": {
    "feedback": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$feedbackId": {
        ".validate": "newData.hasChildren(['id', 'userId', 'title', 'description', 'status'])"
      }
    }
  }
}
```

## Testing

### Test State-Based Filtering

1. **Create test feedback:**
   - Login as user from Karnataka
   - Submit feedback
   - Login as user from Maharashtra
   - Submit feedback

2. **Test State Admin:**
   - Login as Karnataka admin
   - Should see only Karnataka feedback

3. **Test Super Admin:**
   - Login as ECI-INDIA admin
   - Should see ALL feedback (Karnataka + Maharashtra)

## Troubleshooting

**Feedback not appearing for admin:**
- Check Firebase connection
- Verify `userState` is being saved correctly
- Check admin's state in SharedPreferences
- Ensure Firebase rules allow read access

**Feedback not syncing:**
- Check internet connection
- Verify Firebase Database URL is correct
- Check logs: `adb logcat | grep FirebaseFeedbackManager`

## Code Files

- `Feedback.java` - Feedback model with userState field
- `FirebaseFeedbackManager.java` - Firebase operations
- `FeedbackManager.java` - Local storage + Firebase sync
- `AdminFeedbackFragment.java` - Admin feedback view with filtering
- `AccountFragment.java` - User feedback submission

## Future Enhancements

- [ ] Add feedback categories (Bug, Feature Request, Complaint)
- [ ] Email notifications for resolved feedback
- [ ] Feedback analytics dashboard
- [ ] Bulk feedback operations
- [ ] Export feedback to CSV
- [ ] Feedback search functionality
