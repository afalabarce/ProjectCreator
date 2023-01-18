package io.github.afalabarce.projectcreator.entities

data class FlutterSettings(
    val androidPackageName: String = "",
    val iosBundleId: String = ""
): ISettings
