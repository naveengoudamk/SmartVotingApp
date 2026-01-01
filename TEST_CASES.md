# Smart Voting App - Project Test Cases

This document outlines the comprehensive test cases for the Smart Voting Application, covering functional, UI, and security aspects. It distinguishes between scenarios suitable for Manual Testing and those that can be (or are) Automated.

| ID | Feature / Module | Test Scenario Description | Pre-conditions | Expected Result | Manual Testing | Automated Testing |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC01** | **User Login** | Verify login with valid Aadhaar and DOB. | User exists in `aadhaar_data.json`. | OTP is generated and sent; User logs in successfully after verification. | **True** | **False** |
| **TC02** | **User Login** | Verify login with invalid Aadhaar format (less than 12 digits). | App is open on Login screen. | Error message "Invalid 12-digit Aadhaar" is displayed. | **True** | **True** |
| **TC03** | **User Login** | Verify login with non-existent user. | User not in database. | Error message "User not found" is displayed. | **True** | **True** |
| **TC04** | **OTP Verification** | Verify OTP expiration logic. | user requested OTP. | OTP expires after 2 minutes; "OTP Expired" message shown. | **True** | **False** |
| **TC05** | **Admin Login** | Verify Admin login with valid credentials (e.g., `KAR-GOVT`). | App is on Admin Login screen. | Admin Dashboard opens with correct scope (e.g., Karnataka). | **True** | **True** |
| **TC06** | **Admin Login** | Verify Admin login with invalid password. | App is on Admin Login screen. | "Invalid Credentials" toast message appears. | **True** | **True** |
| **TC07** | **User Management** | Admin adds a new citizen user. | Admin is logged in. | New user is saved to internal storage and can immediately log in. | **True** | **False** |
| **TC08** | **User Management** | Admin edits an existing user's details. | Admin is logged in; User exists. | Details (Name, Address, etc.) are updated in persistent storage. | **True** | **False** |
| **TC09** | **Access Control** | State Admin tries to change user's state. | Logged in as `KAR-GOVT`. | State selection dropdown is disabled/locked to "Karnataka". | **True** | **True** |
| **TC10** | **Access Control** | Super Admin (India Govt) tries to change user's state. | Logged in as `ECI-INDIA`. | State selection dropdown is enabled and selectable. | **True** | **True** |
| **TC11** | **Voting Process** | User attempts to vote in an active election. | User logged in; Election is active; User eligible. | Vote is recorded; "Vote Cast Successfully" message appears. | **True** | **False** |
| **TC12** | **Voting Process** | User attempts to vote twice in the same election. | User already voted in this election. | "You have already voted" message prevents action. | **True** | **True** |
| **TC13** | **Party List** | Verify display of party details. | User is on "Candidates" tab. | Party cards show Name, Logo, Leader, Founded Year, HQ, and Ideology. | **True** | **False** |
| **TC14** | **Results** | Admin views election results. | Votes have been cast. | Result count matches the actual votes recorded. | **True** | **True** |
| **TC15** | **Persistence** | Verify new user data persists after app restart. | New user added; App closed and reopened. | New user can still log in successfully using stored JSON. | **True** | **False** |
| **TC16** | **UI/UX** | Verify transparent background implementation. | List Fragment open. | Background is transparent, showing the app-wide theme. | **True** | **False** |
| **TC17** | **UI/UX** | Verify Glassmorphism effect on Profile. | Account Fragment open. | Cards have semi-transparent white background (`#CCFFFFFF`). | **True** | **False** |
| **TC18** | **Search** | Admin searches for a user by name/Aadhaar. | Admin User List open. | List filters to show only matching users. | **True** | **True** |
| **TC19** | **Data Sync** | Verify Asset to Internal Storage sync. | App installed fresh; custom asset data present. | Internal storage is populated with asset users on first run. | **False** | **True** |
| **TC20** | **Feedback** | User submits feedback. | User logged in. | Feedback is saved and visible in Admin Feedback panel. | **True** | **False** |

**Note:**
*   **Manual Testing (True)**: Requires human interaction to verify UI elements, visual flow, or complex logic that isn't easily unit-tested.
*   **Automated Testing (True)**: Logic that can be validated via Unit Tests (JUnit) or UI Tests (Espresso) without human intervention.
