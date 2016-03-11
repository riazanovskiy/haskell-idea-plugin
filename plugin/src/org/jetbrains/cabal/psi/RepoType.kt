package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.parser.REPO_TYPE_VALS

class RepoType(node: ASTNode) : PropertyValue(node), RangedValue {
    override fun getAvailableValues(): List<String> {
        return REPO_TYPE_VALS
    }
}
