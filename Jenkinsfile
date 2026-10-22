pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Target deployment environment'
        )
        string(
            name: 'DOCKER_IMAGE_TAG',
            defaultValue: "${env.BUILD_NUMBER ?: 'latest'}",
            description: 'Docker image tag (e.g. v1.0.0, latest, build number)'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip Selenium test execution (use only in emergency)'
        )
    }

    environment {
        MAVEN_HOME      = 'E:\\Devops_23102A0008\\tools\\apache-maven-3.9.6'
        JAVA_HOME       = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.8-hotspot'
        PATH            = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;${env.PATH}"
        APP_NAME        = 'hostel-complaint-system'
        DOCKER_HUB_REPO = 'hostelcms'
        IMAGE_NAME      = "${DOCKER_HUB_REPO}/${APP_NAME}"
        DOCKER_TAG      = "${params.DOCKER_IMAGE_TAG ?: env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "==> [STAGE 1] Checking out source code..."
                checkout scm
                echo "Branch  : ${env.GIT_BRANCH}"
                echo "Commit  : ${env.GIT_COMMIT}"
                echo "Build # : ${env.BUILD_NUMBER}"
            }
        }

        stage('Build') {
            steps {
                echo "==> [STAGE 2] Compiling Spring Boot application..."
                dir('backend') {
                    bat "mvn clean compile -q"
                }
                echo "==> Compile SUCCESS"
            }
        }

        stage('Unit Test') {
            when {
                expression { return !params.SKIP_TESTS }
            }
            steps {
                echo "==> [STAGE 3] Running unit tests..."
                dir('backend') {
                    bat "mvn test"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'
                }
                failure {
                    echo "==> UNIT TESTS FAILED! Blocking deployment."
                    error("Unit tests failed. Fix before deploying.")
                }
            }
        }

        stage('Package') {
            steps {
                echo "==> [STAGE 4] Packaging as JAR artifact..."
                dir('backend') {
                    bat "mvn package -DskipTests -q"
                }
                echo "==> Artifact: backend/target/complaint-system-1.0.0.jar"
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/target/*.jar',
                                     fingerprint: true,
                                     allowEmptyArchive: false
                    echo "==> Artifact archived successfully"
                }
            }
        }

        stage('Selenium Tests') {
            when {
                expression { return !params.SKIP_TESTS }
            }
            steps {
                echo "==> [STAGE 5] Running Selenium UI tests..."
                dir('tests') {
                    bat "mvn test -Dapp.url=http://localhost:3000"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'tests/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'tests/test-screenshots/**/*.png',
                                     allowEmptyArchive: true
                }
                failure {
                    echo "==> SELENIUM TESTS FAILED! Blocking Docker deployment."
                    error("Selenium tests failed. Fix the defect and recommit.")
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "==> [STAGE 6] Building Docker image ${IMAGE_NAME}:${DOCKER_TAG}..."
                dir('backend') {
                    bat "docker build -t ${IMAGE_NAME}:${DOCKER_TAG} ."
                    bat "docker tag ${IMAGE_NAME}:${DOCKER_TAG} ${IMAGE_NAME}:latest"
                }
                echo "==> Docker image built: ${IMAGE_NAME}:${DOCKER_TAG}"
            }
        }

        stage('Docker Push') {
            steps {
                echo "==> [STAGE 7] Pushing to Docker Hub..."
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    bat "docker push ${IMAGE_NAME}:${DOCKER_TAG}"
                    bat "docker push ${IMAGE_NAME}:latest"
                    bat "docker logout"
                }
                echo "==> Image pushed to registry"
            }
        }

        stage('Deploy Container') {
            steps {
                echo "==> [STAGE 8] Deploying to ${params.ENVIRONMENT}..."
                bat """
                    docker-compose down --remove-orphans || echo "No existing containers"
                    docker-compose pull
                    docker-compose up -d
                    timeout /t 30 /nobreak
                    docker ps --filter name=hostel
                """
                echo "==> Deployment SUCCESS on ${params.ENVIRONMENT}"
            }
        }
    }

    post {
        always {
            echo "==============================="
            echo "Pipeline finished"
            echo "Environment : ${params.ENVIRONMENT}"
            echo "Build       : ${env.BUILD_NUMBER}"
            echo "Status      : ${currentBuild.currentResult}"
            echo "==============================="
        }
        success {
            echo "BUILD SUCCESS — App deployed at http://localhost:3000"
        }
        failure {
            echo "BUILD FAILED — Check stage logs above"
        }
        cleanup {
            cleanWs()
        }
    }
}
