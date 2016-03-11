package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage
import java.util.*

class ElseCondition(node: ASTNode) : Section(node) {

    override fun check(): List<ErrorMessage> {
        if (getSectChildren().size == 0) listOf(ErrorMessage(getSectTypeNode(), "empty else section is not allowed", "error"))
        return listOf()
    }

    override fun getAvailableFieldNames(): List<String> {
        var res = ArrayList<String>()
        res.addAll((parent as Section).getAvailableFieldNames())
        return res
    }

    override fun getSectName(): String? = null
}
