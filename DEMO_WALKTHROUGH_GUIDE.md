# Smart Voting App: End-to-End Demo Walkthrough

This document serves as a **Storyboard and Script** for your project demonstration. Since I am an AI purely in the code environment, I cannot take screenshots of your running Android Emulator.

**Instructions:**
1.  Run your app on a device or emulator.
2.  Follow the steps below.
3.  Take a screenshot at each step.
4.  Paste the screenshot into the `[INSERT SCREENSHOT HERE]` blocks.
5.  Read the **Explanation** text during your presentation or include it in your report.

---

## 🛑 Phase 1: Voter Authentication

### Step 1: Application Launch
*   **Action**: Tap the app icon to launch.
*   **Visual**: The animated Splash Screen.
*   **Screenshot**:
    > [ INSERT SPLASH SCREEN SCREENSHOT HERE ]
*   **Technical Explanation**: 
    "The app launches with a lightweight SplashActivity. It performs an initial security check of the environment (Root detection) and pre-loads the essential JSON assets (`aadhaar_data.json`) into memory for low-latency performance."

### Step 2: Secure Login Interface
*   **Action**: Land on the Login Screen. Select the "Voter" tab.
*   **Visual**: Input field for Aadhaar ID and the Biometric (Fingerprint) icon.
*   **Screenshot**:
    > [ INSERT LOGIN SCREEN (VOTER TAB) HERE ]
*   **Technical Explanation**:
    "The login system uses a `TabLayout` with `ViewPager2` to switch between User and Admin contexts. The Voter input enforces strict regex validation (12 digits) and integrates with Android's `BiometricPrompt` API for 2-Factor Authentication."

---

## 🗳️ Phase 2: The Voting Process

### Step 3: Voter Dashboard
*   **Action**: Successfully log in.
*   **Visual**: The User Dashboard showing the profile photo, "Eligible" status (Green text), and the list of active elections.
*   **Screenshot**:
    > [ INSERT DASHBOARD SCREENSHOT HERE ]
*   **Technical Explanation**:
    "Upon login, the `UserUtils` class deserializes the user's profile. The Dashboard dynamically checks eligibility (Age >= 18) and filters the `RecyclerView` list of elections using the user's specific state (e.g., 'Karnataka'), ensuring relevant content only."

### Step 4: Election Ballot
*   **Action**: Click on "General Assembly Election 2024".
*   **Visual**: A list of political parties/candidates with their logos and radio buttons.
*   **Screenshot**:
    > [ INSERT BALLOT/VOTING SCREEN HERE ]
*   **Technical Explanation**:
    "This is the secure `VotingActivity`. It renders `VotingOption` objects into a card-based layout. The window is secured with `FLAG_SECURE` to prevent screenshots by other apps, maintaining the secrecy of the ballot."

### Step 5: Voting Confirmation
*   **Action**: Select a candidate and click "Submit Vote".
*   **Visual**: The success toast message or confirmation dialog.
*   **Screenshot**:
    > [ INSERT SUCCESS TOAST/DIALOG SCREENSHOT HERE ]
*   **Technical Explanation**:
    "The voting transaction is atomic. The `VoteManager` instantly checks for duplicates. If clean, it serializes a new `VoteRecord` with a Unix timestamp to the encrypted `votes.json` database and locks the user from voting again."

---

## 🔐 Phase 3: Administration

### Step 6: Government Official Login
*   **Action**: Logout and switch to the "Admin" tab on the Login Screen.
*   **Visual**: Input fields for Department Code and Password.
*   **Screenshot**:
    > [ INSERT ADMIN LOGIN SCREEN HERE ]
*   **Technical Explanation**:
    "The Admin portal is isolated from the Voter logic. It validates credentials against a separate hardcoded secure list (simulating a government server) and establishes a privileged session using `SharedPreferences`."

### Step 7: Live Results Dashboard
*   **Action**: Log in as Admin. View the "Results" tab.
*   **Visual**: Charts or lists showing vote counts for each party.
*   **Screenshot**:
    > [ INSERT ADMIN RESULTS SCREENSHOT HERE ]
*   **Technical Explanation**:
    "The Admin Dashboard performs real-time data aggregation. It reads the local 'blockchain' of votes (`votes.json`), groups them by Candidate ID using a HashMap, and calculates percentage victories instantly without needing an external cloud server."
