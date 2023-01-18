package io.github.afalabarce.projectcreator.viewModels.templateactions

import androidx.compose.ui.text.intl.Locale
import io.github.afalabarce.projectcreator.entities.AndroidSettings
import io.github.afalabarce.projectcreator.entities.ISettings
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.utilities.stringResource
import io.github.afalabarce.projectcreator.viewModels.templateactions.interfaces.TemplateProjectAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeLines

class AndroidTemplateActions: TemplateProjectAction() {
    private fun getModuleNames(projectPath: Path): List<String> {
        val settingsGradle = projectPath.toFile().listFiles { f -> f.name.lowercase().startsWith("settings.gradle") }?.firstOrNull()
        if (settingsGradle != null) {
            val settingsLines = settingsGradle.readLines(Charsets.UTF_8)
            return settingsLines.filter { x -> x.startsWith("include '", true) }
                .map { l ->
                    l.replace("include", "")
                        .replace("'", "")
                        .replace(":", "").trim()
                }
        }

        return listOf()
    }

    private fun updateAndroidPackageName(originalPackageName: String, packageName: String, projectPath: Path): Boolean {
        try {
            Files.walkFileTree(projectPath, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.isRegularFile()) {
                        try {
                            val fileContent = file.toFile().readLines(Charsets.UTF_8).toMutableList()
                            fileContent.filter { line -> line.contains(originalPackageName) }.forEach { updateLine ->
                                val lineIndex = fileContent.indexOf(updateLine)
                                fileContent[lineIndex] = updateLine.replace(originalPackageName, packageName)
                            }

                            file.writeLines(fileContent, Charsets.UTF_8)
                        } catch (ex: Exception) {
                            // Do nothing...
                        }
                    }
                    return FileVisitResult.CONTINUE
                }
            })

            return true
        } catch (_: Exception) {

        }

        return false
    }

    private fun getAndroidProjectSourceFolders(projectPath: Path): List<File> = this.getModuleNames(projectPath)
        .map { mod -> File("${projectPath.absolutePathString()}${File.separator}$mod${File.separator}src${File.separator}main${File.separator}java") }
        .union(
            this.getModuleNames(projectPath)
                .map { mod -> File("${projectPath.absolutePathString()}${File.separator}$mod${File.separator}src${File.separator}test${File.separator}java") }
        ).union(
            this.getModuleNames(projectPath)
                .map { mod -> File("${projectPath.absolutePathString()}${File.separator}$mod${File.separator}src${File.separator}androidTest${File.separator}java") }
        ).toList()

    private fun updateAndroidProjectFolders(originalPackageName: String, packageName: String, projectPath: Path): Boolean {
        try {
            val moduleSrcFolders = this.getAndroidProjectSourceFolders(projectPath)

            val newSrcFolders = moduleSrcFolders.associate { newSrc ->
                File("$newSrc${File.separator}${originalPackageName.replace(".", File.separator)}") to
                        File("$newSrc${File.separator}${packageName.replace(".", File.separator)}").apply { mkdirs() }
            }
            if (newSrcFolders.isNotEmpty() && newSrcFolders.size == moduleSrcFolders.size) {
                newSrcFolders.forEach { (k, v) ->
                    this.copyFolder(k.toPath(), v.toPath())
                    k.deleteRecursively()
                }

                return true
            }
        } catch (_: Exception) {

        }

        return false
    }

    private fun updateAndroidProjectName(projectName: String, projectPath: Path): Boolean {
        try {
            val settingsGradle =
                projectPath.toFile().listFiles { f -> f.name.lowercase().startsWith("settings.gradle") }?.firstOrNull()
            if (settingsGradle != null) {
                val settingsLines = settingsGradle.readLines(Charsets.UTF_8).toMutableList()
                val projectLine = settingsLines.firstOrNull { l -> l.startsWith("rootProject.name") }
                if (!projectLine.isNullOrEmpty()) {
                    settingsLines.remove(projectLine)
                }
                settingsLines.add("/* Added by [TaRSyS Mobile Project Creator] */")
                settingsLines.add("rootProject.name = \"$projectName\"")
                settingsGradle.writeText(settingsLines.joinToString("\n"), Charsets.UTF_8)

                return true
            }
        } catch (_: Exception) {

        }

        return false
    }

    override fun loadProject(templateProject: TemplateProject, onLoadTemplateProject: (TemplateProject) -> Unit) {
        val templatePath = File(templateProject.path)
        val settingsFile = templatePath.listFiles()?.firstOrNull { x -> x.name == "settings.gradle" }
        val buildFile = templatePath.listFiles()?.firstOrNull { x -> x.name == "build.gradle" }
        if (settingsFile != null && buildFile != null) {
            val settingsContent = settingsFile.readLines(charset = Charsets.UTF_8)
            val buildContent = buildFile.readLines(charset = Charsets.UTF_8)
            val rootProjectName = (settingsContent.firstOrNull { x -> x.contains("rootProject.name") } ?: "")
                .replace("rootProject.name", "")
                .replace("=", "")
                .replace("\"", "")
                .trim()


            val packageName = (buildContent.firstOrNull { x -> x.contains("base_package") } ?: "")
                .replace("base_package", "")
                .replace("=", "")
                .replace("'", "")
                .trim()

            onLoadTemplateProject(
                templateProject.copy(
                    androidSettings = (templateProject.androidSettings ?: AndroidSettings()).copy(
                        projectName = rootProjectName,
                        originalPackageName = packageName,
                        packageName = packageName
                    ),
                )
            )
        }
    }

    override fun makeNewProject(templateProject: TemplateProject, onWorking:(Boolean, String) -> Unit, onNewProject: (TemplateProject) -> Unit, onError: (String) -> Unit) {
        if (
            templateProject.androidSettings != null &&
            templateProject.androidSettings.projectName.isNotEmpty() &&
            templateProject.androidSettings.packageName.isNotEmpty() &&
            templateProject.folderDestination.isNotEmpty()) {
            try {
                val folderProject = "${templateProject.folderDestination}${File.separator}${templateProject.androidSettings.projectName}"
                val validDestFolder = File(templateProject.folderDestination).exists() && File(templateProject.folderDestination).isDirectory
                val nonExistentProjectFolder = validDestFolder && !File(folderProject).exists()
                val message = if (!validDestFolder)
                    Locale.current.stringResource("invalid_destination_path")
                else if (!nonExistentProjectFolder)
                    Locale.current.stringResource("destination_folder_exists", templateProject.folderDestination)
                else
                    ""

                if (templateProject.androidSettings.originalPackageName == templateProject.androidSettings.packageName) {
                    onError(Locale.current.stringResource("dest_package_not_same_origin"))
                } else if (nonExistentProjectFolder) {
                    onWorking(
                        true,
                        Locale.current.stringResource(
                            resourceKey =  "making_new_project",
                            templateProject.androidSettings.projectName
                        )
                    )

                    this.launch(Dispatchers.IO) {
                        // Firstly, we need to copy template folder to Dest Folder...
                        if (this@AndroidTemplateActions.copyFolder(Path(templateProject.path), Path(folderProject))) {
                            this@AndroidTemplateActions.renameProject(
                                settings = templateProject.androidSettings,
                                projectPath = Path(folderProject)
                            )
                            onWorking(false, "")
                            onError(Locale.current.stringResource("successful_creation"))
                        } else {
                            onWorking(false, "")
                            onError(Locale.current.stringResource("template_not_copied"))
                        }
                    }
                } else {
                    onError(message)
                }
            } catch (ex: Exception) {
                onWorking(false, "")
                onError(ex.message ?: ex.toString())
            }
        } else {
            onError(Locale.current.stringResource("android_mandatory_fields"))
        }
    }

    override fun templateUpdate(
        templateProject: TemplateProject,
        field: String,
        value: Any,
        onTemplateUpdate: (TemplateProject) -> Unit,
    ) {
        onTemplateUpdate(
            templateProject.copy(
                androidSettings = (templateProject.androidSettings ?: AndroidSettings()).copy(
                    projectName = if (field == "ProjectName") value as String else (templateProject.androidSettings
                        ?: AndroidSettings()).projectName,
                    packageName = if (field == "PackageName") value as String else (templateProject.androidSettings
                        ?: AndroidSettings()).packageName,
                )
            )
        )
    }

    override fun renameProject(settings: ISettings, projectPath: Path): Boolean {
        check(settings is AndroidSettings)
        return this.updateAndroidProjectName(settings.projectName, projectPath) &&
                this.updateAndroidProjectFolders(settings.originalPackageName, settings.packageName, projectPath) &&
                this.updateAndroidPackageName(settings.originalPackageName, settings.packageName, projectPath)
    }

}