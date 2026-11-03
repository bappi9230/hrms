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

## Day 4 - Leave Management

Menu: ছুটি (all roles), Settings -> ছুটির নীতি (Admin/HR). **Delete `devDb.mv.db` once** after pulling Day 4 (new tables + seed).

- Leave types/policy (`/master/leavetype`): annual quota (0 = unlimited), YEARLY or MONTHLY accrual, paid flag, carry forward + cap, approval levels (1 = manager, 2 = manager + HR), gender restriction.
- Balance per employee per year is created lazily; carry forward is taken from last year's remaining (up to the cap).
- Approval chain: manager exists -> `MANAGER` (+ `HR` if levels = 2); no manager -> `HR`. Nobody can approve their own request; Admin/HR can act on manager step.
- Final approval: balance used, attendance rows marked `ON_LEAVE` (up to today), employee status `ACTIVE <-> ON_LEAVE` synced (on approve/cancel, on dashboard open, hourly timer).
- Weekend = Friday only (`LeaveService.WEEKEND`). Holiday calendar is not wired in yet.

### Day 4 test checklist
1. `employee` (rahim): Leave -> balance cards (Annual shows 5 carried from last year); apply Casual leave -> goes to manager. Overlap / over-balance / Friday-only range -> red error.
2. `manager` (karim): Leave -> Approval -> approve rahim's pending request -> status APPROVED, balance used +2.
3. Apply Annual leave (2 levels) as rahim -> manager approves -> still pending (HR step) -> `hr` approves -> APPROVED.
4. Reject without remark -> error; with remark -> REJECTED, pending released.
5. Apply leave starting today, approve it -> dashboard "ছুটিতে" count +1, employee status ON_LEAVE, today's attendance shows ON_LEAVE. Cancel (HR) -> back to ACTIVE.
6. Team calendar: manager sees team with type codes; employee sees department peers as dots.
7. `hr`/`admin`: Leave -> সব আবেদন, সবার ব্যালেন্স; Settings -> ছুটির নীতি add/edit type. Nasrin (FEMALE) sees Maternity; Rahim does not.
