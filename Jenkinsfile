// =========================================================================
// REUSABLE HELPER FUNCTIONS (Must be outside the pipeline block)
// =========================================================================
def deployToFirebase(appId, flavor, buildType) {
    echo "--- Deploying ${flavor} ${buildType} to Firebase ---"
    
    withCredentials([
        file(credentialsId: "${env.KEYSTORE_FILE_ID}", variable: 'KEYSTORE_FILE'),
        string(credentialsId: "${env.KEYSTORE_PWD_ID}", variable: 'KEYSTORE_PWD'),
        string(credentialsId: "${env.KEY_ALIAS_ID}", variable: 'KEY_ALIAS'),
        string(credentialsId: "${env.KEY_PWD_ID}", variable: 'KEY_PWD')
    ]) {
        sh """
            ./gradlew :app:assemble${flavor.capitalize()}${buildType.capitalize()} \\
            -PRELEASE_STORE_FILE=${KEYSTORE_FILE} \\
            -PRELEASE_STORE_PASSWORD=${KEYSTORE_PWD} \\
            -PRELEASE_KEY_ALIAS=${KEY_ALIAS} \\
            -PRELEASE_KEY_PASSWORD=${KEY_PWD} \\
            --no-daemon
        """
    }

    def apkPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/${flavor}/${buildType} -name '*.apk' ! -name '*unsigned*' | head -n 1", returnStdout: true).trim()
    if (!apkPath) { apkPath = sh(script: "find ${WORKSPACE}/app/build/outputs/apk/${flavor}/${buildType} -name '*.apk' | head -n 1", returnStdout: true).trim() }
    
    sh """
        # Relocate firebase dynamically
        FIREBASE_BIN=\$(which firebase || find /usr/local/bin /opt/homebrew/bin /Users/*/.nvm/versions/node/*/bin /var/lib/jenkins/.nvm/versions/node/*/bin -name firebase -perm +111 2>/dev/null | head -n 1)
        if [ -z "\$FIREBASE_BIN" ]; then echo "Firebase CLI not found"; exit 1; fi
        export PATH=\$(dirname \$FIREBASE_BIN):\$PATH
        
        firebase appdistribution:distribute \"${apkPath}\" \\
        --app \"${appId}\" \\
        --groups \"${params.TESTER_GROUP}\" \\
        --release-notes \"${params.RELEASE_NOTES}\" \\
        --token \"\$FIREBASE_TOKEN\"
    """
}

pipeline {
    agent any

    parameters {
        choice(name: 'FLAVOR', choices: ['dev', 'prod', 'uat', 'mock', 'all'], description: 'Select flavor to build.')
        choice(name: 'VARIANT', choices: ['Debug', 'Release'], description: 'Select Build Type.')
        gitParameter(name: 'BRANCH_TO_BUILD', type: 'PT_BRANCH', defaultValue: 'master', description: 'Select branch.')
        choice(name: 'TESTER_GROUP', choices: ['business', 'colaborator---tester'], description: 'Firebase Tester Group.')
        text(name: 'RELEASE_NOTES', defaultValue: 'New features and bug fixes.', description: 'Release notes.')
    }

    environment {
        // Credential IDs (Manage Jenkins > Credentials)
        FIREBASE_TOKEN_CRED_ID = 'FIREBASE_TOKEN'
        KEYSTORE_FILE_ID       = 'RELEASE_KEYSTORE_FILE'
        KEYSTORE_PWD_ID       = 'RELEASE_KEYSTORE_PASSWORD'
        KEY_ALIAS_ID          = 'RELEASE_KEY_ALIAS'
        KEY_PWD_ID            = 'RELEASE_KEY_PASSWORD'

        // Firebase App IDs
        FIREBASE_APP_ID_DEV    = "1:626304171263:android:df1dea97585db187c920ca"
        FIREBASE_APP_ID_PROD   = "1:626304171263:android:5626dce56da60590c920ca"

        REPO_URL = 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git'
        COVERAGE_THRESHOLD = "90"
        
        // Resolve credentials securely
        FIREBASE_TOKEN = credentials("${env.FIREBASE_TOKEN_CRED_ID}")
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    try {
                        // Resolve ANDROID_HOME generically
                        def os = sh(script: 'uname', returnStdout: true).trim()
                        if (env.ANDROID_HOME == null || env.ANDROID_HOME == "") {
                            env.ANDROID_HOME = (os == 'Darwin') ? "${HOME}/Library/Android/sdk" : "/opt/android-sdk"
                        }
                        
                        // Resolve branch
                        def rawBranch = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                        env.CURRENT_BRANCH = rawBranch.contains('/') ? rawBranch.split('/')[-1] : rawBranch
                        
                        // Extract flavors
                        env.PROJECT_FLAVORS = sh(script: "grep -o 'create(\"[^\"]*\")' app/build.gradle.kts | cut -d'\"' -f2 | grep -vE 'release|debug|config' | sort -u | tr '\\n' ',' | sed 's/,\$//'", returnStdout: true).trim()
                        
                        env.SELECTED_FLAVOR = params.FLAVOR ?: 'dev'
                        env.SELECTED_VARIANT = params.VARIANT ?: 'Debug'
                        env.BUILD_ALL = (env.SELECTED_FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                        
                        echo "--- Environment Info ---"
                        echo "OS: ${os} | Android Home: ${env.ANDROID_HOME}"
                        echo "Branch: ${env.CURRENT_BRANCH} | Flavors: ${env.PROJECT_FLAVORS}"

                        checkout([$class: 'GitSCM', branches: [[name: "${rawBranch}"]], userRemoteConfigs: [[url: "${env.REPO_URL}"]]])
                        sh 'chmod +x gradlew'
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Initialize: ${e.message}"
                        error("Initialization failed: ${e.message}")
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
                    } catch (Exception e) {
                        currentBuild.description = "Failed at Unit Tests/Coverage."
                        error("Unit Test or Coverage verification failed.")
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
                    } catch (Exception e) {
                        currentBuild.description = "Failed at FVT."
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
                        currentBuild.description = "Failed at Build."
                        error("Build failed: ${e.message}")
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
            script { currentBuild.description = "Successfully built ${env.CURRENT_BRANCH}" }
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
        }
    }
}
