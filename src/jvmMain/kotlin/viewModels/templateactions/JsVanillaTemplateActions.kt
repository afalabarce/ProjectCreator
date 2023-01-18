package io.github.afalabarce.projectcreator.viewModels.templateactions

import io.github.afalabarce.projectcreator.entities.ISettings
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.viewModels.templateactions.interfaces.TemplateProjectAction
import java.nio.file.Path

class JsVanillaTemplateActions: TemplateProjectAction() {
    override fun loadProject(templateProject: TemplateProject, onLoadTemplateProject: (TemplateProject) -> Unit) {
        // TODO
    }

    override fun makeNewProject(
        templateProject: TemplateProject,
        onWorking: (Boolean, String) -> Unit,
        onNewProject: (TemplateProject) -> Unit,
        onError: (String) -> Unit,
    ) {
        // TODO
    }

    override fun templateUpdate(
        templateProject: TemplateProject,
        field: String,
        value: Any,
        onTemplateUpdate: (TemplateProject) -> Unit,
    ) {
        // TODO
    }

    override fun renameProject(settings: ISettings, projectPath: Path): Boolean {
        // TODO
        return false
    }
}