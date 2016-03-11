package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage
import org.jetbrains.cabal.parser.IF_ELSE
import org.jetbrains.cabal.parser.TEST_SUITE_FIELDS
import java.util.*

/**
 * @author Evgeny.Kurbatsky
 */
class TestSuite(node: ASTNode) : BuildSection(node) {

    override fun getAvailableFieldNames(): List<String> {
        var res = ArrayList<String>()
        res.addAll(TEST_SUITE_FIELDS.keys)
        res.addAll(IF_ELSE)
        return res
    }

    override fun check(): List<ErrorMessage> {
        val res = ArrayList<ErrorMessage>()

        val typeField    = getField(TypeField::class.java)
        val mainIsField  = getField(MainFileField::class.java)
        val testModField = getField(TestModuleField::class.java)

        if (typeField == null) {
            res.add(ErrorMessage(getSectTypeNode(), "type field is required", "error"))
        }
        if (typeField?.getValue()?.text == "exitcode-stdio-1.0") {
            if (mainIsField  == null) res.add(ErrorMessage(getSectTypeNode(), "main-is field is required with such test suite type", "error"))
            if (testModField != null) res.add(ErrorMessage(testModField.getKeyNode(), "test-module field is disallowed with such test suite type", "error"))
        }
        if (typeField?.getValue()?.text == "detailed-1.0") {
            if (mainIsField  != null) res.add(ErrorMessage(mainIsField.getKeyNode(), "main-is field is disallowed with such test suite type", "error"))
            if (testModField == null) res.add(ErrorMessage(getSectTypeNode(), "test-module field is required with such test suite type", "error"))
        }
        return res
    }

    fun getTestSuiteName(): String {
        val res = getSectName() ?: throw IllegalStateException()
        return res
    }
}