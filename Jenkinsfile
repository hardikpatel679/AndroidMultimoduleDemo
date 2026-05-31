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

        choice(
            name: 'TESTER_GROUP', 
            choices: ['business', 'colaborator---tester'],
            description: 'Select the Firebase Tester Group Alias to receive the Staging build.'
        )
    }

    environment {
        // PRE-REQUISITE: Add a "Secret text" credential in Jenkins with ID 'FIREBASE_TOKEN'
        FIREBASE_TOKEN = credentials('FIREBASE_TOKEN')
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        
        // Define absolute path to firebase binary to avoid PATH issues
        FIREBASE_BIN = "/Users/hardikp/.nvm/versions/node/v24.15.0/bin/firebase"
        
        // Expanded PATH to include common locations for Homebrew, Node, and NVM
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:/Users/hardikp/.nvm/versions/node/v24.15.0/bin:${env.PATH}"
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

                        echo "--- Verifying Firebase Connectivity ---"
                        sh "${env.FIREBASE_BIN} --version"
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
                        echo "--- Generating Signed Staging APK ---"
                        
                        // PRE-REQUISITE: Add these credentials in Jenkins
                        withCredentials([
                            file(credentialsId: 'RELEASE_KEYSTORE_FILE', variable: 'KEYSTORE_FILE'),
                            string(credentialsId: 'RELEASE_KEYSTORE_PASSWORD', variable: 'KEYSTORE_PWD'),
                            string(credentialsId: 'RELEASE_KEY_ALIAS', variable: 'KEY_ALIAS'),
                            string(credentialsId: 'RELEASE_KEY_PASSWORD', variable: 'KEY_PWD')
                        ]) {
                            sh """
                                ./gradlew :app:assembleDevRelease \
                                -PRELEASE_STORE_FILE=${KEYSTORE_FILE} \
                                -PRELEASE_STORE_PASSWORD=${KEYSTORE_PWD} \
                                -PRELEASE_KEY_ALIAS=${KEY_ALIAS} \
                                -PRELEASE_KEY_PASSWORD=${KEY_PWD} \
                                --no-daemon
                            """
                        }

                        echo "--- Uploading to Firebase App Distribution ---"
                        // Find the APK file dynamically
                        def jenkinsApkBuildPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/dev/release -name '*.apk' ! -name '*unsigned*' | head -n 1", returnStdout: true).trim()
                        
                        if (!jenkinsApkBuildPath) {
                            // Fallback to any apk if signed one not found with specific pattern
                            jenkinsApkBuildPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/dev/release -name '*.apk' | head -n 1", returnStdout: true).trim()
                        }
                        
                        if (!jenkinsApkBuildPath) {
                            error("APK file not found in ${WORKSPACE}/app/build/outputs/apk/dev/release")
                        }
                        
                        echo "Distributing APK: ${jenkinsApkBuildPath}"
                        
                        sh """
                            ${env.FIREBASE_BIN} appdistribution:distribute "${jenkinsApkBuildPath}" \
                            --app "1:626304171263:android:df1dea97585db187c920ca" \
                            --groups "${params.TESTER_GROUP}" \
                            --release-notes "Build from branch: ${env.CURRENT_BRANCH}" \
                            --token "\$FIREBASE_TOKEN"
                        """
                        
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Deploy to Staging: ${e.message}"
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
                        echo "--- Generating Signed Production APK ---"
                        
                        withCredentials([
                            file(credentialsId: 'RELEASE_KEYSTORE_FILE', variable: 'KEYSTORE_FILE'),
                            string(credentialsId: 'RELEASE_KEYSTORE_PASSWORD', variable: 'KEYSTORE_PWD'),
                            string(credentialsId: 'RELEASE_KEY_ALIAS', variable: 'KEY_ALIAS'),
                            string(credentialsId: 'RELEASE_KEY_PASSWORD', variable: 'KEY_PWD')
                        ]) {
                            sh """
                                ./gradlew :app:assembleProdRelease \
                                -PRELEASE_STORE_FILE=${KEYSTORE_FILE} \
                                -PRELEASE_STORE_PASSWORD=${KEYSTORE_PWD} \
                                -PRELEASE_KEY_ALIAS=${KEY_ALIAS} \
                                -PRELEASE_KEY_PASSWORD=${KEY_PWD} \
                                --no-daemon
                            """
                        }
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Deploy to Prod: ${e.message}"
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
