pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        choice(
            name: 'DEPLOY_ENVIRONMENT',
            choices: ['dev', 'staging'],
            description: 'Environment to deploy through Nginx'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Deploying ${env.GIT_BRANCH} at ${env.GIT_COMMIT}"
            }
        }

        stage('Build') {
            steps {
                bat 'call "%USERPROFILE%\\.jenkins-tools\\apache-maven-3.9.6\\bin\\mvn.cmd" -B -f backend\\pom.xml clean verify'
                dir('frontend') {
                    bat 'npm ci'
                    withEnv(['VITE_API_URL=/api']) {
                        bat 'npm run build'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                archiveArtifacts(
                    artifacts: 'backend/target/complaint-system-1.0.0.jar,frontend/dist/**',
                    fingerprint: true,
                    allowEmptyArchive: false
                )
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def webPort = params.DEPLOY_ENVIRONMENT == 'dev' ? '3000' : '3001'
                    def databasePort = params.DEPLOY_ENVIRONMENT == 'dev' ? '5433' : '5434'

                    withEnv([
                        "FRONTEND_HOST_PORT=${webPort}",
                        "POSTGRES_HOST_PORT=${databasePort}",
                        'VITE_API_URL=/api'
                    ]) {
                        bat "docker compose -p hostel-${params.DEPLOY_ENVIRONMENT} config --quiet"
                        bat "docker compose -p hostel-${params.DEPLOY_ENVIRONMENT} up --detach --build --wait --wait-timeout 180"
                        bat 'curl.exe --fail --silent --show-error "http://localhost:%FRONTEND_HOST_PORT%/"'
                        bat 'curl.exe --fail --silent --show-error "http://localhost:%FRONTEND_HOST_PORT%/api/auth/health"'
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline completed with status: ${currentBuild.currentResult}"
        }
        success {
            echo "Application is available at http://localhost:${params.DEPLOY_ENVIRONMENT == 'dev' ? '3000' : '3001'}"
        }
    }
}
