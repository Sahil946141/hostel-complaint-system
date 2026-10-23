# Task 8 — Pipeline as Code and Server Deployment (Nginx)

## Deliverables

| Deliverable | Location |
| --- | --- |
| Jenkinsfile (checkout, build, package, deploy) | [`Jenkinsfile`](../Jenkinsfile) |
| Successful pipeline run | Verified end-to-end locally (see evidence below) |
| Deployed application URL | `http://localhost:3000/` (dev), `http://localhost:3001/` (staging) |
| Configuration evidence | [`docker-compose.yml`](../docker-compose.yml), [`frontend/nginx.conf`](../frontend/nginx.conf), [`frontend/Dockerfile`](../frontend/Dockerfile), [`backend/Dockerfile`](../backend/Dockerfile) |

## Jenkins Pipeline job

Create a **Pipeline** job named `hostel-complaint-system-pipeline` with **Pipeline script from SCM**:

| Setting | Value |
| --- | --- |
| SCM | Git |
| Repository | `https://github.com/Sahil946141/hostel-complaint-system.git` |
| Branch | `*/feature/task-8-pipeline-deployment` (switch to `*/dev` after merging) |
| Script path | `Jenkinsfile` |
| Trigger | Poll SCM: `H/5 * * * *` |

The Windows Jenkins agent needs Java 21, Node.js/npm, Git, Maven at `%USERPROFILE%\.jenkins-tools\apache-maven-3.9.6`, and Docker Desktop with its Linux engine running.

## Pipeline stages

The root `Jenkinsfile` is a declarative pipeline with exactly the four stages required by Task 8.

### Stage 1 — Checkout
Checks out the source from the configured SCM and prints the branch, commit, build number, and selected environment.

### Stage 2 — Build
- Backend: `mvn -B -f backend/pom.xml clean verify` — compiles Spring Boot, runs unit tests, and packages the executable JAR.
- Frontend: `npm ci` then `npm run build` with `VITE_API_URL=/api` — produces the production `dist/` assets.
- The `post` block publishes the JUnit report from `backend/target/surefire-reports/*.xml`.

### Stage 3 — Package
Archives the build artifacts with fingerprinting:
- `backend/target/complaint-system-1.0.0.jar` (executable Spring Boot JAR)
- `frontend/dist/**` (compiled React production assets)

### Stage 4 — Deploy
Builds the Docker images and starts the full stack with Docker Compose:
- PostgreSQL database
- Spring Boot backend (embedded Tomcat)
- Nginx frontend container serving the React UI and reverse-proxying `/api/*` to the backend

Jenkins validates the Compose config, runs `docker compose up --detach --build --wait` (waits for healthy services), then smoke-tests the UI and `/api/auth/health` endpoints.

## Parameterized environment setting

The `DEPLOY_ENVIRONMENT` choice parameter selects the target environment. Each environment uses a separate Compose project name and isolated ports + database volume so they run side by side without collisions.

| Environment | UI URL | Backend port | DB host port | Compose project |
| --- | --- | --- | --- | --- |
| `dev` | `http://localhost:3000/` | 8080 | 5433 | `hostel-dev` |
| `staging` | `http://localhost:3001/` | 8081 | 5434 | `hostel-staging` |

The DB host ports (5433/5434) avoid the host's default PostgreSQL port 5432.

## Deployment target

- **Nginx** serves the compiled React SPA (`frontend/dist`) on port 80 inside the container, mapped to the host port chosen by the environment parameter.
- Nginx reverse-proxies `/api/*` to the Spring Boot backend, which uses an **embedded Tomcat** servlet container (the default Spring Boot web server).
- So the build is deployed to both: Nginx (frontend/static) and Tomcat (backend/API).

## Configuration evidence

### docker-compose.yml (parameterized ports)

```yaml
services:
  db:
    image: postgres:15-alpine
    ports:
      - "${POSTGRES_HOST_PORT:-5432}:5432"
    ...
  backend:
    ports:
      - "${BACKEND_HOST_PORT:-8080}:8080"
    ...
  frontend:
    build:
      args:
        VITE_API_URL: ${VITE_API_URL:-/api}
    ports:
      - "${FRONTEND_HOST_PORT:-3000}:80"
```

