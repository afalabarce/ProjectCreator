package io.github.afalabarce.projectcreator.enums

enum class ProjectType {
    None,
    Android,
    AndroidKts,
    IOS,
    HybridAngular,
    HybridVue,
    HybridReact,
    HybridJsVanilla,
    Flutter;

    fun isHybrid(): Boolean = listOf(HybridAngular, HybridVue, HybridReact, HybridJsVanilla,).any { x -> x == this }
}