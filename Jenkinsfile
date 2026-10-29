// =============================================================================
// Task 12 — Jenkins-Docker Continuous Deployment
// -----------------------------------------------------------------------------
// Extends the Task 10 pipeline (Checkout → Build → Package → Deploy → Selenium)
// with two new stages that complete the commit-to-container pipeline:
//
//   Checkout → Build → Package → Deploy → Selenium Tests
//                                              │  (only on pass)
//                                              ▼
//                                     Docker Build & Tag
//                                              │
//                                              ▼
//                                       Docker Push  (registry)
//                                              │
//                                              ▼
//                                      CD Deploy  (fresh container)
//
// New stages:
//   6. Docker Build & Tag  — builds versioned backend and frontend images
//   7. Docker Push         — pushes both tags to the local Docker Registry
//   9. CD Deploy           — pulls the published image and replaces the test stack
//
// Parameters:
//   DEPLOY_ENVIRONMENT  — dev | staging  (controls ports, same as before)
//   LOCAL_REGISTRY      — local registry host (default: localhost:5000)
//   IMAGE_VERSION       — semantic version tag (default: 1.0.0)
//   SKIP_SELENIUM       — emergency bypass for Selenium gate
// =============================================================================

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        choice(
            name: 'DEPLOY_ENVIRONMENT',
            choices: ['dev', 'staging'],
            description: 'Task 12 deployment: dev → :3100 | staging → :3101'
        )
        string(
            name: 'LOCAL_REGISTRY',
            defaultValue: 'localhost:5000',
            description: 'Local Docker Registry v2 endpoint'
        )
        string(
            name: 'IMAGE_VERSION',
            defaultValue: '1.0.0',
            description: 'Semantic version tag applied alongside the build number'
        )
        booleanParam(
            name: 'SKIP_SELENIUM',
            defaultValue: false,
            description: 'Emergency bypass — skip Selenium quality gate'
        )
    }

    environment {
        MAVEN_CMD    = "${env.USERPROFILE}\\.jenkins-tools\\apache-maven-3.9.6\\bin\\mvn.cmd"
        VITE_API_URL = '/api'

        // Versioned image names, published to the local Registry v2 instance.
        BACKEND_IMAGE  = "${params.LOCAL_REGISTRY}/hostelcms/hostel-complaint-system-backend"
        FRONTEND_IMAGE = "${params.LOCAL_REGISTRY}/hostelcms/hostel-complaint-system-frontend"
        VERSION_TAG    = "${params.IMAGE_VERSION}-${env.BUILD_NUMBER}"
    }

    stages {

        // ── 1. Checkout ────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "==> [Checkout] Pulling source"
                checkout scm
                echo "Branch      : ${env.GIT_BRANCH}"
                echo "Commit      : ${env.GIT_COMMIT}"
                echo "Build #     : ${env.BUILD_NUMBER}"
                echo "Environment : ${params.DEPLOY_ENVIRONMENT}"
                echo "Image       : ${env.BACKEND_IMAGE}:${env.VERSION_TAG}"
                script {
                    if (!(params.IMAGE_VERSION ==~ /\d+\.\d+\.\d+/)) {
                        error('IMAGE_VERSION must use semantic version format, for example 1.0.0')
                    }
                    if (!(params.LOCAL_REGISTRY ==~ /[A-Za-z0-9.-]+:\d+/)) {
                        error('LOCAL_REGISTRY must be a host and port, for example localhost:5000')
                    }
                }
            }
        }

        // ── 2. Build ───────────────────────────────────────────────────────────
        stage('Build') {
            steps {
                echo "==> [Build] Maven clean verify"
                bat "call \"%MAVEN_CMD%\" -B -f backend\\pom.xml clean verify"

                echo "==> [Build] Vite production build"
                dir('frontend') {
                    bat 'npm ci'
                    withEnv(["VITE_API_URL=${env.VITE_API_URL}"]) {
                        bat 'npm run build'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        // ── 3. Package ─────────────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo "==> [Package] Archiving JAR and frontend dist"
                archiveArtifacts(
                    artifacts: 'backend/target/complaint-system-1.0.0.jar,frontend/dist/**',
                    fingerprint: true,
                    allowEmptyArchive: false
                )
            }
        }

        // ── 4. Deploy (Compose) ────────────────────────────────────────────────
        // Brings up the full stack via docker compose so Selenium can test it.
        stage('Deploy') {
            steps {
                script {
                    def envName      = params.DEPLOY_ENVIRONMENT
                    def webPort      = (envName == 'dev') ? '3100' : '3101'
                    def backendPort  = (envName == 'dev') ? '8180' : '8181'
                    def databasePort = (envName == 'dev') ? '55432' : '55433'
                    def projectName  = "hostel-task12-${envName}"

                    echo "==> [Deploy] ${envName} | web=${webPort} api=${backendPort} db=${databasePort}"

                    withEnv([
                        "FRONTEND_HOST_PORT=${webPort}",
                        "BACKEND_HOST_PORT=${backendPort}",
                        "POSTGRES_HOST_PORT=${databasePort}",
                        "VITE_API_URL=${env.VITE_API_URL}"
                    ]) {
                        bat "docker compose -p ${projectName} config --quiet"
                        bat "docker compose -p ${projectName} up --detach --build --wait --wait-timeout 180"
                        bat 'curl.exe --fail --silent --show-error "http://localhost:%FRONTEND_HOST_PORT%/"'
                        bat 'curl.exe --fail --silent --show-error "http://localhost:%FRONTEND_HOST_PORT%/api/auth/health"'
                    }

                    echo "==> [Deploy] Live at http://localhost:${webPort}/"
                }
            }
        }

        // ── 5. Selenium Tests (quality gate — Task 10) ─────────────────────────
        // Failed tests call error() → Docker Build/Push/CDDeploy stages are skipped.
        stage('Selenium Tests') {
            when {
                expression { return !params.SKIP_SELENIUM }
            }
            steps {
                script {
                    def appUrl = (params.DEPLOY_ENVIRONMENT == 'dev')
                            ? 'http://localhost:3100' : 'http://localhost:3101'
                    echo "==> [Selenium] Suite against ${appUrl}"
                    bat "call \"%MAVEN_CMD%\" -B -f tests\\pom.xml clean test \"-Dapp.url=${appUrl}\""
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'tests/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'tests/test-screenshots/**/*.png',
                                     allowEmptyArchive: true
                    echo "==> [Selenium] Report published"
                }
                failure {
                    echo "==> [Selenium] TESTS FAILED — Docker publish blocked!"
                    error("Selenium quality gate failed. Docker image will NOT be published.")
                }
            }
        }

        // ── 6. Docker Build & Tag  ← Task 12 NEW ──────────────────────────────
        // Builds backend and frontend images with two tags:
        //   localhost:5000/hostelcms/hostel-complaint-system-backend:1.0.0-<BUILD_NUMBER>
        //   localhost:5000/hostelcms/hostel-complaint-system-backend:latest
        stage('Docker Build & Tag') {
            steps {
                echo "==> [Docker Build] Building versioned images"
                echo "    Backend : ${env.BACKEND_IMAGE}:${env.VERSION_TAG}"
                echo "    Frontend: ${env.FRONTEND_IMAGE}:${env.VERSION_TAG}"

                // Backend image
                bat "docker build -t %BACKEND_IMAGE%:%VERSION_TAG% ./backend"
                bat "docker tag  %BACKEND_IMAGE%:%VERSION_TAG% %BACKEND_IMAGE%:latest"

                // Frontend image
                bat "docker build -t %FRONTEND_IMAGE%:%VERSION_TAG% --build-arg VITE_API_URL=/api ./frontend"
                bat "docker tag  %FRONTEND_IMAGE%:%VERSION_TAG% %FRONTEND_IMAGE%:latest"

                echo "==> [Docker Build] Images built and tagged"
                bat "docker image inspect --format \"{{.RepoTags}} | ID={{.Id}} | Size={{.Size}}\" %BACKEND_IMAGE%:%VERSION_TAG%"
                bat "docker image inspect --format \"{{.RepoTags}} | ID={{.Id}} | Size={{.Size}}\" %FRONTEND_IMAGE%:%VERSION_TAG%"
            }
        }

        // ── 7. Ensure local Docker Registry ───────────────────────────────────
        stage('Ensure Local Registry') {
            steps {
                script {
                    def registryExists = bat(
                        returnStatus: true,
                        script: 'docker container inspect hostel-task12-registry >NUL 2>&1'
                    )
                    if (registryExists == 0) {
                        env.REGISTRY_COMMAND = 'docker start hostel-task12-registry'
                        bat 'docker start hostel-task12-registry'
                    } else {
                        env.REGISTRY_COMMAND = "docker run --detach --restart unless-stopped --name hostel-task12-registry --publish ${params.LOCAL_REGISTRY.substring(params.LOCAL_REGISTRY.lastIndexOf(':') + 1)}:5000 --volume hostel-task12-registry-data:/var/lib/registry registry:2"
                        bat env.REGISTRY_COMMAND
                    }
                    bat "curl.exe --fail --silent --show-error http://${params.LOCAL_REGISTRY}/v2/"
                }
            }
        }

        // ── 8. Docker Push ────────────────────────────────────────────────────
        // Pushes versioned and latest tags to the local Registry v2.
        stage('Docker Push') {
            steps {
                echo "==> [Docker Push] Publishing to ${params.LOCAL_REGISTRY}"
                bat "docker push %BACKEND_IMAGE%:%VERSION_TAG%"
                bat "docker push %BACKEND_IMAGE%:latest"
                bat "docker push %FRONTEND_IMAGE%:%VERSION_TAG%"
                bat "docker push %FRONTEND_IMAGE%:latest"
            }
        }

        // ── 9. CD Deploy (fresh container)  ← Task 12 NEW ─────────────────────
        // Pulls the freshly published image from the registry and deploys a
        // new container, replacing the compose stack that was used for testing.
        // This proves the published image is independently deployable.
        stage('CD Deploy') {
            steps {
                script {
                    def envName      = params.DEPLOY_ENVIRONMENT
                    def webPort      = (envName == 'dev') ? '3100' : '3101'
                    def backendPort  = (envName == 'dev') ? '8180' : '8181'
                    def databasePort = (envName == 'dev') ? '55432' : '55433'
                    def projectName  = "hostel-task12-${envName}"

                    echo "==> [CD Deploy] Redeploying from registry image ${env.BACKEND_IMAGE}:${env.VERSION_TAG}"

                    // Pull the newly pushed image from the registry
                    bat "docker pull %BACKEND_IMAGE%:%VERSION_TAG%"
                    bat "docker pull %FRONTEND_IMAGE%:%VERSION_TAG%"

                    // Redeploy using the CD override file which replaces build: with image:
                    // so the stack uses the registry-pulled image, not a local build.
                    withEnv([
                        "FRONTEND_HOST_PORT=${webPort}",
                        "BACKEND_HOST_PORT=${backendPort}",
                        "POSTGRES_HOST_PORT=${databasePort}",
                        "VITE_API_URL=${env.VITE_API_URL}",
                        "BACKEND_IMAGE=${env.BACKEND_IMAGE}:${env.VERSION_TAG}",
                        "FRONTEND_IMAGE=${env.FRONTEND_IMAGE}:${env.VERSION_TAG}"
                    ]) {
                        bat "docker compose -p ${projectName} -f docker-compose.yml -f docker-compose.cd.yml up --detach --pull always --wait --wait-timeout 180"
                        bat "docker compose -p ${projectName} -f docker-compose.yml -f docker-compose.cd.yml ps"
                    }

                    // Final smoke test — proves the CD-deployed container responds
                    bat "curl.exe --fail --silent --show-error \"http://localhost:${webPort}/\""
                    bat "curl.exe --fail --silent --show-error \"http://localhost:${webPort}/api/auth/health\""

                    echo "==> [CD Deploy] Fresh container live at http://localhost:${webPort}/"
                    bat "docker image inspect --format \"{{.RepoTags}} | ID={{.Id}} | Size={{.Size}}\" %BACKEND_IMAGE%:%VERSION_TAG%"
                    bat "docker image inspect --format \"{{.RepoTags}} | ID={{.Id}} | Size={{.Size}}\" %FRONTEND_IMAGE%:%VERSION_TAG%"
                    bat "docker ps --filter label=com.docker.compose.project=${projectName} --format \"table {{.Names}}\\t{{.Image}}\\t{{.Status}}\\t{{.Ports}}\""
                    writeFile file: 'docker-command-log.txt', text: """\
${env.REGISTRY_COMMAND}
docker build -t ${env.BACKEND_IMAGE}:${env.VERSION_TAG} ./backend
docker tag ${env.BACKEND_IMAGE}:${env.VERSION_TAG} ${env.BACKEND_IMAGE}:latest
docker build -t ${env.FRONTEND_IMAGE}:${env.VERSION_TAG} --build-arg VITE_API_URL=/api ./frontend
docker tag ${env.FRONTEND_IMAGE}:${env.VERSION_TAG} ${env.FRONTEND_IMAGE}:latest
docker push ${env.BACKEND_IMAGE}:${env.VERSION_TAG}
docker push ${env.BACKEND_IMAGE}:latest
docker push ${env.FRONTEND_IMAGE}:${env.VERSION_TAG}
docker push ${env.FRONTEND_IMAGE}:latest
docker pull ${env.BACKEND_IMAGE}:${env.VERSION_TAG}
docker pull ${env.FRONTEND_IMAGE}:${env.VERSION_TAG}
docker compose -p ${projectName} -f docker-compose.yml -f docker-compose.cd.yml up --detach --pull always --wait --wait-timeout 180
docker compose -p ${projectName} -f docker-compose.yml -f docker-compose.cd.yml ps
""".stripIndent()
                    bat 'docker image inspect --format "{{.RepoTags}} | ID={{.Id}} | Size={{.Size}} bytes" %BACKEND_IMAGE%:%VERSION_TAG% > docker-image-details.txt'
                    bat 'docker image inspect --format "{{.RepoTags}} | ID={{.Id}} | Size={{.Size}} bytes" %FRONTEND_IMAGE%:%VERSION_TAG% >> docker-image-details.txt'
                    bat "docker ps --filter label=com.docker.compose.project=${projectName} --format \"table {{.Names}}\\t{{.Image}}\\t{{.Status}}\\t{{.Ports}}\" > docker-container-status.txt"
                    bat 'type docker-command-log.txt'
                    bat 'type docker-image-details.txt'
                    bat 'type docker-container-status.txt'
                    archiveArtifacts artifacts: 'docker-command-log.txt,docker-image-details.txt,docker-container-status.txt', fingerprint: true
                    echo "==> Commit-to-container pipeline COMPLETE"
                }
            }
        }
    }

    post {
        always {
            echo "================================================="
            echo "Pipeline complete"
            echo "Environment : ${params.DEPLOY_ENVIRONMENT}"
            echo "Image       : ${env.BACKEND_IMAGE}:${env.VERSION_TAG}"
            echo "Build #     : ${env.BUILD_NUMBER}"
            echo "Result      : ${currentBuild.currentResult}"
            echo "================================================="
        }
        success {
            script {
                def url = (params.DEPLOY_ENVIRONMENT == 'dev') ? 'http://localhost:3100' : 'http://localhost:3101'
                echo "BUILD SUCCESS"
                echo "Application : ${url}"
                echo "Image       : ${env.BACKEND_IMAGE}:${env.VERSION_TAG}"
            }
        }
        failure {
            echo "BUILD FAILED — inspect the failing stage above"
        }
    }
}
