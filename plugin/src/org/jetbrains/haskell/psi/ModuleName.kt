package org.jetbrains.haskell.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import org.jetbrains.haskell.fileType.HaskellFile
import org.jetbrains.haskell.fileType.HaskellFileType
import org.jetbrains.haskell.psi.reference.ModuleReference
import org.jetbrains.haskell.scope.GlobalScope

/**
 * Created by atsky on 3/29/14.
 */
class ModuleName(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun getReference(): PsiReference? {
        return ModuleReference(this)
    }

    fun findModuleFile(): HaskellFile? {
        val nameToFind = text!!
        val module = ModuleUtilCore.findModuleForPsiElement(this) ?: return null;

        val sourceRoots = ModuleRootManager.getInstance(module)!!.getSourceRoots(true)

        var result: VirtualFile? = null

        for (root in sourceRoots) {
            trace("", root) { name, file ->
                if (nameToFind == name) {
                    result = file
                    false
                } else {
                    true
                }
            }
        }

        if (result != null) {
            val psiFile = PsiManager.getInstance(project).findFile(result!!)
            return psiFile as HaskellFile
        }

        val haskellFile = GlobalScope.getModule(project, nameToFind)
        if (haskellFile != null) {
            return haskellFile
        }

        return null
    }


    fun trace(suffix: String, dir: VirtualFile, function: (String, VirtualFile) -> Boolean): Boolean {
        for (file in dir.children!!) {
            if (file.isDirectory) {
                if (!trace(suffix + file.name + ".", file, function)) {
                    return false;
                }
            } else {
                if (file.fileType == HaskellFileType.INSTANCE) {
                    if (!function(suffix + file.nameWithoutExtension, file)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}