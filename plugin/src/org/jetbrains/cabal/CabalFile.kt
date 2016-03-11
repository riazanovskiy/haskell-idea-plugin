package org.jetbrains.cabal

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.cabal.parser.PKG_DESCR_FIELDS
import org.jetbrains.cabal.parser.TOP_SECTION_NAMES
import org.jetbrains.cabal.psi.*
import java.util.*


class CabalFile(provider: FileViewProvider) : PsiFileBase(provider, CabalLanguage.INSTANCE), FieldContainer {

    override fun getAvailableFieldNames(): List<String> {
        var res = ArrayList<String>()
        res.addAll(PKG_DESCR_FIELDS.keys)
        res.addAll(TOP_SECTION_NAMES)
        return res
    }

    override fun getFileType(): FileType {
        return CabalFileType.INSTANCE
    }
    override fun accept(visitor: PsiElementVisitor): Unit {
        visitor.visitFile(this)
    }

    fun getExecutables(): MutableList<Executable> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, Executable::class.java)
    }

    fun getFlagNames(): List<String> = PsiTreeUtil.getChildrenOfTypeAsList(this, Flag::class.java).map { it.getFlagName() }

    fun getDataDir(): Path? = getField(DataDirField::class.java)?.getValue() as Path?
}
