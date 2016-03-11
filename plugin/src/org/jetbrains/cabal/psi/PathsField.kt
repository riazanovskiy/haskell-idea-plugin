package org.jetbrains.cabal.psi

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.jetbrains.cabal.CabalFile
import java.io.File
import java.util.*

interface PathsField: PsiElement {

    fun isValidCompletionFile(file: VirtualFile): Boolean = true

    fun validVirtualFile(file: VirtualFile): Boolean

    fun validRelativity(path: File): Boolean = true

    fun getCabalFile(): CabalFile = (containingFile as CabalFile)

    fun getParentBuildSection(): BuildSection? {
        var parent = parent
        while (parent != null) {
            if (parent is BuildSection) return parent
            parent = parent.parent
        }
        return null
    }

    fun getSourceDirs(originalRootDir: VirtualFile): List<VirtualFile> = listOf(originalRootDir)


    fun getParentDirs(prefixPath: Path, originalRootDir: VirtualFile): List<VirtualFile> {

        fun getParentPathFrom(sourceDir: VirtualFile) = File(prefixPath.getPathWithParent(sourceDir)).parent

        fun findFileByPath(path: String) = originalRootDir.fileSystem.findFileByPath(path.replace(File.separatorChar, '/'))

        if (!validRelativity(prefixPath.getFile())) return listOf()
        val parentPaths = (getSourceDirs(originalRootDir).map { getParentPathFrom(it) }).filterNotNull()
        return (parentPaths.map { findFileByPath(it) }).filterNotNull().filter { it.isDirectory }
    }

    fun getNextAvailableFile(prefixPath: Path, originalRootDir: VirtualFile): List<String> {
        val parentDirs = getParentDirs(prefixPath, originalRootDir)
        val completionFiles = ArrayList<VirtualFile>()
        for (dir in parentDirs) {
            completionFiles.addAll(dir.children!!.filter { isValidCompletionFile(it) })
        }
        return completionFiles.map { it.name.plus(if (it.isDirectory) "/" else "") }
    }
}