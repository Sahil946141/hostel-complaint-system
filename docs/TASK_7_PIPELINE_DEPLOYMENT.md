# Task 7 — Pipeline as Code and Nginx Deployment

## Jenkins Pipeline job

Create a **Pipeline** job named `hostel-complaint-system-pipeline` and configure **Pipeline script from SCM**:

| Setting | Value |
| --- | --- |
| SCM | Git |
| Repository | `https://github.com/Sahil946141/hostel-complaint-system.git` |
| Branch | `*/feature/task-7-pipeline-deployment` (switch to `*/dev` after merging the feature branch) |
| Script path | `Jenkinsfile` |
| Trigger | Poll SCM: `H/5 * * * *` |

The Windows Jenkins agent needs Java 21, Node.js/npm, Git, Maven at `%USERPROFILE%\.jenkins-tools\apache-maven-3.9.6`, and Docker Desktop with its Linux engine running.

## Pipeline and deployment

The root `Jenkinsfile` has explicit **Checkout**, **Build**, **Package**, and **Deploy** stages. Build runs `mvn clean verify` and the Vite production build; Package archives the executable backend JAR and frontend `dist` files. Deploy builds and starts the Docker Compose application, serving the React UI through Nginx and proxying `/api` to Spring Boot.

Run the `DEPLOY_ENVIRONMENT` choice parameter:

| Environment | URL | Compose project | PostgreSQL host port |
| --- | --- | --- | --- |
| `dev` | `http://localhost:3000/` | `hostel-dev` | `5433` |
| `staging` | `http://localhost:3001/` | `hostel-staging` | `5434` |

Each environment has a separate Compose project and database volume. The database port mapping avoids the host's default PostgreSQL port. Jenkins waits for healthy services and probes both the UI and `/api/auth/health` before marking deployment successful.

To stop an environment without deleting its persistent database volume, run `docker compose -p hostel-dev down` (replace `hostel-dev` with the selected environment). Do not use `down --volumes` unless database deletion is intended.

## Verification evidence

- Jenkins Pipeline build `#1` checked out commit `c5de680` from `feature/task-7-pipeline-deployment` and completed with `SUCCESS`.
- Maven `clean verify` passed all 6 backend tests; the Vite production build succeeded.
- Jenkins archived `backend/target/complaint-system-1.0.0.jar` and the built `frontend/dist` assets, and published the JUnit report.
- The `dev` Compose project started healthy PostgreSQL and Spring Boot containers with the Nginx frontend running.
- HTTP probes returned `200` for `http://localhost:3000/` and `http://localhost:3000/api/auth/health`.
- SCM polling is configured every five minutes. The Pipeline build and artifacts are available at `http://localhost:9090/jenkins/job/hostel-complaint-system-pipeline/`.

Keep Jenkins credentials, local Jenkins home, and Docker data outside source control.
