pipeline {
    agent any

    parameters {
        // Dropdown for Flavors
        choice(
            name: 'FLAVOR', 
            choices: ['dev', 'prod', 'uat', 'mock', 'all'], 
            description: 'Select the Android flavor to build. "all" will build both dev and prod.'
        )
        
        // Dynamic Branch List (Requires "Git Parameter" plugin)
        // If you don't have the plugin, this will fall back to a manual string input
        string(
            name: 'BRANCH_TO_BUILD', 
            defaultValue: 'master', 
            description: 'Enter the branch name to build (e.g., master, develop, feature/login)'
        )
    }

    environment {
        // Path to your Android SDK
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        PATH = "${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    // Use the parameter if provided, otherwise detect automatically
                    env.CURRENT_BRANCH = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "unknown"
                    
                    // Logic for "all" or specific flavors
                    env.BUILD_ALL = (params.FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                    env.SELECTED_FLAVOR = params.FLAVOR ?: 'dev'
                    
                    echo "Branch: ${env.CURRENT_BRANCH}"
                    echo "Target Flavor: ${env.SELECTED_FLAVOR}"
                    echo "Build All Flavors: ${env.BUILD_ALL}"
                }
            }
        }

        stage('Build') {
            steps {
                echo "--- Compiling Application ---"
                sh 'chmod +x gradlew'
                script {
                    if (env.BUILD_ALL == 'true') {
                        sh './gradlew assembleDevDebug assembleProdDebug'
                    } else {
                        def capFlavor = env.SELECTED_FLAVOR.capitalize()
                        sh "./gradlew assemble${capFlavor}Debug"
                    }
                }
            }
        }

        stage('Unit Test and Code Coverage') {
            steps {
                echo "--- Running Unit Tests & Verifying Coverage ---"
                script {
                    if (env.BUILD_ALL == 'true') {
                        sh './gradlew testDevDebugUnitTest testProdDebugUnitTest'
                    } else {
                        def capFlavor = env.SELECTED_FLAVOR.capitalize()
                        sh "./gradlew test${capFlavor}DebugUnitTest"
                    }
                    sh './gradlew jacocoCoverageVerification'
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                expression { env.SELECTED_FLAVOR == 'dev' || env.BUILD_ALL == 'true' }
            }
            steps {
                echo "--- Building Staging (Dev) APK ---"
                sh './gradlew :app:assembleDevRelease'
            }
        }

        stage('FVT') {
            steps {
                echo "--- Functional Verification Testing (UI Tests) ---"
                sh './gradlew connectedDevDebugAndroidTest'
            }
        }

        stage('Gate') {
            when {
                expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' }
            }
            steps {
                echo "--- Quality Gate Passed ---"
            }
        }

        stage('Deploy to Prod') {
            when {
                expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' }
            }
            steps {
                echo "--- Building Production Release APK ---"
                sh './gradlew :app:assembleProdRelease'
            }
        }
    }

    post {
        always {
            junit '**/build/test-results/**/*.xml'
            archiveArtifacts artifacts: '**/build/reports/jacoco/**/*.html', allowEmptyArchive: true
        }
        success {
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
            echo "CI/CD Pipeline Succeeded for branch: ${env.CURRENT_BRANCH}"
        }
    }
}
