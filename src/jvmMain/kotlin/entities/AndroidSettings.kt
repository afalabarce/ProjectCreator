package io.github.afalabarce.projectcreator.entities

data class AndroidSettings(
    val projectName: String = "",
    val originalPackageName: String = "",
    val packageName: String = "",
): ISettings
