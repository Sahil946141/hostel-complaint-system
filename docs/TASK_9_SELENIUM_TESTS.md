# Task 9 — Selenium Test Design and Local Execution

## Deliverables

| Deliverable | Location |
|---|---|
| Test plan (user journeys + acceptance criteria) | This document |
| Selenium WebDriver test scripts (4 classes, 9 TCs) | `tests/src/test/java/com/hostel/tests/` |
| TestNG suite definition | `tests/src/test/resources/testng.xml` |
| Test data seeder | `backend/src/main/java/.../config/DataInitializer.java` |
| Local test report (Maven Surefire) | `tests/target/surefire-reports/` |
| Failure screenshot mechanism | `BaseTest.tearDown(ITestResult)` — auto-captures on failure |
| Screenshots folder | `tests/test-screenshots/` |

---

## Test Plan — Critical User Journeys

| # | User Journey | Test Class | TCs |
|---|---|---|---|
| 1 | Student / Admin Login | `LoginTest` | TC-001, TC-002, TC-003 |
| 2 | Raise a Complaint | `RaiseComplaintTest` | TC-004, TC-005 |
| 3 | Admin Dashboard & Status Update | `AdminDashboardTest` | TC-006, TC-007, TC-008 |
| 4 | New User Registration | `RegistrationTest` | TC-009 |

---

## Test Cases & Acceptance Criteria

### Journey 1 — Authentication

**TC-001** Valid student login  
- Test data: `student@hostel.com` / `student123`  
- Assertion: URL contains `/student` after login

**TC-002** Invalid credentials show error  
- Test data: `wrong@email.com` / `wrongpassword`  
- Assertion: `.alert-error` element is visible

**TC-003** Admin login redirects to admin dashboard  
- Test data: `admin@hostel.com` / `admin123`  
- Assertion: URL contains `/admin`

---

### Journey 2 — Raise a Complaint

**TC-004** Student raises a complaint and sees the CMP- ID  
- Test data: title="WiFi not working in Block A", description, category=WIFI, priority=HIGH  
- Assertion: `.alert-success` text contains `CMP-`

**TC-005** Empty form submission is blocked  
- Test data: no input  
- Assertion: URL stays on `/raise-complaint` (HTML5 `required` prevents submit)

---

### Journey 3 — Admin Dashboard

**TC-006** Admin dashboard shows statistics and complaints table  
- Assertion: at least 4 `.stat-card` elements visible; `#admin-complaints-table` present

**TC-007** Admin filters complaints by status  
- Actions: click PENDING and IN_PROGRESS filter buttons  
- Assertion: stays on `/admin` page

**TC-008** Admin updates complaint status  
- Actions: click Update button, select IN_PROGRESS, add remarks, save  
- Assertion: `.alert-success` visible after save

---

### Journey 4 — New User Registration

**TC-009** New student registers and lands on student dashboard  
- Test data: unique email (timestamped), role=STUDENT, room=B-202  
- Assertion: URL contains `/student`

---

## Test Infrastructure

### BaseTest — failure screenshot mechanism
```java
@AfterMethod
public void tearDown(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
        takeScreenshot("FAILURE_" + result.getName());   // auto-captured
    }
    driver.quit();
}
```

Every test failure automatically saves `test-screenshots/FAILURE_<testName>_<timestamp>.png`.

### Test data seeding — DataInitializer
Runs on backend startup via `CommandLineRunner`. Seeds:
- `student@hostel.com` / `student123` (STUDENT, room A-101)
- `admin@hostel.com` / `admin123` (ADMIN)
- `staff@hostel.com` / `staff123` (STAFF)
- One sample PLUMBING / HIGH complaint

### React controlled-input typing
React 18 controlled inputs require `click()` to focus before `sendKeys()`. Plain `sendKeys` after `click()` fires `onChange` per keystroke, updating the component state correctly.

---

## Local Execution — Maven

Prerequisites: app running at `http://localhost:3000`, Chrome installed.

```powershell
# Start the app (if not already running)
$env:FRONTEND_HOST_PORT="3000"; $env:BACKEND_HOST_PORT="8080"
$env:POSTGRES_HOST_PORT="5433"; $env:VITE_API_URL="/api"
docker compose -p hostel-dev up --detach --wait

# Run the full Selenium suite
mvn -B -f tests/pom.xml clean test

# Run against a different environment
mvn -B -f tests/pom.xml clean test -Dapp.url=http://localhost:3001
```

---

## Execution Result

```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
Total time: 31.463 s
BUILD SUCCESS
```

| Test | Result |
|---|---|
| TC-001 testValidStudentLogin | ✅ PASS |
| TC-002 testInvalidLogin | ✅ PASS |
| TC-003 testAdminLogin | ✅ PASS |
| TC-004 testRaiseComplaint | ✅ PASS |
| TC-005 testComplaintFormValidation | ✅ PASS |
| TC-006 testAdminDashboardStats | ✅ PASS |
| TC-007 testAdminFilterComplaintsByStatus | ✅ PASS |
| TC-008 testUpdateComplaintStatus | ✅ PASS |
| TC-009 testStudentRegistration | ✅ PASS |

Screenshots saved: `tests/test-screenshots/` (14 success screenshots per run).  
Failure screenshots: auto-captured as `FAILURE_<name>_<timestamp>.png` when any test fails.
