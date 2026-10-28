// =============================================================================
// Task 10 — Continuous Testing in Jenkins
// -----------------------------------------------------------------------------
// Extends the Task 8 pipeline (Checkout → Build → Package → Deploy) with a
// Selenium quality gate:
//
//   Checkout → Build → Package → Deploy → Selenium Tests
//
// Rules:
//   • JUnit XML report published after every run (pass OR fail).
//   • Failure screenshots archived as pipeline artefacts.
//   • A FAILED Selenium stage calls error() so the pipeline stops and the
//     deploy is NOT considered successful — deployment is blocked.
//   • SKIP_SELENIUM param lets operators bypass the gate in an emergency.
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
            description: 'dev => http://localhost:3000  |  staging => http://localhost:3001'
        )
        booleanParam(
            name: 'SKIP_SELENIUM',
            defaultValue: false,
            description: 'Emergency bypass: skip Selenium quality gate (do NOT use in normal runs)'
        )
    }

    environment {
        MAVEN_CMD    = "${env.USERPROFILE}\\.jenkins-tools\\apache-maven-3.9.6\\bin\\mvn.cmd"
        VITE_API_URL = '/api'
    }

    stages {

        // ── 1. Checkout ────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "==> [Checkout] Pulling source"
                checkout scm
                echo "Branch  : ${env.GIT_BRANCH}"
                echo "Commit  : ${env.GIT_COMMIT}"
                echo "Build # : ${env.BUILD_NUMBER}"
                echo "Env     : ${params.DEPLOY_ENVIRONMENT}"
            }
        }

        // ── 2. Build ───────────────────────────────────────────────────────────
        stage('Build') {
            steps {
                echo "==> [Build] Maven clean verify (backend)"
                bat "call \"%MAVEN_CMD%\" -B -f backend\\pom.xml clean verify"

                echo "==> [Build] Vite production build (frontend)"
                dir('frontend') {
                    bat 'npm ci'
                    withEnv(["VITE_API_URL=${env.VITE_API_URL}"]) {
                        bat 'npm run build'
                    }
                }
            }
            post {
                always {
                    // Publish unit-test report from the Build stage
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        // ── 3. Package ─────────────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo "==> [Package] Archiving build artefacts"
                archiveArtifacts(
                    artifacts: 'backend/target/complaint-system-1.0.0.jar,frontend/dist/**',
                    fingerprint: true,
                    allowEmptyArchive: false
                )
            }
        }

        // ── 4. Deploy ──────────────────────────────────────────────────────────
        stage('Deploy') {
            steps {
                script {
                    def envName      = params.DEPLOY_ENVIRONMENT
                    def webPort      = (envName == 'dev') ? '3000' : '3001'
                    def backendPort  = (envName == 'dev') ? '8080' : '8081'
                    def databasePort = (envName == 'dev') ? '5433' : '5434'
                    def projectName  = "hostel-${envName}"

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

        // ── 5. Selenium Tests  ← Task 10 quality gate ─────────────────────────
        //
        //   Runs the full TestNG suite against the deployed application.
        //   • Publishes the JUnit XML report on every outcome.
        //   • Archives failure screenshots as pipeline artefacts.
        //   • Calls error() on failure so the pipeline STOPS and the deploy
        //     step that just ran is marked as quality-gate-blocked.
        //   • Controlled by SKIP_SELENIUM emergency parameter.
        // ──────────────────────────────────────────────────────────────────────
        stage('Selenium Tests') {
            when {
                expression { return !params.SKIP_SELENIUM }
            }
            steps {
                script {
                    def appUrl = (params.DEPLOY_ENVIRONMENT == 'dev')
                            ? 'http://localhost:3000'
                            : 'http://localhost:3001'
                    echo "==> [Selenium] Running suite against ${appUrl}"

                    bat "call \"%MAVEN_CMD%\" -B -f tests\\pom.xml clean test -Dapp.url=${appUrl}"
                }
            }
            post {
                always {
                    // ── Publish JUnit test report ──────────────────────────
                    junit allowEmptyResults: true,
                          testResults: 'tests/target/surefire-reports/*.xml'

                    // ── Archive failure screenshots as pipeline artefacts ──
                    archiveArtifacts(
                        artifacts: 'tests/test-screenshots/**/*.png',
                        allowEmptyArchive: true,
                        fingerprint: false
                    )

                    echo "==> [Selenium] Test report published"
                }
                failure {
                    // ── Block deployment: failed tests stop the pipeline ───
                    echo "==> [Selenium] TESTS FAILED — deployment blocked!"
                    error("Selenium quality gate failed. Fix the defect and recommit.")
                }
            }
        }
    }

    post {
        always {
            echo "================================================="
            echo "Pipeline complete"
            echo "Environment : ${params.DEPLOY_ENVIRONMENT}"
            echo "Build #     : ${env.BUILD_NUMBER}"
            echo "Result      : ${currentBuild.currentResult}"
            echo "================================================="
        }
        success {
            script {
                def url = (params.DEPLOY_ENVIRONMENT == 'dev')
                        ? 'http://localhost:3000' : 'http://localhost:3001'
                echo "BUILD SUCCESS — Application live at ${url}"
            }
        }
        failure {
            echo "BUILD FAILED — inspect the failing stage above"
        }
    }
}