### nginx.conf (reverse proxy)

```nginx
server {
    listen 80;
    location / {
        try_files $uri $uri/ /index.html;   # React SPA routing
    }
    location /api/ {
        proxy_pass http://backend:8080/api/;  # proxy to Spring Boot/Tomcat
    }
}
```

## Verification evidence (end-to-end pipeline run)

### Build stage — backend (Maven)

```
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- spring-boot:3.2.5:repackage (repackage) @ complaint-system ---
[INFO] Building jar: E:\Devops_23102A0008\backend\target\complaint-system-1.0.0.jar
[INFO] BUILD SUCCESS
[INFO] Total time:  26.032 s
```

- 6 unit tests passed (0 failures).
- Executable JAR built: `complaint-system-1.0.0.jar` (48.15 MB).

### Build stage — frontend (Vite)

```
> vite build
vite v8.3.5 building client environment for production...
✓ 87 modules transformed.
dist/index.html                   0.62 kB
dist/assets/index-6uAZG2hS.css    8.43 kB
dist/assets/index-mbbF2er1.js   335.25 kB
✓ built in 409ms
```

### Package stage — artifacts

| Artifact | Size |
| --- | --- |
| `backend/target/complaint-system-1.0.0.jar` | 48.15 MB |
| `frontend/dist/index.html` | 0.62 kB |
| `frontend/dist/assets/index-*.css` | 8.43 kB |
| `frontend/dist/assets/index-*.js` | 335.25 kB |

### Deploy stage — containers

```
NAMES                       STATUS                        PORTS
hostel-dev-frontend-1        Up (healthy)                  0.0.0.0:3000->80/tcp
hostel-dev-backend-1         Up (healthy)                  0.0.0.0:8080->8080/tcp
hostel-dev-db-1              Up (healthy)                  0.0.0.0:5433->5432/tcp
hostel-staging-frontend-1    Up (healthy)                  0.0.0.0:3001->80/tcp
hostel-staging-backend-1      Up (healthy)                  0.0.0.0:8081->8080/tcp
hostel-staging-db-1           Up (healthy)                  0.0.0.0:5434->5432/tcp
```

Both `hostel-dev` and `hostel-staging` Compose projects run simultaneously with isolated ports and database volumes.

### Deploy stage — smoke tests (deployed application URL)

```
dev UI  (http://localhost:3000/)            → HTTP 200 (627 bytes)
dev API (http://localhost:3000/api/auth/health) → "Hostel Complaint System is running!"

stg UI  (http://localhost:3001/)            → HTTP 200 (627 bytes)
stg API (http://localhost:3001/api/auth/health) → "Hostel Complaint System is running!"
```

The deployed frontend returns the real application HTML:

```html
<!doctype html>
<html lang="en">
  <head>
    <title>Hostel CMS | Complaint Management System</title>
    ...
  </head>
  <body>
    <div id="root"></div>
  </body>
</html>
```

### Docker images built

```
REPOSITORY                TAG       SIZE
hostel-dev-frontend        latest    94.1MB
hostel-dev-backend         latest    382MB
hostel-staging-frontend    latest    94.1MB
hostel-staging-backend    latest    382MB
```

## How to run

From a Jenkins Pipeline build (recommended):
1. Select `DEPLOY_ENVIRONMENT` (dev or staging) in the build parameters.
2. Build. The pipeline runs Checkout → Build → Package → Deploy and prints the live URL.

From a local terminal (manual verification):
```powershell
# dev environment
$env:FRONTEND_HOST_PORT="3000"; $env:BACKEND_HOST_PORT="8080"; $env:POSTGRES_HOST_PORT="5433"; $env:VITE_API_URL="/api"
docker compose -p hostel-dev up --detach --build --wait

# staging environment
$env:FRONTEND_HOST_PORT="3001"; $env:BACKEND_HOST_PORT="8081"; $env:POSTGRES_HOST_PORT="5434"; $env:VITE_API_URL="/api"
docker compose -p hostel-staging up --detach --build --wait
```

To stop an environment without deleting its database volume:
```powershell
docker compose -p hostel-dev down
```

Do not use `down --volumes` unless database deletion is intended.
