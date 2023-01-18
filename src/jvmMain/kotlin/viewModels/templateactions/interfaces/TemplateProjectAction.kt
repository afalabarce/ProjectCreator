package io.github.afalabarce.projectcreator.viewModels.templateactions.interfaces

import io.github.afalabarce.projectcreator.entities.ISettings
import io.github.afalabarce.projectcreator.entities.TemplateProject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists

abstract class TemplateProjectAction: CoroutineScope by CoroutineScope(Dispatchers.IO) {
    abstract fun loadProject(templateProject: TemplateProject, onLoadTemplateProject: (TemplateProject) -> Unit)
    abstract fun makeNewProject(templateProject: TemplateProject, onWorking:(Boolean, String) -> Unit, onNewProject: (TemplateProject) -> Unit, onError: (String) -> Unit)
    abstract fun templateUpdate(templateProject: TemplateProject, field: String, value: Any, onTemplateUpdate: (TemplateProject) -> Unit)
    abstract fun renameProject(settings: ISettings, projectPath: Path): Boolean

    @Throws(IOException::class)
    protected fun copyFolder(source: Path, target: Path, vararg options: CopyOption): Boolean {
        try {
            Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.createDirectories(target.resolve(source.relativize(dir)))
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.copy(file, target.resolve(source.relativize(file)), *options)
                    return FileVisitResult.CONTINUE
                }
            })

            return target.exists()
        } catch (ex: Exception) {
            return false
        }
    }

}