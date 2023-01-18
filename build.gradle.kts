import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "io.github.afalabarce"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            kotlinOptions.freeCompilerArgs += listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.RequiresOptIn",
                "-Xallow-jvm-ir-dependencies",
                "-Xskip-prerelease-check"
            )
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.1.0-alpha04")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "io.github.afalabarce.projectcreator.MainKt"
        fromFiles(project.fileTree("libs/") { include("**/*.jar") })

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TaRSyS Mobile Project Creator"
            description = "Aplicación para crear Proyectos Mobile desde Proyectos Plantilla"
            packageVersion = "1.0.0"
            includeAllModules = true
            javaHome = "/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk"
            vendor = "Antonio Fdez. Alabarce"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            windows {
                iconFile.set(project.file("src/jvmMain/resources/mipmap/ic_launcher.ico"))
                dirChooser = true
                //javaHome = "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2022.2.2\\jbr"
                menuGroup = "TaRSyS Mobile Project Creator"
            }

            macOS{
                packageBuildVersion = "1.0.0"
                bundleID = "io.github.afalabarce.projectcreator.MainKt"
                dockName = "TaRSyS Mobile Project Creator"
                javaHome = "/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk"
                iconFile.set(project.file("src/jvmMain/resources/mipmap/ic_launcher.icns"))
                mainClass = "io.github.afalabarce.projectcreator.MainKt"
                appCategory = "public.app-category.developer-tools"
            }
        }
    }
}
