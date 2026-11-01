# HRMS (Grails 7)

10-day build plan: see `HRMS_10_Day_Routine.docx`. GUI plan: `HRMS_GUI_Plan_Bangla.docx`.

## Run

```bash
./gradlew bootRun        # http://localhost:8080
```

Database is H2 file `devDb` (dev). **After pulling Day 1, delete `devDb.mv.db` once**
(Employee got new columns and `joinDate` is now a date) - the seed data is re-created on start.

## Demo accounts (dev only) - password for all: `Hrms@2026`

| Username   | Role(s)                | Linked employee  |
|------------|------------------------|------------------|
| admin      | ROLE_ADMIN             | -                |
| hr         | ROLE_HR, ROLE_EMPLOYEE | Nasrin Sultana   |
| manager    | ROLE_MANAGER, ROLE_EMPLOYEE | Karim Hossain |
| accountant | ROLE_ACCOUNTANT, ROLE_EMPLOYEE | Sumaiya Akter |
| employee   | ROLE_EMPLOYEE          | Rahim Uddin      |

Change passwords from the top-right user menu -> "পাসওয়ার্ড পরিবর্তন".

## Day 1 manual test checklist

1. Open `/` while logged out -> redirected to login. Wrong password -> red error.
2. Log in as each user; check the sidebar matches the role (GUI plan section 6).
3. `employee` user: only "আমার প্রোফাইল"; visiting `/employee` or `/department` -> 403 page;
   `/employee/show/<other id>` -> 403.
4. `hr`: Employee list -> New employee -> fill all fields + photo -> saved, profile tabs show data.
   Try bad NID (e.g. `123`) and a duplicate email -> inline red errors.
5. `hr`: edit employee, replace / remove photo. `admin` only: delete button (blocked if a user is linked).
6. `accountant` and `employee` (own profile) see the salary tab; `manager` does not.
7. Change password (wrong current / short / mismatch -> error; valid -> toast, re-login works).
8. Logout -> back at login.