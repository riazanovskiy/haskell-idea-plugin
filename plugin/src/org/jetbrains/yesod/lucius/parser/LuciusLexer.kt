package org.jetbrains.yesod.lucius.parser

/**
 * @author Leyla H
 */

import com.intellij.lexer.FlexAdapter

import java.io.Reader

public class LuciusLexer : FlexAdapter(_LuciusLexer(null as Reader?))