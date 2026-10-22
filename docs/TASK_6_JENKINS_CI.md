# Task 6 — Jenkins Continuous Integration

## Local Jenkins controller

- Jenkins LTS: 2.580.1
- URL: `http://localhost:9090/jenkins/`
- Java: Eclipse Temurin 21
- Jenkins home: `jenkins/jenkins_home` (local only; excluded by `.gitignore`)
- Start the controller with `jenkins/start-jenkins.ps1` from Windows PowerShell and keep its window open while using Jenkins.

Do not commit the Jenkins home directory, unlock token, user credentials, or secret files.

## CI job

| Setting | Value |
| --- | --- |
| Job | `hostel-complaint-system-ci` (Freestyle project) |
| Repository | `https://github.com/Sahil946141/hostel-complaint-system.git` |
| Branch | `*/dev` |
| Trigger | Poll SCM: `H/5 * * * *` (approximately every five minutes) |
| Build command | `call "%USERPROFILE%\.jenkins-tools\apache-maven-3.9.6\bin\mvn.cmd" -B -f backend\pom.xml clean verify` |
| Test reports | `backend/target/surefire-reports/*.xml` |
| Archived artifact | `backend/target/complaint-system-1.0.0.jar` |

The repository is public, so the job checks it out without stored Git credentials. Maven 3.9.6 is installed outside the repository under the Jenkins user's profile; Java 21 is available to the Jenkins process.

## Verification evidence

- Build `#1` checked out `dev` at `29d40a1441bc094c3f4ddeccfa2390033eea4d0f`.
- `mvn clean verify` completed successfully: 6 tests, 0 failures, 0 errors.
- Jenkins archived `complaint-system-1.0.0.jar` (about 50.5 MB) and published the JUnit report.
- The Git Polling Log recorded a scheduled poll at 4:06 PM on 22-Oct-2026, checked the remote `dev` branch, and reported no changes because the current revision had already been built.

In Jenkins, view the result at **hostel-complaint-system-ci → Build #1 → Console Output**. The job dashboard also exposes the archived artifact and test report.
