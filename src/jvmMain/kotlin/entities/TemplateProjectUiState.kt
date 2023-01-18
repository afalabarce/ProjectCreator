package io.github.afalabarce.projectcreator.entities

data class TemplateProjectUiState(
    val isWorking: Boolean = false,
    val workingMessage: String = "",
    val currentTemplate: TemplateProject? = null,
    val templates: List<TemplateProject> = listOf(),
)
