package io.github.afalabarce.projectcreator.viewModels.templateactions

import androidx.compose.ui.text.intl.Locale
import io.github.afalabarce.projectcreator.entities.ISettings
import io.github.afalabarce.projectcreator.entities.IosSettings
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.utilities.stringResource
import io.github.afalabarce.projectcreator.viewModels.templateactions.interfaces.TemplateProjectAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.impl.Log
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.*

class IosTemplateActions: TemplateProjectAction() {

    override fun loadProject(templateProject: TemplateProject, onLoadTemplateProject: (TemplateProject) -> Unit) {
        val templatePath = File(templateProject.path)
        val templatePathFiles = templatePath.listFiles()
        val xProjPath = templatePathFiles?.firstOrNull { x -> x.name.lowercase().endsWith("xcodeproj") }
        val projPath = templatePathFiles?.firstOrNull { x ->
            x.name.lowercase() == xProjPath!!.name.lowercase().replace(".xcodeproj", "")
        }
        val infoPlist = projPath?.listFiles()?.firstOrNull { x -> x.name.lowercase() == "info.plist" }

        if (infoPlist != null) {
            val projectName = projPath.name
            val infoLines = xProjPath!!.listFiles()!!.firstOrNull { x -> x.name.lowercase() == "project.pbxproj" }
                ?.readLines(Charsets.UTF_8)
            val baseBundleId = infoLines?.firstOrNull { l ->
                l.trim().startsWith("PRODUCT_BUNDLE_IDENTIFIER") && l.trim().endsWith("$projectName;")
            } ?: ""
            val devTeam = infoLines?.firstOrNull { l -> l.trim().startsWith("DEVELOPMENT_TEAM") } ?: ""

            onLoadTemplateProject(
                templateProject.copy(
                    iosSettings = (templateProject.iosSettings ?: IosSettings()).copy(
                        productName = projectName,
                        originalProductName = projectName,
                        originalExecutableName = projectName,
                        executableName = projectName,
                        originalBundleIdentifier = baseBundleId.replace("PRODUCT_BUNDLE_IDENTIFIER", "").trim()
                            .replace("=", "").replace(";", "").trim(),
                        originalDevelopmentTeam = devTeam.replace("DEVELOPMENT_TEAM", "").trim().replace("=", "")
                            .replace(";", "").trim(),
                        developmentTeam = devTeam.replace("DEVELOPMENT_TEAM", "").trim().replace("=", "")
                            .replace(";", "").trim(),
                    ),
                )
            )
        }
    }

    override fun makeNewProject(
        templateProject: TemplateProject,
        onWorking: (Boolean, String) -> Unit,
        onNewProject: (TemplateProject) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (
            templateProject.iosSettings != null &&
            templateProject.iosSettings.productName.isNotEmpty() &&
            templateProject.iosSettings.developmentTeam.isNotEmpty() &&
            templateProject.iosSettings.executableName.isNotEmpty() &&
            templateProject.iosSettings.bundleIdentifier.isNotEmpty() &&
            templateProject.folderDestination.isNotEmpty()
        ){
            try{
                val folderProject = "${templateProject.folderDestination}${File.separator}${templateProject.iosSettings.productName}"
                val validDestFolder = File(templateProject.folderDestination).exists() && File(templateProject.folderDestination).isDirectory
                val nonExistentProjectFolder = validDestFolder && !File(folderProject).exists()
                val message = if (!validDestFolder)
                    Locale.current.stringResource("invalid_destination_path")
                else if (!nonExistentProjectFolder)
                    Locale.current.stringResource("destination_folder_exists", templateProject.folderDestination)
                else
                    ""

                if (templateProject.iosSettings.originalBundleIdentifier == templateProject.iosSettings.bundleIdentifier) {
                    onError(Locale.current.stringResource("dest_package_not_same_origin"))
                }else if(nonExistentProjectFolder) {
                    onWorking(true, Locale.current.stringResource(
                                resourceKey =  "making_new_project",
                                templateProject.iosSettings.productName
                            )
                        )
                    // And now... we launch project creation...
                    this.launch(Dispatchers.IO) {
                        if (this@IosTemplateActions.copyFolder(Path(templateProject.path), Path(folderProject))) {
                            try {
                                this@IosTemplateActions.renameProject(
                                    templateProject.iosSettings,
                                    projectPath = File(folderProject).toPath()
                                )

                                // finally, we notify...
                                onWorking(false, "")
                                onError(Locale.current.stringResource("successful_creation"))
                            } catch (ex: Exception) {
                                onError(ex.message ?: ex.toString())
                            }
                        }
                    }
                }else {
                    onError(message)
                }

            }catch (ex:Exception){
                onWorking(false, "")
                onError(ex.message ?: ex.toString())
            }
        }else{
            onError(Locale.current.stringResource("ios_mandatory_fields"))
        }
    }

