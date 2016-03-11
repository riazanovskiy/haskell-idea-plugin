package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.vfs.VirtualFile

class DataFilesField(node: ASTNode) : MultiValueField(node), PathsField {

    override fun validVirtualFile(file: VirtualFile): Boolean = !file.isDirectory

    override fun getSourceDirs(originalRootDir: VirtualFile): List<VirtualFile>
            = listOf(getCabalFile().getDataDir()?.getVirtualFile(originalRootDir)  ?:  originalRootDir)
}