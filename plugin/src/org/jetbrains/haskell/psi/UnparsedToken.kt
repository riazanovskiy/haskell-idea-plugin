package org.jetbrains.haskell.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * Created by atsky on 4/25/14.
 */
class UnparsedToken(node: ASTNode) : ASTWrapperPsiElement(node)