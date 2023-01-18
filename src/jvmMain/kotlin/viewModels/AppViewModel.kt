package io.github.afalabarce.projectcreator.viewModels

import androidx.compose.ui.text.intl.Locale
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.entities.TemplateProjectUiState
import io.github.afalabarce.projectcreator.utilities.projectType
import io.github.afalabarce.projectcreator.utilities.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*

class AppViewModel : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val templatesPathWatcher: Path
    private val pathWatcherService: WatchService
    private val watcherKey: WatchKey
    private val templatesFolder = "${System.getProperty("user.home")}${File.separator}${Locale.current.stringResource("app_name")}${File.separator}templates"
    private val _appUiState: MutableStateFlow<TemplateProjectUiState> by lazy { MutableStateFlow(TemplateProjectUiState()) }
    val appUiState: StateFlow<TemplateProjectUiState>
        get() = this._appUiState

    init {
        val templatesDirectory = File(templatesFolder)
        if (!templatesDirectory.exists())
            templatesDirectory.mkdirs()

        this.pathWatcherService = FileSystems.getDefault().newWatchService()
        this.templatesPathWatcher = templatesDirectory.toPath()
        this.watcherKey = this.templatesPathWatcher.register(
            this.pathWatcherService,
            ENTRY_CREATE,
            ENTRY_DELETE,
            ENTRY_MODIFY
        )

        this.launch(Dispatchers.IO) {
            this@AppViewModel.folderWatcher { files ->
                this@AppViewModel._appUiState.update { old ->
                    old.copy(templates = files.filter { f -> f.isDirectory }.map { template ->
                        TemplateProject(
                            path = template.absolutePath,
                            title = template.name,
                            projectType = template.projectType()
                        )
                    })
                }
            }
        }

    }

    //region public functions

    fun loadTemplates() {
        val templatesDirectory = File(templatesFolder)
        if (templatesDirectory.exists() && templatesDirectory.isDirectory) {
            this._appUiState.update { old ->
                old.copy(templates =
                templatesDirectory.listFiles()?.let {projects ->
                    projects.filter { f -> f.isDirectory }.map { template ->
                        TemplateProject(
                            path = template.absolutePath,
                            title = template.name,
                            projectType = template.projectType()
                        )
                    }?.toList()
                } ?: listOf())
            }
        } else if (!templatesDirectory.exists()) {
            templatesDirectory.mkdir()
        }
    }

    fun updateTemplate(templateProject: TemplateProject?, field: String, value: Any) {
        if (templateProject != null) {
            when (field) {
                "FolderDestination" -> this._appUiState.update { old ->
                    old.copy(
                        currentTemplate = templateProject.copy(
                            folderDestination = value as String
                        )
                    )
                }

                "NoError" -> this._appUiState.update { old ->
                    old.copy(
                        currentTemplate = templateProject.copy(
                            withError = false,
                            errorMessage = ""
                        )
                    )
                }

                else -> templateProject.templateActions?.templateUpdate(templateProject, field, value) { newTemplate ->
                    this._appUiState.update { old -> old.copy(currentTemplate = newTemplate) }
                }

            }
        }
    }

    fun loadTemplate(templateProject: TemplateProject?) {
        if (templateProject != null) {
            val templatePath = File(templateProject.path)

            if (templatePath.exists() && templatePath.isDirectory) {
                templateProject.templateActions?.loadProject(templateProject){ loadedTemplate ->
                    this._appUiState.update { old ->
                        old.copy(
                            currentTemplate = loadedTemplate.copy()
                        )
                    }
                }
            }
        } else {
            this._appUiState.update { old -> old.copy(currentTemplate = null) }
        }
    }

    fun makeNewProject(templateProject: TemplateProject){
        templateProject.templateActions?.makeNewProject(
            templateProject =  templateProject,
            onWorking = { isWorking, message -> this._appUiState.update { old -> old.copy(isWorking = isWorking, workingMessage = message) } },
            onError = { errorMessage -> this.notifyError(errorMessage) },
            onNewProject = { newTemplateProject ->
                this._appUiState.update { old -> old.copy(currentTemplate = newTemplateProject) }
            }
        )
    }

    //endregion

    //region Private general purpose functions

    private fun notifyError(message: String){
        this@AppViewModel._appUiState.update { old -> old.copy(isWorking = false, workingMessage = "") }
        this._appUiState.update { old ->
            old.copy(
                currentTemplate = old.currentTemplate?.copy(
                    withError = true,
                    errorMessage = message
                )
            )
        }
    }

    private suspend fun folderWatcher(onWatch: (List<File>) -> Unit) {
        withContext(Dispatchers.IO) {
            while (true) {
                val key = this@AppViewModel.pathWatcherService.take()
                val templatesDirectory = File(templatesFolder)

                key.pollEvents().filter { evt ->
                    listOf(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY).any { x ->
                        x == evt.kind()
                    } && evt is WatchEvent<*>
                }.forEach { _ ->
                    templatesDirectory.listFiles()?.let { onWatch(it.filter { f -> f.isDirectory }.toList()) }
                    key.reset()
                    return@forEach
                }
            }
        }
    }

    //endregion

}