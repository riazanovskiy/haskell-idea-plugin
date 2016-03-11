package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode

class FullVersionConstraint(node: ASTNode) : PropertyValue(node) {

    fun getBaseName() : String = firstChild!!.text!!

    fun getConstraint() : ComplexVersionConstraint?
            = (children.firstOrNull { it is ComplexVersionConstraint }) as ComplexVersionConstraint?
}