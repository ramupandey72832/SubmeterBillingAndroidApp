plugins {
    id("java-library")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":rrAllUtilityLib"))
    implementation(project(":submeterCommonLib"))
    implementation(project(":submeterServiceLib"))
}