pipeline {
    agent any

    parameters {
        choice(
            name: 'FLAVOR', 
            choices: ['dev', 'prod', 'uat', 'mock', 'all'],
            description: 'Select the Android flavor to build. "all" will build both dev and prod.'
        )
        
        choice(
            name: 'VARIANT', 
            choices: ['Debug', 'Release'],
            description: 'Select the Build Type (Variant).'
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

        text(
            name: 'RELEASE_NOTES', 
            defaultValue: 'New features and bug fixes.', 
            description: 'Enter the release notes for Firebase App Distribution.'
        )
    }

    environment {
        // PRE-REQUISITE: Add a "Secret text" credential in Jenkins with ID 'FIREBASE_TOKEN'
        FIREBASE_TOKEN = credentials('FIREBASE_TOKEN')
        ANDROID_HOME = "/Users/hardikp/Library/Android/sdk"
        
        // Code Coverage Threshold (%)
        COVERAGE_THRESHOLD = "90"
        
        // Expanded PATH to include common locations for Homebrew, Node, and System Binaries
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    try {
                        // 1. Resolve basic branch info
                        def rawBranch = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                        env.CURRENT_BRANCH = rawBranch.contains('/') ? rawBranch.split('/')[-1] : rawBranch
                        
                        // 2. Extract Flavors from the project itself
                        // Improved logic: grep the whole file but exclude common false positives like 'release' or 'debug' signing configs
                        env.PROJECT_FLAVORS = sh(script: "grep -o 'create(\"[^\"]*\")' app/build.gradle.kts | cut -d'\"' -f2 | grep -vE 'release|debug|config' | sort -u | tr '\\n' ',' | sed 's/,\$//'", returnStdout: true).trim()
                        
                        echo "Detected Project Flavors: ${env.PROJECT_FLAVORS}"
                        
                        // 3. Resolve selected flavor and variant
                        env.SELECTED_FLAVOR = params.FLAVOR ?: 'dev'
                        env.SELECTED_VARIANT = params.VARIANT ?: 'Debug'
                        env.BUILD_ALL = (env.SELECTED_FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                        
                        echo "Branch: ${env.CURRENT_BRANCH} | Flavor: ${env.SELECTED_FLAVOR} | Variant: ${env.SELECTED_VARIANT} | Build All: ${env.BUILD_ALL}"
                        
                        // 4. Validate selected flavor against detected flavors
                        def flavorList = env.PROJECT_FLAVORS.split(',')
                        if (env.SELECTED_FLAVOR != 'all' && !flavorList.contains(env.SELECTED_FLAVOR)) {
                            echo "Warning: Selected flavor '${env.SELECTED_FLAVOR}' not found in project. Defaulting to 'dev'."
                            env.SELECTED_FLAVOR = 'dev'
                        }

                        checkout([$class: 'GitSCM', 
                            branches: [[name: "${rawBranch}"]], 
                            userRemoteConfigs: [[url: 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git']]
                        ])
                        
                        sh 'chmod +x gradlew'

                        echo "--- Verifying Firebase Connectivity ---"
                        // Look for firebase in standard paths, and specifically check your NVM folder to avoid a slow full-disk search
                        def findFirebase = sh(script: """
                            which firebase || \
                            find /Users/hardikp/.nvm -name firebase -type f -perm +111 2>/dev/null | head -n 1 || \
                            find /usr/local/bin -name firebase -type f -perm +111 2>/dev/null | head -n 1
                        """, returnStdout: true).trim()
                        
                        if (!findFirebase) {
                            error("Firebase CLI not found. Please ensure it is installed and available to Jenkins.")
                        }
                        env.FIREBASE_EXE = findFirebase
                        sh "${env.FIREBASE_EXE} --version"
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
                        def variant = env.SELECTED_VARIANT ?: 'Debug'
                        echo "--- Running Unit Tests & Verifying ${env.COVERAGE_THRESHOLD}% Coverage for ${variant} ---"
                        
                        def testTasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "test${it.capitalize()}${variant}UnitTest" }.join(' ') : 
                            "test${env.SELECTED_FLAVOR.capitalize()}${variant}UnitTest"
                        
                        // Pass the threshold to Gradle as well to ensure synchronization
                        def thresholdDecimal = (env.COVERAGE_THRESHOLD.toInteger() / 100).toString()
                        sh "./gradlew ${testTasks} jacocoCoverageVerification -PcoverageThreshold=${thresholdDecimal} --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Unit Tests/Coverage: Check if threshold ${env.COVERAGE_THRESHOLD}% is met or tests are passing."
                        error("Unit Test or Coverage verification failed. Ensure coverage is >= ${env.COVERAGE_THRESHOLD}%.")
                    }
                }
            }
        }

        stage('FVT') {
            steps {
                script {
                    try {
                        def variant = env.SELECTED_VARIANT ?: 'Debug'
                        echo "--- Functional Verification Testing (${variant}) ---"
                        
                        // For UI tests, we usually just test the dev flavor to save time, 
                        // or we test the selected flavor.
                        def flavorToTest = (env.SELECTED_FLAVOR == 'all') ? 'dev' : env.SELECTED_FLAVOR
                        sh "./gradlew connected${flavorToTest.capitalize()}${variant}AndroidTest --no-daemon"
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
                        def variant = env.SELECTED_VARIANT ?: 'Debug'
                        echo "--- Compiling Application (${variant}) ---"
                        
                        def tasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "assemble${it.capitalize()}${variant}" }.join(' ') : 
                            "assemble${env.SELECTED_FLAVOR.capitalize()}${variant}"

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
                            ${env.FIREBASE_EXE} appdistribution:distribute "${jenkinsApkBuildPath}" \
                            --app "1:626304171263:android:df1dea97585db187c920ca" \
                            --groups "${params.TESTER_GROUP}" \
                            --release-notes "${params.RELEASE_NOTES}" \
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
