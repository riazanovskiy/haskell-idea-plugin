package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.vfs.VirtualFile

class RepoSubdirField(node: ASTNode) : SingleValueField(node), PathsField {

    override fun validVirtualFile(file: VirtualFile): Boolean = true

    override fun getSourceDirs(originalRootDir: VirtualFile): List<VirtualFile> = listOf()
}