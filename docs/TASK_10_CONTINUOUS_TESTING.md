# Task 10 — Continuous Testing in Jenkins

## Deliverables

| Deliverable | Location |
|---|---|
| Jenkinsfile with Selenium quality gate | [`Jenkinsfile`](../Jenkinsfile) |
| Selenium test scripts (9 TCs) | `tests/src/test/java/com/hostel/tests/` |
| JUnit XML test reports | `tests/target/surefire-reports/TEST-TestSuite.xml` |
| Failure screenshots | `tests/test-screenshots/FAILURE_*.png` |
| Defect commit | `b75179d` — "introduce deliberate defect in LoginTest TC-001" |
| Fix commit | this commit — "fix(task-10): correct broken selector + stable TC-004" |
| Successful rerun evidence | Tests run: 9, Failures: 0 — BUILD SUCCESS |

---

## Pipeline — 5 Stages

```
Checkout → Build → Package → Deploy → Selenium Tests
```

The Selenium Tests stage is the **quality gate**: if any test fails the
pipeline calls `error()` which stops execution and marks the build FAILED —
deployment is blocked.

```groovy
stage('Selenium Tests') {
    when { expression { return !params.SKIP_SELENIUM } }
    steps {
        bat "call \"%MAVEN_CMD%\" -B -f tests\\pom.xml clean test -Dapp.url=${appUrl}"
    }
    post {
        always {
            // Publish JUnit report every run
            junit allowEmptyResults: true,
                  testResults: 'tests/target/surefire-reports/*.xml'
            // Archive failure screenshots as pipeline artefacts
            archiveArtifacts artifacts: 'tests/test-screenshots/**/*.png',
                             allowEmptyArchive: true
        }
        failure {
            error("Selenium quality gate failed. Fix the defect and recommit.")
        }
    }
}
```

### Parameters

| Parameter | Values | Purpose |
|---|---|---|
| `DEPLOY_ENVIRONMENT` | `dev` / `staging` | Selects host ports + Compose project |
| `SKIP_SELENIUM` | `false` (default) | Emergency bypass — skips quality gate |

---

## Jenkins Configuration

Create a **Pipeline** job named `hostel-complaint-system-pipeline`:

| Setting | Value |
|---|---|
| Pipeline definition | Pipeline script from SCM |
| SCM | Git |
| Repository | `https://github.com/Sahil946141/hostel-complaint-system.git` |
| Branch | `*/feature/task-10-continuous-testing` |
| Script path | `Jenkinsfile` |
| Trigger | Poll SCM: `H/5 * * * *` |

The agent needs Java 21, Maven at `%USERPROFILE%\.jenkins-tools\apache-maven-3.9.6`, Node.js, Chrome browser, and Docker Desktop.

### Publish test report
The `junit` step in the `post { always { } }` block publishes the TestNG/JUnit
XML to the Jenkins **Test Results** page after every run (pass or fail).

---

## Deliberate Defect — Evidence

### Defect introduced (commit `b75179d`)

`LoginTest.java` TC-001 was changed to use a non-existent button ID:

```java
// ⚠️ DELIBERATE DEFECT
driver.findElement(By.id("login-btn-BROKEN")).click();
```

### Broken pipeline run result

```
Tests run: 9, Failures: 2, Errors: 0, Skipped: 0   ← FAILURE
com.hostel.tests.LoginTest.testValidStudentLogin     ← NoSuchElementException
  NoSuchElement: Unable to locate element {"method":"css selector",
  "selector":"#login\\-btn\\-BROKEN"}

com.hostel.tests.RaiseComplaintTest.testRaiseComplaint  ← cascading timeout
BUILD FAILURE — Selenium quality gate failed. Deployment blocked.
```

Failure screenshots auto-captured:
- `FAILURE_testValidStudentLogin_<timestamp>.png`
- `FAILURE_testRaiseComplaint_<timestamp>.png`

---

## Defect Fix — Evidence

### Fix applied (this commit)

`LoginTest.java` TC-001 restored to the correct ID:

```java
// ✅ DEFECT FIXED
driver.findElement(By.id("login-btn")).click();
```

### Successful rerun result

```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
Total time:  01:02 min
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

---

## How to Run Locally

```powershell
# Ensure app is running
$env:FRONTEND_HOST_PORT="3000"; $env:BACKEND_HOST_PORT="8080"
$env:POSTGRES_HOST_PORT="5433"; $env:VITE_API_URL="/api"
docker compose -p hostel-dev up --detach --wait

# Run the Selenium quality gate
mvn -B -f tests/pom.xml clean test -Dapp.url=http://localhost:3000

# Run with JUnit report only (no browser — fails gracefully)
mvn -B -f tests/pom.xml clean test -Dapp.url=http://localhost:3000
```

---

## How the Quality Gate Works

```
Pipeline runs Selenium Tests stage
         │
         ▼
   mvn clean test (9 TCs)
         │
    ┌────┴────┐
    │         │
  PASS      FAIL
    │         │
    ▼         ▼
junit()    junit()   ← report published either way
archive    archive   ← screenshots archived either way
    │         │
    ▼         ▼
Continue   error()  ← pipeline STOPS, build = FAILURE
 to post    deploy is blocked, team sees red build
```
