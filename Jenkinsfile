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
    // We prioritize signed APKs (! -name '*unsigned*')
    def searchPath = "${WORKSPACE}/app/build/outputs/apk/${flavor}/${buildType}"
    def jenkinsApkPath = sh(script: "find ${searchPath} -name '*.apk' ! -name '*unsigned*' | head -n 1", returnStdout: true).trim()
    
    if (!jenkinsApkPath) {
        // Fallback to any APK if no specific signed one found
        jenkinsApkPath = sh(script: "find ${searchPath} -name '*.apk' | head -n 1", returnStdout: true).trim()
    }
    
    if (!jenkinsApkPath) {
        error("Deployment Failed: No APK found at ${searchPath}")
    }

    echo "Distributing APK: ${jenkinsApkPath}"

    // 3. Upload to Firebase using generic binary resolution
    sh """
        # Dynamically locate firebase across all common paths (Linux/macOS/NVM)
        # Add common binary locations to PATH for this shell execution
        export PATH="/usr/local/bin:/opt/homebrew/bin:/bin:/usr/bin:/usr/sbin:/sbin:\$PATH"
        
        # Check all possible NVM installations without hardcoding a version
        FIREBASE_BIN=\$(which firebase || find /Users /home /var/lib/jenkins -maxdepth 6 -name firebase -perm +111 2>/dev/null | grep -E ".nvm/versions/node/.+/bin/firebase" | head -n 1)
        
        if [ -z "\$FIREBASE_BIN" ]; then
            echo "ERROR: Firebase CLI not found on this server. Install via: npm install -g firebase-tools"
            exit 1
        fi

        # Prepend firebase's directory to PATH so it can find its 'node' dependency
        export PATH=\$(dirname \$FIREBASE_BIN):\$PATH
        
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
        // Initial defaults - These update automatically after the first build
        choice(name: 'FLAVOR', choices: ['dev', 'all'].join('\n'), description: 'Select flavor to build.')
        choice(name: 'VARIANT', choices: ['Debug', 'Release'].join('\n'), description: 'Select Build Type (Variant).')
        gitParameter(name: 'BRANCH_TO_BUILD', type: 'PT_BRANCH', defaultValue: 'master', description: 'Select branch to build.')
        choice(name: 'TESTER_GROUP', choices: ['business', 'colaborator---tester'].join('\n'), description: 'Firebase Tester Group.')
        text(name: 'RELEASE_NOTES', defaultValue: 'Automated CI/CD build.', description: 'Notes for testers.')
    }

    // =========================================================================
    // SERVER CONFIGURATION (Generic IDs for Shared Server)
    // =========================================================================
    environment {
        // 1. CREDENTIAL IDs (Configure these in Jenkins UI > Manage Jenkins > Credentials)
        FIREBASE_TOKEN_CRED_ID = 'FIREBASE_TOKEN' 
        KEYSTORE_FILE_ID       = 'RELEASE_KEYSTORE_FILE' 
        KEYSTORE_PWD_ID       = 'RELEASE_KEYSTORE_PASSWORD' 
        KEY_ALIAS_ID          = 'RELEASE_KEY_ALIAS' 
        KEY_PWD_ID            = 'RELEASE_KEY_PASSWORD' 

        // 2. FIREBASE APP IDs (Generic for this project)
        FIREBASE_APP_ID_DEV    = "1:626304171263:android:df1dea97585db187c920ca"
        FIREBASE_APP_ID_PROD   = "1:626304171263:android:5626dce56da60590c920ca"

        // 3. PROJECT SETTINGS
        REPO_URL = 'https://github.com/hardikpatel679/AndroidMultimoduleDemo.git'
        COVERAGE_THRESHOLD = "90" // Fail build if coverage < 90%
        
        // Securely resolve credentials
        FIREBASE_TOKEN = credentials("${env.FIREBASE_TOKEN_CRED_ID}")
    }

    stages {
        stage('Initialize & Sync') {
            steps {
                script {
                    try {
                        // A. Resolve ANDROID_HOME for macOS (Darwin) or Linux fallback
                        def osName = sh(script: 'uname', returnStdout: true).trim()
                        if (!env.ANDROID_HOME) {
                            env.ANDROID_HOME = (osName == 'Darwin') ? "${HOME}/Library/Android/sdk" : "/opt/android-sdk"
                        }
                        
                        // B. Add build tools to system path
                        env.PATH = "${env.ANDROID_HOME}/cmdline-tools/latest/bin:${env.ANDROID_HOME}/platform-tools:${env.PATH}"

                        // C. Checkout selected branch
                        def rawBranch = params.BRANCH_TO_BUILD ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: "master"
                        checkout([$class: 'GitSCM', branches: [[name: "${rawBranch}"]], userRemoteConfigs: [[url: "${env.REPO_URL}"]]])
                        
                        // D. DYNAMIC FLAVOR DETECTION
                        // Reads app/build.gradle.kts to automatically update Jenkins UI
                        def rawFlavors = sh(script: "grep -o 'create(\"[^\"]*\")' app/build.gradle.kts | cut -d'\"' -f2 | grep -vE 'release|debug|config|test' | sort -u", returnStdout: true).trim()
                        List<String> flavorList = rawFlavors.split('\n').collect { it.trim() }.findAll { !it.isEmpty() }
                        env.PROJECT_FLAVORS = flavorList.join(',')
                        
                        // Sync UI Parameters for the next build
                        properties([
                            parameters([
                                choice(name: 'FLAVOR', choices: (flavorList + ['all']).join('\n'), description: 'Select Android flavor.'),
                                choice(name: 'VARIANT', choices: ['Debug', 'Release'].join('\n'), description: 'Select Build Type.'),
                                gitParameter(name: 'BRANCH_TO_BUILD', type: 'PT_BRANCH', defaultValue: 'master', description: 'Select branch.'),
                                choice(name: 'TESTER_GROUP', choices: ['business', 'colaborator---tester'].join('\n'), description: 'Firebase Group.'),
                                text(name: 'RELEASE_NOTES', defaultValue: params.RELEASE_NOTES, description: 'Notes.')
                            ])
                        ])

                        env.CURRENT_BRANCH = rawBranch.contains('/') ? rawBranch.split('/')[-1] : rawBranch
                        env.SELECTED_FLAVOR = params.FLAVOR ?: flavorList[0]
                        env.SELECTED_VARIANT = params.VARIANT ?: 'Debug'
                        env.BUILD_ALL = (env.SELECTED_FLAVOR == 'all' || (params.FLAVOR == null && (env.CURRENT_BRANCH == 'master' || env.CURRENT_BRANCH == 'main'))).toString()
                        
                        echo "Environment Ready | OS: ${osName} | Project: ${env.CURRENT_BRANCH} | Flavors: ${env.PROJECT_FLAVORS}"
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
                        // Standardize: Tests always run on Debug for accurate coverage
                        def testVariant = 'Debug'
                        echo "--- Verifying ${env.COVERAGE_THRESHOLD}% Coverage ---"
                        
                        def testTasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "test${it.capitalize()}${testVariant}UnitTest" }.join(' ') : 
                            "test${env.SELECTED_FLAVOR.capitalize()}${testVariant}UnitTest"
                        
                        def thresholdDecimal = (env.COVERAGE_THRESHOLD.toInteger() / 100).toString()
                        sh "./gradlew ${testTasks} jacocoFullReport jacocoCoverageVerification -PcoverageThreshold=${thresholdDecimal} --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Quality Gate Failed: Coverage below ${env.COVERAGE_THRESHOLD}%"
                        error("Unit tests failed or coverage threshold not met.")
                    }
                }
            }
        }

        stage('FVT (UI Tests)') {
            steps {
                script {
                    try {
                        def testVariant = 'Debug'
                        def flavorToTest = (env.SELECTED_FLAVOR == 'all') ? 'dev' : env.SELECTED_FLAVOR
                        sh "./gradlew connected${flavorToTest.capitalize()}${testVariant}AndroidTest --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "FVT Error: UI Tests Failed."
                        error("Instrumented tests failed.")
                    }
                }
            }
        }

        stage('Gate') {
            when { expression { env.BUILD_ALL == 'true' || env.SELECTED_FLAVOR == 'prod' } }
            steps { echo "--- Production Readiness Gate Passed ---" }
        }

        stage('Build Artifacts') {
            steps {
                script {
                    try {
                        def tasks = (env.BUILD_ALL == 'true') ? 
                            env.PROJECT_FLAVORS.split(',').collect { "assemble${it.capitalize()}${env.SELECTED_VARIANT}" }.join(' ') : 
                            "assemble${env.SELECTED_FLAVOR.capitalize()}${env.SELECTED_VARIANT}"
                        sh "./gradlew ${tasks} --no-daemon"
                    } catch (Exception e) {
                        currentBuild.description = "Build Error: Compilation failed."
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
            script { currentBuild.description = "SUCCESS: Build complete for ${env.CURRENT_BRANCH}" }
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true
        }
    }
}
