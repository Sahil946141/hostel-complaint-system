// =============================================================================
// Task 8 — Pipeline as Code and Server Deployment (Nginx)
// -----------------------------------------------------------------------------
// Declarative Jenkins pipeline with four stages: Checkout, Build, Package,
// Deploy. The React UI is served through Nginx which also reverse-proxies
// /api/* to the Spring Boot backend, both running inside Docker Compose.
//
// Required stages for Task 8: checkout -> build -> package -> deploy
// Parameterized: DEPLOY_ENVIRONMENT (dev | staging) changes the host ports and
//                the isolated Compose project so each environment runs side by
//                side without port or database-volume collisions.
// Deploy target: Nginx (frontend container) with /api proxy to Tomcat-embedded
//                Spring Boot (backend container).
// =============================================================================

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // ---- At least one parameterized environment setting (Task 8 requirement) ----
    parameters {
        choice(
            name: 'DEPLOY_ENVIRONMENT',
            choices: ['dev', 'staging'],
            description: 'Target environment. dev => http://localhost:3000, staging => http://localhost:3001'
        )
    }

    environment {
        // Maven used by the Jenkins agent (Windows). Matches the tool installed
        // for Task 6's CI job under %USERPROFILE%\.jenkins-tools.
        MAVEN_CMD = "${env.USERPROFILE}\\.jenkins-tools\\apache-maven-3.9.6\\bin\\mvn.cmd"
        // Vite build-time API base path; Nginx proxies /api to the backend.
        VITE_API_URL = '/api'
    }

    stages {

        // ---------------------------------------------------------------
        // STAGE 1 — Checkout: pull the source from the configured SCM
        // ---------------------------------------------------------------
        stage('Checkout') {
            steps {
                echo "==> [Checkout] Pulling source from SCM"
                checkout scm
                echo "Branch  : ${env.GIT_BRANCH}"
                echo "Commit  : ${env.GIT_COMMIT}"
                echo "Build # : ${env.BUILD_NUMBER}"
                echo "Env     : ${params.DEPLOY_ENVIRONMENT}"
            }
        }

        // ---------------------------------------------------------------
        // STAGE 2 — Build: compile backend (Maven) + frontend (Vite)
        // ---------------------------------------------------------------
        stage('Build') {
            steps {
                echo "==> [Build] Compiling Spring Boot backend with Maven"
                bat "call \"%MAVEN_CMD%\" -B -f backend\\pom.xml clean verify"

                echo "==> [Build] Building React frontend with Vite"
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

        // ---------------------------------------------------------------
        // STAGE 3 — Package: archive the executable JAR + frontend dist
        // ---------------------------------------------------------------
        stage('Package') {
            steps {
                echo "==> [Package] Archiving build artifacts"
                archiveArtifacts(
                    artifacts: 'backend/target/complaint-system-1.0.0.jar,frontend/dist/**',
                    fingerprint: true,
                    allowEmptyArchive: false
                )
            }
        }

        // ---------------------------------------------------------------
        // STAGE 4 — Deploy: build images and start the stack via Compose
        //            Nginx serves the UI and proxies /api to Spring Boot.
        // ---------------------------------------------------------------
        stage('Deploy') {
            steps {
                script {
                    def envName = params.DEPLOY_ENVIRONMENT
                    // Parameterized environment setting: each env owns its ports
                    // and Compose project name + isolated database volume.
                    def webPort       = (envName == 'dev') ? '3000' : '3001'
                    def backendPort   = (envName == 'dev') ? '8080' : '8081'
                    def databasePort  = (envName == 'dev') ? '5433' : '5434'
                    def projectName   = "hostel-${envName}"

                    echo "==> [Deploy] Environment=${envName} project=${projectName} web=${webPort} api=${backendPort} db=${databasePort}"

                    withEnv([
                        "FRONTEND_HOST_PORT=${webPort}",
                        "BACKEND_HOST_PORT=${backendPort}",
                        "POSTGRES_HOST_PORT=${databasePort}",
                        "VITE_API_URL=${env.VITE_API_URL}"
                    ]) {
                        // Validate the resolved Compose file before bringing it up.
                        bat "docker compose -p ${projectName} config --quiet"

                        // Build images and start all services (db -> backend -> frontend),
                        // waiting until they are healthy.
                        bat "docker compose -p ${projectName} up --detach --build --wait --wait-timeout 180"

                        // Smoke-test the deployed application URLs.
                        bat 'curl.exe --fail --silent --show-error "http://localhost:%FRONTEND_HOST_PORT%/"'
                        bat 'curl.exe --fail --silent --show-error "http://localhost:%FRONTEND_HOST_PORT%/api/auth/health"'
                    }

                    echo "==> [Deploy] ${envName} is live at http://localhost:${webPort}/"
                }
            }
        }
    }

    post {
        always {
            echo "================================================="
            echo "Pipeline finished"
            echo "Environment : ${params.DEPLOY_ENVIRONMENT}"
            echo "Build #     : ${env.BUILD_NUMBER}"
            echo "Result      : ${currentBuild.currentResult}"
            echo "================================================="
        }
        success {
            script {
                def url = (params.DEPLOY_ENVIRONMENT == 'dev') ? 'http://localhost:3000' : 'http://localhost:3001'
                echo "BUILD SUCCESS — Application available at ${url}"
            }
        }
        failure {
            echo "BUILD FAILED — Inspect the failing stage log above"
        }
    }
}
