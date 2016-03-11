package org.jetbrains.haskell.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.haskell.psi.Module
import org.jetbrains.haskell.psi.SomeId
import org.jetbrains.haskell.scope.ModuleScope
import java.util.*

/**
 * Created by atsky on 4/11/14.
 */
class SomeIdReference(val someId : SomeId) : PsiReferenceBase<SomeId>(
        someId,
        TextRange(0, someId.textRange!!.length)) {

    override fun resolve(): PsiElement? {
        val module = Module.findModule(someId)
        if (module != null) {
            val text = someId.text!!
            if (Character.isUpperCase(text[0])) {
                //for (function in ModuleScope(module).getVisibleTypes()) {
                //    if (function.getName() == text) {
                //        return function
                //    }
                //}
                for (function in ModuleScope(module).getVisibleConstructors()) {
                    if (function.getDeclarationName() == text) {
                        return function
                    }
                }
            } else {
                //for (function in ModuleScope(module).getVisibleValues().flatMap { it.getNames() }) {
                //    if (function.getText() == text) {
                //        return function
                //    }
                //}
            }
        }
        return null;
    }


    override fun getVariants(): Array<Any> {
        val module = Module.findModule(someId)
        var result = ArrayList<Any>()
        if (module != null) {
            //result.addAll(ModuleScope(module).getVisibleTypes())
            result.addAll(ModuleScope(module).getVisibleConstructors())
            //result.addAll(ModuleScope(module).getVisibleValues().flatMap { it.getNames() })
        }
        return result.toArray()
    }


}