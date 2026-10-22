# Task 7 — Pipeline as Code and Server Deployment

## Jenkinsfile Stages Explained

| Stage | Description |
|-------|-------------|
| Checkout | Clone source from GitHub |
| Build | `mvn clean compile` — compile Java sources |
| Test | `mvn test` — run unit tests, publish JUnit report |
| Package | `mvn package` — create JAR artifact |
| Docker Build | `docker build` — build image, tag with version |
| Docker Push | `docker push` — push to Docker Hub registry |
| Deploy | `docker-compose up` — deploy fresh container |

## Parameterization (Task 7 Requirement)

The pipeline has 3 parameters:

```groovy
parameters {
    choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'])
    string(name: 'DOCKER_IMAGE_TAG', defaultValue: 'latest')
    booleanParam(name: 'SKIP_TESTS', defaultValue: false)
}
```

## Deployment Flow

```
Git Push → Jenkins Webhook
    ↓
Stage 1: Checkout (git clone)
    ↓
Stage 2: Build (mvn clean compile)
    ↓
Stage 3: Test (mvn test → JUnit report)
    ↓
Stage 4: Package (mvn package → .jar)
    ↓
Stage 5: Docker Build (docker build -t hostelcms/hostel-complaint-system:v1.0)
    ↓
Stage 6: Docker Push (docker push → Docker Hub)
    ↓
Stage 7: Deploy (docker-compose up -d)
    ↓
App running at http://localhost:8080
```

## Evidence Required
- Jenkinsfile in root of repo ✅
- Successful pipeline run screenshot
- Deployed app URL/screenshot
- Parameter configuration screenshot
