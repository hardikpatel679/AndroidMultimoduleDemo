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
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        PATH = "${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    try {
                        env.CURRENT_BRANCH = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                        env.SELECTED_FLAVOR = params.FLAVOR ?: 'dev'
                        env.BUILD_ALL = (env.SELECTED_FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                        
                        echo "Branch: ${env.CURRENT_BRANCH} | Flavor: ${env.SELECTED_FLAVOR} | Build All: ${env.BUILD_ALL}"
                        
                        checkout([$class: 'GitSCM', 
                            branches: [[name: "${env.CURRENT_BRANCH}"]], 
                            userRemoteConfigs: [[url: 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git']]
                        ])
                        
                        sh 'chmod +x gradlew'
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Initialize: ${e.message}"
                        error("Initialization failed: ${e.message}")
                    }
                }
            }
        }

        stage('Unit Test and Code Coverage') {
            steps {
                script {
                    try {
                        echo "--- Running Unit Tests & Verifying 90% Coverage ---"
                        def testTasks = (env.BUILD_ALL == 'true') ? 'testDevDebugUnitTest testProdDebugUnitTest' : "test${env.SELECTED_FLAVOR.capitalize()}DebugUnitTest"
                        sh "./gradlew ${testTasks} jacocoCoverageVerification --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Unit Tests/Coverage: Check if threshold 90% is met or tests are passing."
                        error("Unit Test or Coverage verification failed. Ensure coverage is >= 90%.")
                    }
                }
            }
        }

        stage('FVT') {
            steps {
                script {
                    try {
                        echo "--- Functional Verification Testing ---"
                        sh './gradlew connectedDevDebugAndroidTest --no-daemon'
                    } catch (Exception e) {
                        currentBuild.description = "Failed at FVT: UI Tests failed or Emulator not responsive."
                        error("UI Testing (FVT) failed.")
                    }
                }
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

        stage('Build') {
            steps {
                script {
                    try {
                        echo "--- Compiling Application ---"
                        def tasks = (env.BUILD_ALL == 'true') ? 'assembleDevDebug assembleProdDebug' : "assemble${env.SELECTED_FLAVOR.capitalize()}Debug"
                        sh "./gradlew ${tasks} --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Build: Compilation error in Android code."
                        error("Build stage failed: ${e.message}")
                    }
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                expression { env.SELECTED_FLAVOR == 'dev' || env.BUILD_ALL == 'true' }
            }
            steps {
                script {
                    try {
                        echo "--- Generating Staging APK ---"
                        sh './gradlew :app:assembleDevRelease --no-daemon'
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Deploy to Staging: Could not generate Dev Release APK."
                        error("Staging deployment failed: ${e.message}")
                    }
                }
            }
        }

        stage('Deploy to Prod') {
            when { expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' } }
            steps {
                script {
                    try {
                        echo "--- Generating Production APK ---"
                        sh './gradlew :app:assembleProdRelease --no-daemon'
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Deploy to Prod: Could not generate Prod Release APK."
                        error("Production deployment failed: ${e.message}")
                    }
                }
            }
        }
    }

    post {
        always {
            junit '**/build/test-results/**/*.xml'
            archiveArtifacts artifacts: '**/build/reports/jacoco/**/*.html', allowEmptyArchive: true
        }
        success {
            script {
                currentBuild.description = "Successfully built ${env.CURRENT_BRANCH}"
            }
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
        }
    }
}
