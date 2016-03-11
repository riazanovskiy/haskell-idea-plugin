package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage
import org.jetbrains.cabal.parser.EXECUTABLE_FIELDS
import org.jetbrains.cabal.parser.IF_ELSE
import java.util.*

/**
 * @author Evgeny.Kurbatsky
 */
class Executable(node: ASTNode) : BuildSection(node) {

    fun getExecutableName(): String {
        val res = getSectName() ?: throw IllegalStateException()
        return res
    }

    override fun check(): List<ErrorMessage> {
        if (getField(MainFileField::class.java) == null) return listOf(ErrorMessage(getSectTypeNode(), "main-is field is required", "error"))
        return listOf()
    }

    override fun getAvailableFieldNames(): List<String> {
        var res = ArrayList<String>()
        res.addAll(EXECUTABLE_FIELDS.keys)
        res.addAll(IF_ELSE)
        return res
    }

    fun getMainFile(): Path? = getField(MainFileField::class.java)?.getValue() as Path?
}