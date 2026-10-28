# Task 9 — Selenium Test Plan

## Critical User Journeys Tested

| # | Journey | Test Class | Assertion |
|---|---------|-----------|-----------|
| 1 | Student Login | `LoginTest.java` | JWT token received, redirect to dashboard |
| 2 | Register new user | `LoginTest.java` | Account created, auto-login |
| 3 | Raise a complaint | `RaiseComplaintTest.java` | Complaint ID generated, status = PENDING |
| 4 | View my complaints | `RaiseComplaintTest.java` | Complaint list visible, correct data |
| 5 | Admin updates status | `AdminDashboardTest.java` | Status changes to IN_PROGRESS / RESOLVED |

## Test Data Used

```java
// Student credentials
email    = "student@hostel.com"
password = "student123"

// Admin credentials
email    = "admin@hostel.com"
password = "admin123"

// Complaint data
title       = "Water leakage in Room 201"
description = "Pipe is leaking since 3 days"
category    = "WATER"
priority    = "HIGH"
```

## Failure Screenshot Mechanism

```java
// BaseTest.java captures screenshot on failure
@AfterMethod
public void takeScreenshotOnFailure(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(src, new File("screenshots/" + result.getName() + ".png"));
    }
}
```

## Run Locally

```bash
cd tests
mvn test -Dtest=LoginTest,RaiseComplaintTest,AdminDashboardTest
```

## Run via Jenkins

```groovy
stage('Selenium Tests') {
    steps {
        dir('tests') {
            bat 'mvn test'
        }
    }
    post {
        always {
            publishTestNGResults testResultsPattern: 'tests/target/surefire-reports/*.xml'
        }
    }
}
```
