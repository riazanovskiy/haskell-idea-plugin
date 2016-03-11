package org.jetbrains.haskell.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.haskell.psi.ModuleName

/**
 * Created by atsky on 4/4/14.
 */

class ModuleReference(moduleName : ModuleName) : PsiReferenceBase<ModuleName>(
        moduleName,
        TextRange(0, moduleName.textRange!!.length)) {

    override fun resolve(): PsiFile? {
        return element!!.findModuleFile()
    }

    override fun getVariants(): Array<Any> = arrayOf()
}