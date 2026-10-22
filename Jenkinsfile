pipeline {
    agent any

    // ──────────────────────────────────────────
    // PARAMETERS (Task 7 - parameterize env)
    // ──────────────────────────────────────────
    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Target deployment environment'
        )
        string(
            name: 'DOCKER_IMAGE_TAG',
            defaultValue: 'latest',
            description: 'Docker image tag (e.g. v1.0.0, latest)'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip test execution'
        )
    }

    // ──────────────────────────────────────────
    // ENVIRONMENT VARIABLES
    // ──────────────────────────────────────────
    environment {
        MAVEN_HOME      = 'E:\\Devops_23102A0008\\tools\\apache-maven-3.9.6'
        JAVA_HOME       = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.8-hotspot'
        PATH            = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;${env.PATH}"
        APP_NAME        = 'hostel-complaint-system'
        DOCKER_HUB_REPO = 'hostelcms'                  // change to your Docker Hub username
        IMAGE_NAME      = "${DOCKER_HUB_REPO}/${APP_NAME}"
        DOCKER_TAG      = "${params.DOCKER_IMAGE_TAG}"
    }

    // ──────────────────────────────────────────
    // STAGES
    // ──────────────────────────────────────────
    stages {

        // STAGE 1: Checkout
        stage('Checkout') {
            steps {
                echo "==> Checking out source code..."
                checkout scm
                echo "Branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }

        // STAGE 2: Build
        stage('Build') {
            steps {
                echo "==> Building Spring Boot application..."
                dir('backend') {
                    bat 'mvn clean compile -q'
                }
                echo "==> Build SUCCESS"
            }
        }

        // STAGE 3: Test (can be skipped via parameter)
        stage('Test') {
            when {
                expression { return !params.SKIP_TESTS }
            }
            steps {
                echo "==> Running unit tests..."
                dir('backend') {
                    bat 'mvn test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'
                }
                failure {
                    echo "==> Tests FAILED! Stopping pipeline."
                    error("Tests failed. Deployment blocked.")
                }
            }
        }

        // STAGE 4: Package
        stage('Package') {
            steps {
                echo "==> Packaging application as JAR..."
                dir('backend') {
                    bat 'mvn package -DskipTests -q'
                }
                echo "==> Artifact created: backend/target/complaint-system-1.0.0.jar"
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/target/*.jar',
                                     fingerprint: true,
                                     allowEmptyArchive: false
                }
            }
        }

        // STAGE 5: Docker Build
        stage('Docker Build') {
            steps {
                echo "==> Building Docker image: ${IMAGE_NAME}:${DOCKER_TAG}"
                dir('backend') {
                    bat "docker build -t ${IMAGE_NAME}:${DOCKER_TAG} ."
                    bat "docker tag ${IMAGE_NAME}:${DOCKER_TAG} ${IMAGE_NAME}:latest"
                }
                echo "==> Docker image built successfully"
            }
        }

        // STAGE 6: Docker Push (to Docker Hub)
        stage('Docker Push') {
            steps {
                echo "==> Pushing image to Docker Hub..."
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    bat "docker push ${IMAGE_NAME}:${DOCKER_TAG}"
                    bat "docker push ${IMAGE_NAME}:latest"
                }
            }
        }

        // STAGE 7: Deploy
        stage('Deploy') {
            steps {
                echo "==> Deploying to ${params.ENVIRONMENT} environment..."
                bat """
                    docker-compose down || echo "No running containers"
                    docker-compose up -d --build
                    docker ps
                """
                echo "==> Deployment SUCCESSFUL to ${params.ENVIRONMENT}"
            }
        }
    }

    // ──────────────────────────────────────────
    // POST ACTIONS
    // ──────────────────────────────────────────
    post {
        always {
            echo "==> Pipeline completed. Environment: ${params.ENVIRONMENT}"
        }
        success {
            echo "==> BUILD SUCCESS — Application deployed!"
        }
        failure {
            echo "==> BUILD FAILED — Check logs above for errors."
        }
        cleanup {
            cleanWs()
        }
    }
}
