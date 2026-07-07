// =========================================================================
// REUSABLE HELPER FUNCTIONS (Must stay outside the pipeline block)
// =========================================================================
def deployToFirebase(appId, flavor, buildType) {
    echo "--- Preparing Deployment: ${flavor} ${buildType} ---"
    
    // 1. Securely fetch signing credentials
    withCredentials([
        file(credentialsId: "${env.KEYSTORE_FILE_ID}", variable: 'KEYSTORE_FILE'),
        string(credentialsId: "${env.KEYSTORE_PWD_ID}", variable: 'KEYSTORE_PWD'),
        string(credentialsId: "${env.KEY_ALIAS_ID}", variable: 'KEY_ALIAS'),
        string(credentialsId: "${env.KEY_PWD_ID}", variable: 'KEY_PWD')
    ]) {
        // Compile signed APK
        sh """
            ./gradlew :app:assemble${flavor.capitalize()}${buildType.capitalize()} \\
            -PRELEASE_STORE_FILE=${KEYSTORE_FILE} \\
            -PRELEASE_STORE_PASSWORD=${KEYSTORE_PWD} \\
            -PRELEASE_KEY_ALIAS=${KEY_ALIAS} \\
            -PRELEASE_KEY_PASSWORD=${KEY_PWD} \\
            --no-daemon
        """
    }

    // 2. Locate the generated APK built in this workspace
    def searchPath = "${WORKSPACE}/app/build/outputs/apk/${flavor}/${buildType}"
    def jenkinsApkPath = sh(script: "find ${searchPath} -name '*.apk' ! -name '*unsigned*' | head -n 1", returnStdout: true).trim()
    
    if (!jenkinsApkPath) {
        jenkinsApkPath = sh(script: "find ${searchPath} -name '*.apk' | head -n 1", returnStdout: true).trim()
    }
    
    if (!jenkinsApkPath) {
        error("Deployment Failed: No APK found at ${searchPath}")
    }

    echo "Distributing APK: ${jenkinsApkPath}"

    // 3. Upload to Firebase using resolved binary
    sh """
        # Ensure 'node' can be found by adding the firebase bin directory to PATH
        if [ -n \"${env.FIREBASE_BIN_DIR}\" ]; then
            export PATH=\"${env.FIREBASE_BIN_DIR}:\$PATH\"
        fi
        
        firebase appdistribution:distribute \"${jenkinsApkPath}\" \\
        --app \"${appId}\" \\
        --groups \"${params.TESTER_GROUP}\" \\
        --release-notes \"${params.RELEASE_NOTES}\" \\
        --token \"\$FIREBASE_TOKEN\"
    """
}

