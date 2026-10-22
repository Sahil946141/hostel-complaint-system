# Task 12 — Jenkins + Docker CD Pipeline

## What This Task Adds

Extends the Jenkins pipeline (Task 7) to:
1. Build a **versioned** Docker image (`v1.0.<BUILD_NUMBER>`)
2. Push to **Docker Hub** registry
3. Deploy a **fresh container** automatically after tests pass

## Versioning Strategy

```
hostelcms/hostel-complaint-system:v1.0.5   ← build number
hostelcms/hostel-complaint-system:latest   ← always latest
```

## Docker Hub Registry

- Registry: `docker.io`
- Repository: `hostelcms/hostel-complaint-system`
- Tags: `v1.0.<BUILD_NUMBER>`, `latest`

## Pipeline Extension (added to Jenkinsfile)

```groovy
environment {
    VERSION = "v1.0.${BUILD_NUMBER}"
    IMAGE   = "hostelcms/hostel-complaint-system"
}

stage('Docker Build & Tag') {
    steps {
        bat "docker build -t ${IMAGE}:${VERSION} ./backend"
        bat "docker tag ${IMAGE}:${VERSION} ${IMAGE}:latest"
        echo "Built image: ${IMAGE}:${VERSION}"
    }
}

stage('Docker Push to Hub') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'docker-hub-credentials',
            usernameVariable: 'USER',
            passwordVariable: 'PASS'
        )]) {
            bat "docker login -u %USER% -p %PASS%"
            bat "docker push ${IMAGE}:${VERSION}"
            bat "docker push ${IMAGE}:latest"
        }
        echo "Pushed: ${IMAGE}:${VERSION}"
    }
}

stage('Deploy Fresh Container') {
    steps {
        bat "docker rm -f hostel-backend || echo No container"
        bat "docker run -d --name hostel-backend -p 8080:8080 ${IMAGE}:${VERSION}"
        bat "docker ps"
        echo "Deployed version: ${VERSION}"
    }
}
```

## End-to-End Commit-to-Container Flow

```
git push origin dev
       ↓ (Jenkins webhook fires)
Jenkins Job #5
       ↓
Checkout → Build → Test (Selenium) → Package
       ↓ (only if all pass)
docker build -t hostelcms/hostel-complaint-system:v1.0.5 .
       ↓
docker push → Docker Hub → hostelcms/hostel-complaint-system:v1.0.5
       ↓
docker rm -f hostel-backend
docker run -d hostelcms/hostel-complaint-system:v1.0.5
       ↓
App live at http://localhost:8080  ✅
```
