package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.parser.FLAG_FIELDS
import java.util.*

class Flag(node: ASTNode) : Section(node) {

    override fun getAvailableFieldNames(): List<String> {
        var res = ArrayList<String>()
        res.addAll(FLAG_FIELDS.keys)
        return res
    }

    fun getFlagName(): String {
        val res = getSectName()?.toLowerCase() ?: throw IllegalStateException()
        return res
    }
}
