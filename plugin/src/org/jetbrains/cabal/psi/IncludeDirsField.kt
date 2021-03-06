package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.vfs.VirtualFile

class IncludeDirsField(node: ASTNode) : MultiValueField(node), PathsField {

    override fun isValidCompletionFile(file: VirtualFile): Boolean = file.isDirectory

    override fun validVirtualFile(file: VirtualFile): Boolean = file.isDirectory
}