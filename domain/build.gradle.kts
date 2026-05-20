plugins {
    alias(libs.plugins.kotlinjvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.serialization.json)
}
