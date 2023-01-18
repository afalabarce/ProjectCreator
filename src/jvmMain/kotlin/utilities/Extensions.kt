package io.github.afalabarce.projectcreator.utilities

import io.github.afalabarce.projectcreator.enums.ProjectType
import java.io.File

fun File.projectType(): ProjectType = try{
    if (this.listFiles() == null)
        ProjectType.None
    else if (!this.listFiles()!!.any { x -> x.name.lowercase() == "pubspec.yaml" } && (this.listFiles()!!.any { x -> x.name.lowercase().contains("xcodeproj") } ||
             this.listFiles()!!.flatMap { x -> x.listFiles()?.toList() ?: listOf() }.any { x -> x.name.lowercase().contains("xcodeproj") }))
        ProjectType.IOS
    else  if (this.listFiles()!!.any { x -> x.name.lowercase() == "package.json" })
        ProjectType.HybridAngular
    else  if (this.listFiles()!!.any { x -> x.name.lowercase() == "pubspec.yaml" })
        ProjectType.Flutter
    else  if (this.listFiles()!!.any { x -> x.name.lowercase() == "build.gradle" })
        ProjectType.Android
    else
        ProjectType.None
}catch (_: Exception){
    ProjectType.None
}
