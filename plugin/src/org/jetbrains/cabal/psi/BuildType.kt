package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.parser.BUILD_TYPE_VALS

class BuildType(node: ASTNode) : PropertyValue(node), RangedValue  {

    override fun getAvailableValues(): List<String> {
        return BUILD_TYPE_VALS
    }
}
