package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class IncludesField(node: ASTNode) : MultiValueField(node), PathsField {

    override fun validVirtualFile(file: VirtualFile): Boolean = file.isDirectory

    override fun validRelativity(path: File): Boolean = !path.isAbsolute

    override fun getSourceDirs(originalRootDir: VirtualFile): List<VirtualFile>
            = (getParentBuildSection()!!.getIncludeDirs().map { it.getVirtualFile(originalRootDir) }).filterNotNull()
}