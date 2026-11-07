# HRMS (Grails 7)

10-day build plan: see `HRMS_10_Day_Routine.docx`. GUI plan: `HRMS_GUI_Plan_Bangla.docx`.

## Run

```bash
./gradlew bootRun        # http://localhost:8080
```

Database is H2 file `devDb` (dev). **After pulling Day 1 (and again for Day 4 and Day 7), delete `devDb.mv.db` once**
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

## Day 7 - Recruitment & Onboarding

Menu: নিয়োগ (Kanban), অনবোর্ডিং, জনবল চাহিদা, আমার ইন্টারভিউ; Settings -> অনবোর্ডিং কাজ (checklist template).
**Delete `devDb.mv.db` once** after pulling Day 7 (new tables + seed).

Flow: Requisition (manager/HR) -> approve -> Job posting -> Candidate (Kanban: APPLIED -> SCREENING -> INTERVIEW -> OFFER -> HIRED)
-> Interview + feedback -> Offer (printable letter) -> Accepted -> **Convert to employee** (creates Employee, history, salary structure,
onboarding checklist, probation record, CV document). Requisition closes itself when all vacancies are filled.

Rules: posting only from APPROVED requisition with free vacancies; moving to OFFER needs one COMPLETED interview with HIRE/HOLD;
HIRED only via convert (cannot be dragged); one live (SENT/ACCEPTED) offer per candidate; interview times cannot clash for an interviewer;
feedback only after the interview time, by the interviewer or HR/Admin; probation: confirm / extend (max 2 x 1-3 months) / not confirmed.
Creating a login account for the new employee is still manual. "Not confirmed" only records the decision - exit process is Day 9.

### Day 7 test checklist (seed data included)
1. `hr`: Recruitment board shows 5 columns. Drag Tahmid APPLIED -> SCREENING (ok). Drag Farhana -> OFFER (red toast: needs interview). Drag anything to HIRED (not allowed).
2. `manager` (karim): জনবল চাহিদা -> new request -> appears PENDING; he sees only his own. `hr`: approve it, reject another without note (error).
3. `hr`: Postings -> new posting from the approved requisition; add a candidate with a PDF CV (>2 MB or .exe -> error; same email on same posting -> error).
4. `manager`: আমার ইন্টারভিউ -> Imran's interview (time passed) -> give feedback HIRE 4/5 -> status COMPLETED. Feedback on a future interview -> error.
5. `hr`: Farhana -> schedule interview (same interviewer, overlapping time -> error). Sadia (completed HIRE) -> create offer -> printable letter -> "accepted".
6. Mahfuz (offer ACCEPTED) -> Convert (pick manager/grade) -> redirected to onboarding checklist; employee list has Mahfuz (PROBATION), profile has history + CV document.
7. Convert again / reject candidate with live offer (offer withdrawn) / decline offer (candidate REJECTED) behave as described.
8. Onboarding: tick tasks, add a custom task, issue/return an asset (same serial twice -> error). Nasrin: probation OVERDUE -> extend (reason needed) / confirm -> type becomes FULL_TIME + history line.
9. Delete requisition-linked data checks: an interviewer or converted employee cannot be deleted from Employee screen.

## Day 8 - Performance, Training, Expense/Travel, Assets, Benefits

Menu: পারফরম্যান্স, প্রশিক্ষণ, খরচ ও সম্পদ (খরচের দাবি / ভ্রমণ / সম্পদ রেজিস্টার / সুবিধা) - সব রোলের জন্য; Settings -> সুবিধার প্ল্যান (Admin/HR).
**Delete `devDb.mv.db` once** after pulling Day 8 (new tables, `AssetAllocation.asset` column + seed). Dashboard "অপেক্ষমাণ অনুমোদন" now also counts expense, travel and appraisal reviews.

- Goals/KPI: self / manager / HR can set; total active weight per year <= 100; reaching target auto-COMPLETES.
- Appraisal cycle: DRAFT -> OPEN (creates an appraisal for every active employee) -> CLOSED. Self rating (1-5) -> manager rating -> COMPLETED; manager may review early only after self is submitted (or after due date); rating 1-2 or 5 needs a comment; nobody reviews themself.
- Training: catalogue (cards), self-enroll or HR enroll, seats, HR records PASS/FAIL -> certificate number + printable certificate.
- Expense: PENDING_MANAGER -> PENDING_ACCOUNTS -> APPROVED -> PAID (no manager = skips step 1). Receipt required above 500 TK (pdf/jpg/png, 2 MB); max 90 days old; duplicate check; nobody approves/pays their own claim.
- Travel: manager/HR approve; cannot clash with another trip or approved leave; expense can be linked to an approved trip.
- Assets: register (AVAILABLE/ASSIGNED/MAINTENANCE/RETIRED), issue/receive, same serial cannot be with two people. Benefits: plan has min service months; one ACTIVE enrollment per plan per employee.

### Day 8 test checklist (seed data included)
1. `employee` (rahim): Performance -> 3 goals (weights 40/30/30); add a goal with weight 10 -> error (total > 100). Update progress to target -> COMPLETED. Appraisals: his is already with the manager.
2. `manager` (karim): Performance -> team goals; Appraisals -> rahim -> rate 5 without comment (error) / rate 4 -> COMPLETED with goal score. Nasrin still waiting for self review -> cannot review before due date.
3. `hr`: Cycles -> create next-year-overlap cycle (error); open the DRAFT cycle -> appraisals created; close a cycle.
4. Training: `employee` enrolls in the 2-seat workshop (1 seat left) -> second person gets "আসন খালি নেই". `hr`: record PASS for Rahim in the running course -> certificate; Rahim's FAILED row can re-enroll only if course still open.
5. Expense: `employee` new claim 600 TK without receipt -> error; 450 TK ok. `manager` approves rahim's claim -> goes to accounts. `accountant` (sumaiya) approves/pays rahim's and karim's claims (payment ref required); sumaiya cannot pay her own.
6. Travel: rahim requests a trip overlapping Sylhet one -> error; `manager` approves it; reject without note -> error.
7. Assets (`hr`): issue AST-0002 to someone -> ASSIGNED; issue same serial again -> error; receive with "needs maintenance"; RETIRE an assigned asset -> error. `employee`: খরচ ও সম্পদ -> "আমার সম্পদ".
8. Benefits (`hr`): enroll Nasrin (joined < 3 months?) in health plan -> min-service error if too new; enroll twice -> error; end a benefit. Settings -> সুবিধার প্ল্যান add/edit.
