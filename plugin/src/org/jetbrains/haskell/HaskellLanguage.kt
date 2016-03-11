package org.jetbrains.haskell

import com.intellij.lang.Language

class HaskellLanguage : Language("Haskell", "text/haskell") {

    companion object {
        val INSTANCE: HaskellLanguage = HaskellLanguage()
    }
}