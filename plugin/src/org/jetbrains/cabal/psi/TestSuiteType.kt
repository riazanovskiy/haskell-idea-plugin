package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.parser.TS_TYPE_VALS

class TestSuiteType(node: ASTNode) : PropertyValue(node), RangedValue  {
    override fun getAvailableValues(): List<String> {
        return TS_TYPE_VALS
    }
}
