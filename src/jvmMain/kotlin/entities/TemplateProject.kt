package io.github.afalabarce.projectcreator.entities

import io.github.afalabarce.projectcreator.enums.ProjectType
import io.github.afalabarce.projectcreator.viewModels.templateactions.*
import io.github.afalabarce.projectcreator.viewModels.templateactions.interfaces.TemplateProjectAction

data class TemplateProject(
    val path: String = "",
    val title: String = "",
    val androidSettings: AndroidSettings? = null,
    val iosSettings: IosSettings? = null,
    val flutterSettings: FlutterSettings? = null,
    val folderDestination: String = "",
    val withError: Boolean = false,
    val errorMessage: String = "",
    val projectType: ProjectType = ProjectType.Android,
){
    val templateActions: TemplateProjectAction?
        get() = when(this.projectType){
            ProjectType.None -> null
            ProjectType.Android -> AndroidTemplateActions()
            ProjectType.AndroidKts -> AndroidKtsTemplateActions()
            ProjectType.IOS -> IosTemplateActions()
            ProjectType.HybridAngular -> AngularTemplateActions()
            ProjectType.HybridVue -> VueTemplateActions()
            ProjectType.HybridReact -> ReactTemplateActions()
            ProjectType.HybridJsVanilla -> JsVanillaTemplateActions()
            ProjectType.Flutter -> FlutterTemplateActions()
        }
}
