# Task 12 — Jenkins and Docker Continuous Deployment

## Pipeline

The root `Jenkinsfile` builds and tests the Spring Boot backend and React frontend, deploys an isolated Compose stack for the Selenium gate, builds and tags both Docker images, pushes them to a local Docker Registry v2, then recreates the stack from the pushed tags.

The Docker stages only run after the backend/frontend build and enabled Selenium suite succeed. `SKIP_SELENIUM` defaults to `false`; leave it disabled for the acceptance build.

The pipeline uses:

| Parameter | Default | Purpose |
| --- | --- | --- |
| `DEPLOY_ENVIRONMENT` | `dev` | Isolated test/deployment stack; also supports `staging` |
| `LOCAL_REGISTRY` | `localhost:5000` | Registry endpoint; no Docker Hub account or Jenkins password is required |
| `IMAGE_VERSION` | `1.0.0` | Semantic version; the build number is appended as a unique tag |
| `SKIP_SELENIUM` | `false` | Optional emergency bypass; not used for acceptance verification |

The Jenkins test deployment enables deterministic student/admin test accounts through `APP_TEST_FIXTURES_ENABLED`. The Compose default is disabled, so normal deployments do not create these accounts.

For example, build `#3` publishes:

- `localhost:5000/hostelcms/hostel-complaint-system-backend:1.0.0-3`
- `localhost:5000/hostelcms/hostel-complaint-system-frontend:1.0.0-3`
- Corresponding `:latest` tags

The registry container is named `hostel-task12-registry`; its data persists in the `hostel-task12-registry-data` volume. Published images can be queried at `http://localhost:5000/v2/_catalog`.

## Isolated deployment ports

| Environment | Frontend / Nginx | Backend API | PostgreSQL |
| --- | --- | --- | --- |
| `dev` | `http://localhost:3100/` | `localhost:8180` | `localhost:55432` |
| `staging` | `http://localhost:3101/` | `localhost:8181` | `localhost:55433` |

The Task 12 Compose project is named `hostel-task12-<environment>` to avoid replacing other local application containers.

## Deliverables and evidence

- Backend runtime image: [`backend/Dockerfile`](../backend/Dockerfile)
- Nginx frontend runtime image: [`frontend/Dockerfile`](../frontend/Dockerfile)
- Build/test stack: [`docker-compose.yml`](../docker-compose.yml)
- Registry-image deployment override: [`docker-compose.cd.yml`](../docker-compose.cd.yml)
- Pipeline SCM source: `feature/task-12-jenkins-docker-cd`, script path `Jenkinsfile`
- Jenkins build: [Task 12 pipeline history](http://localhost:9090/jenkins/job/hostel-complaint-system-pipeline/)

On a successful build, Jenkins archives `docker-command-log.txt`, `docker-image-details.txt`, and `docker-container-status.txt` together with the application artifacts. The Console Output records the live build, push/pull commands, smoke-test responses, and stage results.

## Local commands

```powershell
docker image ls --digests
docker image inspect localhost:5000/hostelcms/hostel-complaint-system-backend:1.0.0-<BUILD_NUMBER>
docker container ls --filter "label=com.docker.compose.project=hostel-task12-dev"
curl.exe --fail http://localhost:5000/v2/_catalog
curl.exe --fail http://localhost:3100/api/auth/health
```

Keep Jenkins credentials and `jenkins/jenkins_home/` out of source control. The local registry is intended for same-machine development/demo deployments; use an authenticated TLS registry for a remote production deployment.
