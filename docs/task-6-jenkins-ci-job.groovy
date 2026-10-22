// Task 6 — Jenkins CI Job Configuration
// Freestyle Job equivalent as a Pipeline script

pipeline {
    agent any

    // Task 6: Trigger on GitHub push or scheduled polling
    triggers {
        pollSCM('H/5 * * * *')   // Poll every 5 minutes
        githubPush()              // Webhook trigger
    }

    tools {
        maven 'Maven-3.9.6'
        jdk   'Java-21'
    }

    environment {
        BUILD_DIR   = 'backend'
        ARTIFACT    = 'backend/target/complaintsystem-0.0.1-SNAPSHOT.jar'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '=== Task 6: Checkout Source Code ==='
                checkout scm
                sh 'git log --oneline -5'
            }
        }

        stage('Build') {
            steps {
                echo '=== Task 6: Maven Build ==='
                dir('backend') {
                    sh 'mvn clean compile -B'
                }
            }
        }

        stage('Test') {
            steps {
                echo '=== Task 6: Unit Tests ==='
                dir('backend') {
                    sh 'mvn test -B'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo '=== Task 6: Package as JAR ==='
                dir('backend') {
                    sh 'mvn package -DskipTests -B'
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                echo '=== Task 6: Archiving Build Artifact ==='
                archiveArtifacts artifacts: 'backend/target/*.jar',
                                 fingerprint: true,
                                 allowEmptyArchive: false
                echo "Artifact archived: ${env.ARTIFACT}"
            }
        }
    }

    post {
        success {
            echo '✅ Task 6 CI Build SUCCESS — Artifact archived'
        }
        failure {
            echo '❌ Task 6 CI Build FAILED — Check logs above'
        }
    }
}
