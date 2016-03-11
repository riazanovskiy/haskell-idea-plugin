package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage
import org.jetbrains.cabal.parser.BENCHMARK_FIELDS
import org.jetbrains.cabal.parser.IF_ELSE
import java.util.*

class Benchmark(node: ASTNode) : BuildSection(node) {

    override fun getAvailableFieldNames(): List<String> {
        var res = ArrayList<String>()
        res.addAll(BENCHMARK_FIELDS.keys)
        res.addAll(IF_ELSE)
        return res
    }

    override fun check(): List<ErrorMessage> {
        val res = ArrayList<ErrorMessage>()

        val typeField   = getField(TypeField::class.java)
        val mainIsField = getField(MainFileField::class.java)

        if (typeField == null) {
            res.add(ErrorMessage(getSectTypeNode(), "type field is required", "error"))
        }
        if ((typeField?.getValue()?.text == "exitcode-stdio-1.0") && (mainIsField == null)) {
            res.add(ErrorMessage(getSectTypeNode(), "main-is field is required", "error"))
        }
        return res
    }

    fun getBenchmarkName(): String {
        val res = getSectName() ?: throw IllegalStateException()
        return res
    }
}
