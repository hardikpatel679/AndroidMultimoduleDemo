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
    alias(libs.plugins.google.services) apply false
}

apply(plugin = "jacoco")

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

val fileFilter = listOf(
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

tasks.register<JacocoReport>("jacocoFullReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for all modules"

    // Depend on valid test tasks. Filter out mock/uat to avoid Google Services errors.
    val subprojectsWithTests = subprojects.flatMap { subproject ->
        subproject.tasks.withType<Test>().matching { task ->
            val name = task.name.lowercase()
            // Only include dev, prod, or generic test tasks
            name.contains("dev") || name.contains("prod") || (!name.contains("mock") && !name.contains("uat"))
        }
    }
    dependsOn(subprojectsWithTests)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // Source directories
    val sourceDirs = subprojects.flatMap { subproject ->
        listOf("src/main/java", "src/main/kotlin").map { path ->
            file("${subproject.projectDir}/$path")
        }
    }.filter { it.exists() }
    sourceDirectories.setFrom(files(sourceDirs))

    // Class directories - collect from all variants to be flavor-agnostic
    val classDirs = subprojects.map { subproject ->
        val subProjectBuildDir = subproject.layout.buildDirectory.get().asFile
        fileTree(subProjectBuildDir) {
            include("**/tmp/kotlin-classes/**")
            include("**/intermediates/javac/**/classes/**")
            include("**/classes/kotlin/main/**")
            exclude(fileFilter)
        }
    }
    classDirectories.setFrom(files(classDirs))

    // Execution data - collect all .exec files found in subprojects
    val execData = subprojects.map { subproject ->
        fileTree(subproject.layout.buildDirectory.get().asFile) {
            include("**/*.exec")
        }
    }
    executionData.setFrom(files(execData))
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "Reporting"
    description = "Verify Jacoco coverage for all modules"

    dependsOn("jacocoFullReport")

    // Use same source/class/exec as the report
    val fullReport = tasks.named<JacocoReport>("jacocoFullReport").get()
    sourceDirectories.setFrom(fullReport.sourceDirectories)
    classDirectories.setFrom(fullReport.classDirectories)
    executionData.setFrom(fullReport.executionData)

    violationRules {
        rule {
            limit {
                val threshold = project.findProperty("coverageThreshold")?.toString() ?: "0.90"
                minimum = threshold.toBigDecimal()
            }
        }
    }
}