    override fun templateUpdate(
        templateProject: TemplateProject,
        field: String,
        value: Any,
        onTemplateUpdate: (TemplateProject) -> Unit,
    ) {
        onTemplateUpdate(templateProject.copy(
            iosSettings = (templateProject.iosSettings ?: IosSettings()).copy(
                developmentLanguage = if (field == "IosDevelopmentLanguage") value as String else (templateProject.iosSettings ?: IosSettings()).developmentLanguage,
                productName = if (field == "IosProductName") value as String else (templateProject.iosSettings ?: IosSettings()).productName,
                bundleIdentifier = if (field == "IosBundleIdentifier") value as String else (templateProject.iosSettings ?: IosSettings()).bundleIdentifier,
                executableName = if (field == "IosExecutableName") value as String else (templateProject.iosSettings ?: IosSettings()).executableName,
                developmentTeam = if (field == "IosDevelopmentTeam") value as String else (templateProject.iosSettings ?: IosSettings()).developmentTeam,
            )
        ))
    }

    override fun renameProject(settings: ISettings, projectPath: Path): Boolean {
        check(settings is IosSettings)

        Files.walkFileTree(projectPath,  object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                //if folder like Original Project name... we need to rename...
                if (dir.name.contains(settings.originalProductName)) {
                    try {
                        dir.filter { x -> !x.isDirectory() && x.name.contains(settings.originalProductName) }
                            .forEach { f ->
                                f.toFile().renameTo(
                                    File(
                                        f.absolutePathString()
                                            .replace(settings.originalProductName, settings.productName)
                                    )
                                )
                                File("${dir.absolutePathString()}${File.separator}${f.name}").deleteRecursively()
                            }

                        this@IosTemplateActions.copyFolder(
                            dir,
                            File(
                                dir.absolutePathString()
                                    .replace(settings.originalProductName, settings.productName)
                            ).toPath()
                        )

                    } catch (ex: Exception) {
                        Log.info("Error: ${ex.message ?: ex.toString()}\n ${ex.stackTraceToString()}")
                    }
                }

                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                try {
                    if (dir?.name?.contains(settings.originalProductName) == true) {
                        dir.toFile().deleteRecursively()
                    }
                } catch (ex: Exception) {
                    Log.info(ex.message ?: ex.toString())
                }
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                try {
                    val newFile = file.absolutePathString().replace(settings.originalProductName, settings.productName)
                    val lines = file.toFile().readText(Charsets.UTF_8)
                        .replace(settings.originalBundleIdentifier, settings.bundleIdentifier)
                        .replace(settings.originalDevelopmentTeam, settings.developmentTeam)
                        .replace(settings.originalProductName, settings.productName)

                    if (file.name.contains(settings.originalProductName)) {
                        file.toFile().copyTo(File(newFile), overwrite = true)
                        File(newFile.replace("${settings.productName}.${file.extension}" ,file.name)).deleteRecursively()
                    }

                    File(newFile).writeText(lines, Charsets.UTF_8)
                } catch (ex: Exception) {
                    Log.info("${ex.message ?: ex.toString()}\n${ex.stackTraceToString()}")
                }

                return FileVisitResult.CONTINUE
            }
        })

        return false
    }
}