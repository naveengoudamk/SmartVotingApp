# Smart Voting App - Project Test Cases

This document outlines the comprehensive test cases for the Smart Voting Application, structured according to the logical project workflow. It distinguishes between scenarios suitable for Manual Testing and those that can be (or are) Automated.

---

### **Phase 1: Initial Setup & Data Integrity**
Before any user interaction, the system must ensure data is correctly loaded and synchronized.

| ID | Feature | Test Scenario | Pre-conditions | Expected Result | Manual | Auto |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC01** | **Data Sync** | Verify Asset to Internal Storage sync on first launch. | App installed fresh; custom `aadhaar_data.json` in assets. | Internal storage is populated with asset users; Login works for these users. | False | **True** |
| **TC02** | **Data Persistence** | Verify data persists across app restarts. | User added or edited; App killed and restarted. | New data (User/Vote) remains available. | True | **False** |

---

### **Phase 2: Authentication (Entry Point)**
The application supports two distinct login flows: Citizen (User) and Government (Admin).

| ID | Feature | Test Scenario | Pre-conditions | Expected Result | Manual | Auto |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC03** | **User Validation** | Login with invalid Aadhaar format (<12 digits). | Login Screen open. | Error "Invalid 12-digit Aadhaar" displayed. | True | **True** |
| **TC04** | **User Validation** | Login with non-existent user credentials. | User not in database. | Error "User not found" displayed. | True | **True** |
| **TC05** | **OTP Generation** | Request OTP with valid credentials. | Valid Aadhaar & DOB entered. | OTP generated, Notification sent, OTP Dialog appears. | **True** | False |
| **TC06** | **OTP Verification** | Enter correct OTP. | OTP sent. | Login successful; Redirect to User Dashboard. | **True** | False |
| **TC07** | **OTP Expiry** | Wait for OTP timer (2 mins) to expire. | OTP sent. | "OTP Expired" message shown; Verification fails. | **True** | False |
| **TC08** | **Admin Login** | Login with valid State Admin credentials (e.g., `KAR-GOVT`). | Admin Login Screen open. | Admin Dashboard opens with filtered "Karnataka" scope. | **True** | **True** |
| **TC09** | **Admin Login** | Login with valid Super Admin credentials (`ECI-INDIA`). | Admin Login Screen open. | Admin Dashboard opens with Global scope (All States). | **True** | **True** |
| **TC10** | **Admin Login** | Login with invalid Dept Code or Password. | Admin Login Screen open. | "Invalid Credentials" toast appears. | True | **True** |

---

### **Phase 3: Administration (System Setup)**
Admins configure the system, manage users, and set up elections.

| ID | Feature | Test Scenario | Pre-conditions | Expected Result | Manual | Auto |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC11** | **User Management** | Admin adds a new citizen user. | Admin logged in. | User saved to internal DB; "User Added" toast; User can now log in. | **True** | False |
| **TC12** | **Acc. Control (State)**| State Admin (e.g., Karnataka) adds user. | Logged in as `KAR-GOVT`. | State Spinner locked to "Karnataka". | **True** | **True** |
| **TC13** | **Acc. Control (Central)**| Super Admin adds user. | Logged in as `ECI-INDIA`. | State Spinner enabled; Can select any state. | **True** | **True** |
| **TC14** | **Edit User** | Admin modifies user details (Address/DOB). | User exists in list. | Changes saved and reflected immediately. | **True** | False |
| **TC15** | **Search User** | Search user by Name or Aadhaar. | User List populated. | List filters to show matching results. | **True** | **True** |
| **TC16** | **Create Election** | Admin creates a new election. | Admin Election Tab. | Election appears in active list; Visible to users. | **True** | False |
| **TC17** | **Add Party** | Admin adds a political party. | Admin Party Tab. | Party saved with Logo and Name. | **True** | False |

---

### **Phase 4: Citizen Experience (Voting & Info)**
The core functionality where users interact with the data set up by admins.

| ID | Feature | Test Scenario | Pre-conditions | Expected Result | Manual | Auto |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC18** | **Dashboard UI** | User views Candidate/Party list. | User logged in. | Cards show Logo, Name, Leader, Founded, HQ, Ideology. | **True** | False |
| **TC19** | **Voting Eligibility** | Ineligible user attempts to vote. | User marked `eligible: false`. | "Not Eligible to Vote" message/blocker. | **True** | **True** |
| **TC20** | **Cast Vote** | Eligible user votes in active election. | Election active; Not voted yet. | Vote recorded; Success animation/message; "Voted" status updates. | **True** | False |
| **TC21** | **Duplicate Vote** | User attempts to vote again. | User already voted. | "Already Voted" error message displayed. | **True** | **True** |
| **TC22** | **Profile View** | User views Account tab. | Account Fragment open. | Details (Aadhaar, Address) match login info; Glassmorphism UI applied. | **True** | False |
| **TC23** | **Submit Feedback** | User sends feedback/complaint. | Feedback Form open. | Feedback saved; Admin receives it. | **True** | False |

---

### **Phase 5: Post-Election (Analysis & Results)**
After voting, the system aggregates user choices.

| ID | Feature | Test Scenario | Pre-conditions | Expected Result | Manual | Auto |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC24** | **View Results** | Admin views election results. | Votes cast in election. | Vote counts per party match actual database records. | **True** | **True** |
| **TC25** | **Result UI** | Admin opens Result Fragment. | Fragment open. | Background is transparent; Layout is clean (No white background artifacts). | **True** | False |

---

### **Summary of Testing Types**
*   **Manual Testing (True)**: Focuses on UI/UX flows (Glassmorphism, Dialogs), hardware interaction (Notifications, Camera badge), and complex state workflows that require human judgment.
*   **Automated Testing (True)**: Focuses on Logic Validation (Login credentials, State locking rules, Math/Counting of results, Input validation).
