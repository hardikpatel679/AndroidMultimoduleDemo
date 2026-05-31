pipeline {
    agent any

    environment {
        // Path to your Android SDK
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        PATH = "${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
        // Detection for Main branch
        IS_MAIN_BRANCH = "${env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'master'}"
    }

    stages {
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
                echo "--- Running Unit Tests & Verifying 80% Coverage ---"
                script {
                    if (env.IS_MAIN_BRANCH == 'true') {
                        // Test both flavors on main
                        sh './gradlew testDevDebugUnitTest testProdDebugUnitTest'
                    } else {
                        sh './gradlew testDevDebugUnitTest'
                    }
                    // Run consolidated coverage verification (threshold 80% as configured in build.gradle.kts)
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
                echo "--- Quality Gate Passed: Branch is ${env.BRANCH_NAME} ---"
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
            echo "CI/CD Pipeline Succeeded for ${env.BRANCH_NAME}"
        }
    }
}
