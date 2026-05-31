pipeline {
    agent any

    environment {
        // Path to your Android SDK
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        PATH = "${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Verify and Build') {
            steps {
                script {
                    // Branch logic: main runs dev & prod, others run dev
                    def flavors = (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') ? ['dev', 'prod'] : ['dev']

                    for (flavor in flavors) {
                        def capFlavor = flavor.capitalize()

                        echo "--- Processing Flavor: ${flavor} ---"

                        // 1. Run Lint
                        sh "./gradlew lint${capFlavor}Debug"

                        // 2. Run Unit Tests
                        sh "./gradlew test${capFlavor}DebugUnitTest"

                        // 3. Check Code Coverage (Fails if < 90% because of our build.gradle logic)
                        sh "./gradlew jacocoCoverageVerification"

                        // 4. Run UI Tests
                        // Note: This requires an emulator to be already running on your machine
                        sh "./gradlew connected${capFlavor}DebugAndroidTest"

                        // 5. Build Release APK
                        sh "./gradlew :app:assemble${capFlavor}Release"
                    }
                }
            }
        }
    }

    post {
        always {
            // Archive Unit Test results
            junit '**/build/test-results/**/*.xml'

            // Archive Coverage Reports
            archiveArtifacts artifacts: 'build/reports/jacoco/**/*.html', allowEmptyArchive: true
        }
        success {
            // Archive the generated APKs
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
            echo "CI/CD Pipeline Succeeded for ${env.BRANCH_NAME}"
        }
    }
}