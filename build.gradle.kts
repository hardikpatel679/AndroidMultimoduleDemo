// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinjvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    jacoco
}

val jacocoVersion = libs.versions.jacoco.get()

subprojects {
    apply(plugin = "jacoco")

    configure<JacocoPluginExtension> {
        toolVersion = jacocoVersion
    }

    tasks.withType<Test> {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

val fileFilter = mutableListOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "**/androidx/**/*.*",
    "**/*Fragment*.*",
    "**/*Activity*.*",
    "**/*Adapter*.*",
    "**/*ViewPager*.*",
    "**/*ViewHolder*.*",
    "**/*Module*.*",
    "**/*_Factory*.*",
    "**/*_Provide*Factory*.*",
    "**/*_MembersInjector*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/hilt_aggregated_deps/**",
    "**/*_HiltModules*.*",
    "**/*_ViewBinding*.*",
    "**/DataBinderMapperImpl.*",
    "**/DataBindingInfo.*",
    "**/*Screen*.*",
    "**/*Composable*.*",
    "**/theme/**"
)

val debugTree = mutableListOf<FileTree>()
val executionData = mutableListOf<FileCollection>()

subprojects {
    val subprojectName = name
    afterEvaluate {
        val isAndroidApp = plugins.hasPlugin("com.android.application")
        val isAndroidLib = plugins.hasPlugin("com.android.library")

        if (isAndroidApp || isAndroidLib) {
            val variant = "devDebug"
            val testTaskName = "test${variant.capitalize()}UnitTest"
            
            if (tasks.findByName(testTaskName) != null) {
                debugTree.add(fileTree("${buildDir}/tmp/kotlin-classes/${variant}") {
                    setExcludes(fileFilter)
                })
                debugTree.add(fileTree("${buildDir}/intermediates/javac/${variant}/classes") {
                    setExcludes(fileFilter)
                })
                executionData.add(files("${buildDir}/jacoco/${testTaskName}.exec"))
            }
        } else {
            // JVM module (like :domain)
            debugTree.add(fileTree("${buildDir}/classes/kotlin/main") {
                setExcludes(fileFilter)
            })
            executionData.add(files("${buildDir}/jacoco/test.exec"))
        }
    }
}

tasks.register<JacocoReport>("jacocoFullReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for all modules"

    dependsOn(subprojects.map { it.tasks.withType<Test>() })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    sourceDirectories.setFrom(files(subprojects.map { 
        val path = if (it.plugins.hasPlugin("com.android.application") || it.plugins.hasPlugin("com.android.library")) {
            "src/main/java"
        } else {
            "src/main/kotlin"
        }
        "${it.projectDir}/$path" 
    }))
    
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(files(executionData))
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "Reporting"
    description = "Verify Jacoco coverage for all modules"

    dependsOn("jacocoFullReport")

    sourceDirectories.setFrom(files(subprojects.map { 
        val path = if (it.plugins.hasPlugin("com.android.application") || it.plugins.hasPlugin("com.android.library")) {
            "src/main/java"
        } else {
            "src/main/kotlin"
        }
        "${it.projectDir}/$path" 
    }))
    
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(files(executionData))

    violationRules {
        rule {
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}
