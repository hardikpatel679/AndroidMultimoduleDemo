pipeline {
    agent any

    // =========================================================================
    // BUILD PARAMETERS
    // =========================================================================
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

    // =========================================================================
    // CONFIGURABLE ENVIRONMENT VARIABLES (Edit these for new environments)
    // =========================================================================
    environment {
        // 1. CREDENTIALS IDS: Must match IDs in Jenkins > Credentials
        FIREBASE_TOKEN_CRED_ID = 'FIREBASE_TOKEN' // Secret Text ID
        KEYSTORE_FILE_ID       = 'RELEASE_KEYSTORE_FILE' // Secret File ID
        KEYSTORE_PWD_ID       = 'RELEASE_KEYSTORE_PASSWORD' // Secret Text ID
        KEY_ALIAS_ID          = 'RELEASE_KEY_ALIAS' // Secret Text ID
        KEY_PWD_ID            = 'RELEASE_KEY_PASSWORD' // Secret Text ID

        // 2. FIREBASE APP IDS: Found in Firebase Console > Project Settings
        FIREBASE_APP_ID_DEV    = "1:626304171263:android:df1dea97585db187c920ca"
        FIREBASE_APP_ID_PROD   = "1:626304171263:android:5626dce56da60590c920ca"

        // 3. PROJECT INFO
        REPO_URL = 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git'
        
        // 4. QUALITY GATES
        COVERAGE_THRESHOLD = "90" // Percentage (%)

        // 5. SYSTEM PATHS: Using ${HOME} makes it generic for any user on the machine
        ANDROID_HOME = "${HOME}/Library/Android/sdk"
        
        // Resolve credentials securely
        FIREBASE_TOKEN = credentials("${env.FIREBASE_TOKEN_CRED_ID}")
        
        // Final System PATH construction
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    try {
                        // Resolve branch info
                        def rawBranch = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                        env.CURRENT_BRANCH = rawBranch.contains('/') ? rawBranch.split('/')[-1] : rawBranch
                        
                        // Extract flavors dynamically from app/build.gradle.kts
                        env.PROJECT_FLAVORS = sh(script: "grep -o 'create(\"[^\"]*\")' app/build.gradle.kts | cut -d'\"' -f2 | grep -vE 'release|debug|config' | sort -u | tr '\\n' ',' | sed 's/,\$//'", returnStdout: true).trim()
                        
                        // Set build logic variables
                        env.SELECTED_FLAVOR = params.FLAVOR ?: 'dev'
                        env.SELECTED_VARIANT = params.VARIANT ?: 'Debug'
                        env.BUILD_ALL = (env.SELECTED_FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                        
                        echo "Project Flavors: ${env.PROJECT_FLAVORS} | Branch: ${env.CURRENT_BRANCH}"

                        checkout([$class: 'GitSCM', 
                            branches: [[name: "${rawBranch}"]], 
                            userRemoteConfigs: [[url: "${env.REPO_URL}"]]
                        ])
                        
                        sh 'chmod +x gradlew'

                        echo "--- Verifying Tool Connectivity ---"
                        sh """
                            # Dynamically locate firebase even in NVM environments
                            FIREBASE_BIN=\$(which firebase || find /usr/local/bin /opt/homebrew/bin /Users/*/.nvm/versions/node/*/bin -name firebase -perm +111 2>/dev/null | head -n 1)
                            if [ -z "\$FIREBASE_BIN" ]; then echo "Firebase CLI not found"; exit 1; fi
                            export PATH=\$(dirname \$FIREBASE_BIN):\$PATH
                            firebase --version
                        """
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
                        def testVariant = 'Debug' // Standard for tests
                        echo "--- Verifying ${env.COVERAGE_THRESHOLD}% Coverage for ${testVariant} ---"
                        
                        def testTasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "test${it.capitalize()}${testVariant}UnitTest" }.join(' ') : 
                            "test${env.SELECTED_FLAVOR.capitalize()}${testVariant}UnitTest"
                        
                        def thresholdDecimal = (env.COVERAGE_THRESHOLD.toInteger() / 100).toString()
                        sh "./gradlew ${testTasks} jacocoFullReport jacocoCoverageVerification -PcoverageThreshold=${thresholdDecimal} --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Unit Tests/Coverage: threshold ${env.COVERAGE_THRESHOLD}% not met."
                        error("Unit Test or Coverage verification failed.")
                    }
                }
            }
        }

        stage('FVT') {
            steps {
                script {
                    try {
                        def testVariant = 'Debug'
                        def flavorToTest = (env.SELECTED_FLAVOR == 'all') ? 'dev' : env.SELECTED_FLAVOR
                        sh "./gradlew connected${flavorToTest.capitalize()}${testVariant}AndroidTest --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Failed at FVT: UI Tests failed."
                        error("UI Testing (FVT) failed.")
                    }
                }
            }
        }

        stage('Gate') {
            when { expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' } }
            steps { echo "--- Quality Gate Passed ---" }
        }

        stage('Build') {
            steps {
                script {
                    try {
                        def variant = env.SELECTED_VARIANT ?: 'Debug'
                        def tasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "assemble${it.capitalize()}${variant}" }.join(' ') : 
                            "assemble${env.SELECTED_FLAVOR.capitalize()}${variant}"
                        sh "./gradlew ${tasks} --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Build: Compilation error."
                        error("Build failed: ${e.message}")
                    }
                }
            }
        }

        stage('Deploy to Staging') {
            when { expression { env.SELECTED_FLAVOR == 'dev' || env.BUILD_ALL == 'true' } }
            steps {
                script {
                    try {
                        withCredentials([
                            file(credentialsId: "${env.KEYSTORE_FILE_ID}", variable: 'KEYSTORE_FILE'),
                            string(credentialsId: "${env.KEYSTORE_PWD_ID}", variable: 'KEYSTORE_PWD'),
                            string(credentialsId: "${env.KEY_ALIAS_ID}", variable: 'KEY_ALIAS'),
                            string(credentialsId: "${env.KEY_PWD_ID}", variable: 'KEY_PWD')
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

                        def apkPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/dev/release -name '*.apk' ! -name '*unsigned*' | head -n 1", returnStdout: true).trim()
                        if (!apkPath) { apkPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/dev/release -name '*.apk' | head -n 1", returnStdout: true).trim() }
                        
                        sh """
                            FIREBASE_BIN=\$(which firebase || find /usr/local/bin /opt/homebrew/bin /Users/*/.nvm/versions/node/*/bin -name firebase -perm +111 2>/dev/null | head -n 1)
                            export PATH=\$(dirname \$FIREBASE_BIN):\$PATH
                            firebase appdistribution:distribute "${apkPath}" \
                            --app "${env.FIREBASE_APP_ID_DEV}" \
                            --groups "${params.TESTER_GROUP}" \
                            --release-notes "${params.RELEASE_NOTES}" \
                            --token "\$FIREBASE_TOKEN"
                        """
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Deploy to Staging: ${e.message}"
                        error("Staging deployment failed.")
                    }
                }
            }
        }

        stage('Deploy to Prod') {
            when { expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' } }
            steps {
                script {
                    try {
                        withCredentials([
                            file(credentialsId: "${env.KEYSTORE_FILE_ID}", variable: 'KEYSTORE_FILE'),
                            string(credentialsId: "${env.KEYSTORE_PWD_ID}", variable: 'KEYSTORE_PWD'),
                            string(credentialsId: "${env.KEY_ALIAS_ID}", variable: 'KEY_ALIAS'),
                            string(credentialsId: "${env.KEY_PWD_ID}", variable: 'KEY_PWD')
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

                        def apkPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/prod/release -name '*.apk' ! -name '*unsigned*' | head -n 1", returnStdout: true).trim()
                        if (!apkPath) { apkPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/prod/release -name '*.apk' | head -n 1", returnStdout: true).trim() }

                        sh """
                            FIREBASE_BIN=\$(which firebase || find /usr/local/bin /opt/homebrew/bin /Users/*/.nvm/versions/node/*/bin -name firebase -perm +111 2>/dev/null | head -n 1)
                            export PATH=\$(dirname \$FIREBASE_BIN):\$PATH
                            firebase appdistribution:distribute "${apkPath}" \
                            --app "${env.FIREBASE_APP_ID_PROD}" \
                            --groups "${params.TESTER_GROUP}" \
                            --release-notes "${params.RELEASE_NOTES}" \
                            --token "\$FIREBASE_TOKEN"
                        """
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Deploy to Prod: ${e.message}"
                        error("Production deployment failed.")
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
            script { currentBuild.description = "Successfully built ${env.CURRENT_BRANCH}" }
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
        }
    }
}