pipeline {
    agent any

    // =========================================================================
    // BUILD PARAMETERS
    // =========================================================================
    parameters {
        choice(name: 'FLAVOR', choices: ['dev'], description: 'Select flavor to build.')
        choice(name: 'VARIANT', choices: ['Debug', 'Release'], description: 'Select Build Type.')
        gitParameter(name: 'BRANCH_TO_BUILD', type: 'PT_BRANCH', defaultValue: 'master', description: 'Select branch.')
        choice(name: 'TESTER_GROUP', choices: ['business', 'colaborator---tester'], description: 'Firebase Tester Group.')
        text(name: 'RELEASE_NOTES', defaultValue: 'Automated CI/CD build.', description: 'Notes.')
    }

    // =========================================================================
    // SERVER CONFIGURATION
    // =========================================================================
    environment {
        FIREBASE_TOKEN_CRED_ID  = 'FIREBASE_TOKEN'
        KEYSTORE_FILE_ID        = 'RELEASE_KEYSTORE_FILE'
        KEYSTORE_PWD_ID         = 'RELEASE_KEYSTORE_PASSWORD'
        KEY_ALIAS_ID            = 'RELEASE_KEY_ALIAS'
        KEY_PWD_ID              = 'RELEASE_KEY_PASSWORD'

        FIREBASE_APP_ID_DEV     = "1:626304171263:android:df1dea97585db187c920ca"
        FIREBASE_APP_ID_PROD    = "1:626304171263:android:5626dce56da60590c920ca"

        REPO_URL = 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git'
        COVERAGE_THRESHOLD = "90"
        
        FIREBASE_TOKEN = credentials("${env.FIREBASE_TOKEN_CRED_ID}")
    }

    stages {
        stage('Initialize & Sync') {
            steps {
                script {
                    try {
                        // A. Resolve OS and ANDROID_HOME
                        def osName = sh(script: 'uname', returnStdout: true).trim()
                        if (!env.ANDROID_HOME) {
                            env.ANDROID_HOME = (osName == 'Darwin') ? "${HOME}/Library/Android/sdk" : "/opt/android-sdk"
                        }
                        
                        // B. Locate Firebase CLI generically
                        echo "--- Locating Firebase CLI ---"
                        // This search covers Homebrew, /usr/local/bin, and deep NVM paths
                        def findFirebase = sh(script: """
                            which firebase || \
                            find /usr/local/bin /opt/homebrew/bin /Users /home /var/lib/jenkins -name firebase -perm +111 2>/dev/null | grep -E \"(/bin/firebase\$|/.nvm/versions/node/)\" | head -n 1
                        """, returnStdout: true).trim()
                        
                        if (!findFirebase) {
                            error("Firebase CLI not found. Please install via: npm install -g firebase-tools")
                        }
                        
                        env.FIREBASE_BIN_DIR = findFirebase.substring(0, findFirebase.lastIndexOf('/'))
                        
                        // C. Construct system PATH
                        env.PATH = "${env.FIREBASE_BIN_DIR}:${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
                        echo "Resolved Firebase PATH: ${env.FIREBASE_BIN_DIR}"

                        // D. Checkout
                        def rawBranch = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                        checkout([$class: 'GitSCM', branches: [[name: "${rawBranch}"]], userRemoteConfigs: [[url: "${env.REPO_URL}"]]])
                        
                        // E. Sync Flavors
                        def rawFlavors = sh(script: "grep -o 'create(\"[^\"]*\")' app/build.gradle.kts | cut -d'\"' -f2 | grep -vE 'release|debug|config|test' | sort -u", returnStdout: true).trim()
                        List<String> flavorList = rawFlavors.split('\n').collect { it.trim() }.findAll { !it.isEmpty() }
                        env.PROJECT_FLAVORS = flavorList.join(',')
                        
                        properties([
                            parameters([
                                choice(name: 'FLAVOR', choices: flavorList, description: 'Select flavor.'),
                                choice(name: 'VARIANT', choices: ['Debug', 'Release'], description: 'Select Build Type.'),
                                gitParameter(name: 'BRANCH_TO_BUILD', type: 'PT_BRANCH', defaultValue: 'master', description: 'Select branch.'),
                                choice(name: 'TESTER_GROUP', choices: ['business', 'colaborator---tester'], description: 'Firebase Group.'),
                                text(name: 'RELEASE_NOTES', defaultValue: params.RELEASE_NOTES, description: 'Notes.')
                            ])
                        ])

                        env.CURRENT_BRANCH = rawBranch.contains('/') ? rawBranch.split('/')[-1] : rawBranch
                        env.SELECTED_FLAVOR = params.FLAVOR ?: flavorList[0]
                        env.SELECTED_VARIANT = params.VARIANT ?: 'Debug'
                        env.BUILD_ALL = (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master')).toString()
                        
                        sh 'chmod +x gradlew'
                    } catch (Exception e) {
                        currentBuild.description = "Initialization Error: ${e.message}"
                        error("Pipeline setup failed.")
                    }
                }
            }
        }

        stage('Unit Test & Coverage') {
            steps {
                script {
                    try {
                        def testTasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "test${it.capitalize()}DebugUnitTest" }.join(' ') : 
                            "test${env.SELECTED_FLAVOR.capitalize()}DebugUnitTest"
                        
                        def thresholdDecimal = (env.COVERAGE_THRESHOLD.toInteger() / 100).toString()
                        sh "./gradlew ${testTasks} jacocoFullReport jacocoCoverageVerification -PcoverageThreshold=${thresholdDecimal} --no-daemon"
                    } catch (Exception ignored) {
                        currentBuild.description = "Quality Gate Failed"
                        error("Unit tests failed or coverage threshold not met.")
                    }
                }
            }
        }

        stage('FVT (UI Tests)') {
            steps {
                script {
                    try {
                        def flavorToTest = (env.SELECTED_FLAVOR == 'all') ? 'dev' : env.SELECTED_FLAVOR
                        sh "./gradlew connected${flavorToTest.capitalize()}DebugAndroidTest --no-daemon"
                    } catch (Exception ignored) {
                        error("Instrumented tests failed.")
                    }
                }
            }
        }

        stage('Build Artifacts') {
            steps {
                script {
                    try {
                        def tasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "assemble${it.capitalize()}${env.SELECTED_VARIANT}" }.join(' ') : 
                            "assemble${env.SELECTED_FLAVOR.capitalize()}${env.SELECTED_VARIANT}"
                        sh "./gradlew ${tasks} --no-daemon"
                    } catch (Exception ignored) {
                        error("Gradle build failed.")
                    }
                }
            }
        }

        stage('Deploy Staging') {
            when { expression { env.SELECTED_FLAVOR == 'dev' || env.BUILD_ALL == 'true' } }
            steps {
                script {
                    deployToFirebase(env.FIREBASE_APP_ID_DEV, 'dev', 'release')
                }
            }
        }

        stage('Deploy Production') {
            when { expression { env.SELECTED_FLAVOR == 'prod' || env.BUILD_ALL == 'true' } }
            steps {
                script {
                    deployToFirebase(env.FIREBASE_APP_ID_PROD, 'prod', 'release')
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
            script { currentBuild.description = "SUCCESS: ${env.CURRENT_BRANCH}" }
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
        }
    }
}
