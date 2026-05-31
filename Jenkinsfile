pipeline {
    agent any

    environment {
        // Path to your Android SDK
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        PATH = "${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    // Jenkins "Pipeline" jobs use GIT_BRANCH, "Multibranch" use BRANCH_NAME
                    def fullBranch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: "unknown"
                    // Strip "origin/" if present
                    env.CURRENT_BRANCH = fullBranch.contains("/") ? fullBranch.split("/")[-1] : fullBranch
                    
                    // Determine if this is a main branch (master or main)
                    env.IS_MAIN_BRANCH = (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main').toString()
                    
                    echo "Branch detected: ${env.CURRENT_BRANCH}"
                    echo "Is Main Branch: ${env.IS_MAIN_BRANCH}"
                }
            }
        }

        stage('Build') {
            steps {
                echo "--- Compiling Application ---"
                sh 'chmod +x gradlew'
                script {
                    if (env.IS_MAIN_BRANCH == 'true') {
                        sh './gradlew assembleDevDebug assembleProdDebug'
                    } else {
                        sh './gradlew assembleDevDebug'
                    }
                }
            }
        }

        stage('Unit Test and Code Coverage') {
            steps {
                echo "--- Running Unit Tests & Verifying Coverage ---"
                script {
                    if (env.IS_MAIN_BRANCH == 'true') {
                        // Test both flavors on main
                        sh './gradlew testDevDebugUnitTest testProdDebugUnitTest'
                    } else {
                        sh './gradlew testDevDebugUnitTest'
                    }
                    // Run consolidated coverage verification (80% as configured in build.gradle.kts)
                    sh './gradlew jacocoCoverageVerification'
                }
            }
        }

        stage('Deploy to Staging') {
            steps {
                echo "--- Building Staging (Dev) APK ---"
                sh './gradlew :app:assembleDevRelease'
            }
        }

        stage('FVT') {
            steps {
                echo "--- Functional Verification Testing (UI Tests) ---"
                // Runs UI tests on dev flavor to verify staging build
                sh './gradlew connectedDevDebugAndroidTest'
            }
        }

        stage('Gate') {
            when {
                expression { env.IS_MAIN_BRANCH == 'true' }
            }
            steps {
                echo "--- Quality Gate Passed: Branch is ${env.CURRENT_BRANCH} ---"
            }
        }

        stage('Deploy to Prod') {
            when {
                expression { env.IS_MAIN_BRANCH == 'true' }
            }
            steps {
                echo "--- Building Production Release APK ---"
                sh './gradlew :app:assembleProdRelease'
            }
        }
    }

    post {
        always {
            // Collect and display test results in Jenkins UI
            junit '**/build/test-results/**/*.xml'
            // Save coverage reports
            archiveArtifacts artifacts: '**/build/reports/jacoco/**/*.html', allowEmptyArchive: true
        }
        success {
            // Save all final APKs as build artifacts
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
            echo "CI/CD Pipeline Succeeded for branch: ${env.CURRENT_BRANCH}"
        }
    }
}
