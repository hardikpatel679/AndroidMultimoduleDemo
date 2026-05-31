pipeline {
    agent any

    parameters {
        choice(
            name: 'FLAVOR', 
            choices: ['dev', 'prod', 'mock', 'all'],
            description: 'Select the Android flavor to build. "all" will build both dev and prod.'
        )
        
        gitParameter(
            name: 'BRANCH_TO_BUILD', 
            type: 'PT_BRANCH', 
            defaultValue: 'master', 
            description: 'Select the branch to build',
            sortMode: 'ASCENDING_SMART',
            selectedValue: 'NONE'
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
                    // Resolve branch and flavor logic
                    env.CURRENT_BRANCH = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                    env.SELECTED_FLAVOR = params.FLAVOR ?: 'dev'
                    env.BUILD_ALL = (env.SELECTED_FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                    
                    echo "Branch: ${env.CURRENT_BRANCH} | Flavor: ${env.SELECTED_FLAVOR} | Build All: ${env.BUILD_ALL}"
                    
                    checkout([$class: 'GitSCM', 
                        branches: [[name: "${env.CURRENT_BRANCH}"]], 
                        userRemoteConfigs: [[url: 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git']]
                    ])
                    
                    sh 'chmod +x gradlew'
                }
            }
        }

        stage('Unit Test and Code Coverage') {
            steps {
                script {
                    echo "--- Running Unit Tests & Verifying Coverage First (Fail Fast) ---"
                    def testTasks = (env.BUILD_ALL == 'true') ? 'testDevDebugUnitTest testProdDebugUnitTest' : "test${env.SELECTED_FLAVOR.capitalize()}DebugUnitTest"
                    sh "./gradlew ${testTasks} jacocoCoverageVerification --no-daemon"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "--- Tests Passed! Now Compiling Application ---"
                    def tasks = (env.BUILD_ALL == 'true') ? 'assembleDevDebug assembleProdDebug' : "assemble${env.SELECTED_FLAVOR.capitalize()}Debug"
                    sh "./gradlew ${tasks} --no-daemon"
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                expression { env.SELECTED_FLAVOR == 'dev' || env.BUILD_ALL == 'true' }
            }
            steps {
                echo "--- Generating Staging APK ---"
                sh './gradlew :app:assembleDevRelease --no-daemon'
            }
        }

        stage('FVT') {
            steps {
                echo "--- Functional Verification Testing ---"
                sh './gradlew connectedDevDebugAndroidTest --no-daemon'
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
            when { expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' } }
            steps {
                echo "--- Generating Production APK ---"
                sh './gradlew :app:assembleProdRelease --no-daemon'
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
            echo "Pipeline Succeeded: ${env.CURRENT_BRANCH}"
        }
        failure {
            echo "Pipeline Failed: ${env.CURRENT_BRANCH}. Tests failed or Coverage threshold not met."
        }
    }
}
