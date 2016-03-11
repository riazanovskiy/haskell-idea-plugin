package org.jetbrains.haskell.parser

import com.intellij.psi.tree.IElementType
import org.jetbrains.haskell.HaskellLanguage

class HaskellTokenType(debugName: String) : IElementType(debugName, HaskellLanguage.INSTANCE) {
    val myName: String = debugName

}