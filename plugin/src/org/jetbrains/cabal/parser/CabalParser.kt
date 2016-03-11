package org.jetbrains.cabal.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.jetbrains.haskell.parser.rules.BaseParser

class CabalParser(root: IElementType, builder: PsiBuilder) : BaseParser(root, builder) {

    fun parse(): ASTNode = parseInternal(root)

    fun canParse(parse: () -> Boolean): Boolean {
        val marker = builder.mark()
        val res = parse()
        marker.rollbackTo()
        return res
    }

    fun indentSize(str: String): Int {
        val indexOf = str.lastIndexOf('\n')
        return str.length - indexOf - 1
    }

    fun nextLevel() : Int? {                                  //there can never be two NEW_LINE_INDENT's next to each other
        if ((!builder.eof()) && (builder.tokenType == TokenType.NEW_LINE_INDENT)) {
            return indentSize(builder.tokenText!!)
        }
        return null
    }

    fun isLastOnThisLine() : Boolean = builder.eof() || (builder.tokenType == TokenType.NEW_LINE_INDENT)

    fun skipNewLineBiggerLevel(prevLevel: Int) {
        val nextIndent = nextLevel()
        if ((nextIndent != null) && (nextIndent > prevLevel))
            builder.advanceLexer()
    }

    fun isLastBiggerLevel(level: Int) : Boolean {
        val nextIndent = nextLevel()
        if (builder.eof() || ((nextIndent != null) && (nextIndent <= level))) {
            return true
        }
        return false
    }

    fun skipAllBiggerLevelTill(level: Int, parseSeparator: () -> Boolean) {
        while (!builder.eof()) {
            if (isLastBiggerLevel(level)) {
                break
            }
            if (canParse({ skipNewLineBiggerLevel(level); parseSeparator() })) {
                break
            }
            builder.advanceLexer();
        }
    }

    fun skipFreeLineTill(parseSeparator: () -> Boolean) {
        while (!isLastOnThisLine() && !canParse(parseSeparator)) {
            builder.advanceLexer();
        }
    }

    fun parsePropertyKey(propName : String?) = start(CabalTokelTypes.PROPERTY_KEY) {
        if (propName == null) token(CabalTokelTypes.ID) else matchesIgnoreCase(CabalTokelTypes.ID, propName)
    }

    fun parseBool() = matchesIgnoreCase(CabalTokelTypes.ID, "true") || matchesIgnoreCase(CabalTokelTypes.ID, "false")

    fun parseVersion() = token(CabalTokelTypes.NUMBER) || token(CabalTokelTypes.ID)

    fun parseSimpleVersionConstraint() = start(CabalTokelTypes.VERSION_CONSTRAINT) {
        if (token(CabalTokelTypes.COMPARATOR)) {
            parseVersion()
        }
        else {
            matches(CabalTokelTypes.ID, "-any")
        }
    }

    fun parseFreeLine(elemType: IElementType) = start(elemType) {
        var isEmpty = true
        while (!isLastOnThisLine()) {
            builder.advanceLexer()
            isEmpty = false
        }
        !isEmpty
    }

    fun parseInvalidLine() = parseFreeLine(CabalTokelTypes.INVALID_VALUE) || start(CabalTokelTypes.INVALID_VALUE, { true })

    fun parseAsInvalid(parseBody: () -> Boolean) = start(CabalTokelTypes.INVALID_VALUE, { parseBody() })

    fun parseFreeForm(prevLevel: Int) = start(CabalTokelTypes.FREE_FORM) {
        skipAllBiggerLevelTill(prevLevel, parseSeparator = { false }); true
    }

    fun parseInvalidTillSeparator(prevLevel: Int, parseSeparator: () -> Boolean, onOneLine: Boolean) = start(CabalTokelTypes.INVALID_VALUE) {
        if (!onOneLine) {
            skipAllBiggerLevelTill(prevLevel, parseSeparator)
        }
        else {
            skipFreeLineTill(parseSeparator)
        }
        true
    }

    fun parseIdValue(elemType: IElementType) = start(elemType, { token(CabalTokelTypes.ID) })

    fun parseTokenValue(elemType: IElementType) = start(elemType) {

        fun nextTokenIsValid() = !isLastOnThisLine()
                              && (builder.tokenType != CabalTokelTypes.COMMA)
                              && (builder.tokenType != CabalTokelTypes.TAB)

        fun emptySpaceBeforeNext()
                = ((builder.rawLookup(-1) == TokenType.WHITE_SPACE) || (builder.rawLookup(-1) == CabalTokelTypes.COMMENT))

        var isEmpty = true
        while (nextTokenIsValid()) {
            builder.advanceLexer()
            isEmpty = false
            if (!isLastOnThisLine() && emptySpaceBeforeNext()) break
        }
        !isEmpty
    }

    fun parsePath() = parseTokenValue(CabalTokelTypes.PATH)

    fun parseVersionValue() = start(CabalTokelTypes.VERSION_VALUE, { parseVersion() })

    fun parseTillSeparatorOrPrevLevel(prevLevel: Int, parseValue : () -> Boolean, parseSeparator : () -> Boolean, onOneLine: Boolean, separatorIsOptional: Boolean) : Boolean {
        if (!onOneLine) skipNewLineBiggerLevel(prevLevel)                                                          // returns false if there is nothing to parse
        val mark = builder.mark()
        var valueParsed = parseValue()
        if (!onOneLine) skipNewLineBiggerLevel(prevLevel)
        if (valueParsed && (isLastBiggerLevel(prevLevel) || canParse({ parseSeparator() }) || separatorIsOptional)) {
            mark.drop()
        }
        else {
            mark.rollbackTo()
            parseInvalidTillSeparator(prevLevel, parseSeparator, onOneLine)
            if (!onOneLine) skipNewLineBiggerLevel(prevLevel)
        }
        return true
    }

    fun parseTillEndValueList(prevLevel: Int, parseValue : () -> Boolean, parseSeparator : () -> Boolean, separatorIsOptional: Boolean, onOneLine: Boolean) : Boolean {
        do {
            parseTillSeparatorOrPrevLevel(prevLevel, parseValue, parseSeparator, onOneLine, separatorIsOptional)
        } while ((!builder.eof()) && !isLastBiggerLevel(prevLevel) && (parseSeparator() || separatorIsOptional))
        return true
    }

    fun parseTillValidValueList(prevLevel: Int, parseValue : () -> Boolean, parseSeparator : () -> Boolean, onOneLine: Boolean) : Boolean {
        var mark: Marker? = builder.mark()
        var nonEmpty = false
        do {
            if (!onOneLine) skipNewLineBiggerLevel(prevLevel);
            if (parseValue()) {
                mark?.drop()
                nonEmpty = true
            }
            else break
            mark = builder.mark()
            if (!onOneLine) skipNewLineBiggerLevel(prevLevel);
        } while ((!builder.eof()) && parseSeparator())
        mark?.rollbackTo()
        return nonEmpty
    }

    fun parseCommonCommaList(prevLevel: Int, parseBody: () -> Boolean) = parseTillEndValueList(
            prevLevel,
            parseBody,
            { token(CabalTokelTypes.COMMA) },
            separatorIsOptional = true,
            onOneLine = false
    )

    fun parseTokenList(prevLevel: Int)  = parseCommonCommaList(prevLevel, { parseTokenValue(CabalTokelTypes.TOKEN) })

    fun parseIdList(prevLevel: Int) = parseCommonCommaList(prevLevel, { parseIdValue(CabalTokelTypes.IDENTIFIER) })

    fun parseOptionList(prevLevel: Int) = parseCommonCommaList(prevLevel, { parseTokenValue(CabalTokelTypes.OPTION) })

    fun parsePathList(prevLevel: Int)   = parseCommonCommaList(prevLevel, { parsePath() })

    fun parseLanguageList(prevLevel: Int) = parseCommonCommaList(prevLevel, { parseIdValue(CabalTokelTypes.LANGUAGE) })

    fun parseComplexVersionConstraint(prevLevel : Int, onOneLine: Boolean = false) = start(CabalTokelTypes.COMPLEX_CONSTRAINT) {
        parseTillValidValueList(prevLevel, { parseSimpleVersionConstraint() }, { token(CabalTokelTypes.LOGIC) }, onOneLine)
    }

    fun parseFullVersionConstraint(prevLevel: Int, tokenType: IElementType, onOneLine: Boolean = false) = start(CabalTokelTypes.FULL_CONSTRAINT) {
        parseIdValue(tokenType)
                && (parseComplexVersionConstraint(prevLevel, onOneLine) || true)
    }

    fun parseSimpleCondition(prevLevel: Int) = start(CabalTokelTypes.SIMPLE_CONDITION) {
        val testName = builder.tokenText
        if (parseBool()) {
            true
        }
        else if (token(CabalTokelTypes.ID) && token(CabalTokelTypes.OPEN_PAREN)) {
            var res: Boolean
            when (testName) {
                "impl" -> res = parseFullVersionConstraint(prevLevel, CabalTokelTypes.COMPILER, true)
                "flag" -> res = parseIdValue(CabalTokelTypes.NAME)
                else   -> res = parseIdValue(CabalTokelTypes.IDENTIFIER)
            }
            res && token(CabalTokelTypes.CLOSE_PAREN)
        }
        else false
    }

    fun parseInvalidConditionPart() = start(CabalTokelTypes.INVALID_CONDITION_PART) {
        while (!builder.eof() && (builder.tokenType != CabalTokelTypes.LOGIC)
                                        && (builder.tokenType != CabalTokelTypes.CLOSE_PAREN)
                                        && (builder.tokenType != TokenType.NEW_LINE_INDENT)) {
            if ((builder.tokenText == "flag") && token(CabalTokelTypes.ID) && token(CabalTokelTypes.OPEN_PAREN)) {
                parseIdValue(CabalTokelTypes.NAME)
            }
            else builder.advanceLexer()
        }
        true
    }

    fun parseConditionPart(prevLevel: Int): Boolean = start(CabalTokelTypes.CONDITION_PART) {
        if (token(CabalTokelTypes.NEGATION)) {
            parseConditionPart(prevLevel)
        }
        else if (token(CabalTokelTypes.OPEN_PAREN)) {
            parseCondition(prevLevel) && (token(CabalTokelTypes.CLOSE_PAREN) || true)
        }
        else {
            parseSimpleCondition(prevLevel)
        }
    }

    fun parseCondition(prevLevel: Int) = parseTillValidValueList(
            prevLevel,
            { parseConditionPart(prevLevel) || parseInvalidConditionPart() },
            { token(CabalTokelTypes.LOGIC) || (parseInvalidConditionPart() && token(CabalTokelTypes.LOGIC)) },
            onOneLine = true
    )

    fun parseFullCondition(level: Int) = start(CabalTokelTypes.FULL_CONDITION, { parseCondition(level) })

    fun parseConstraintList(prevLevel: Int,
                            tokenType: IElementType = CabalTokelTypes.IDENTIFIER,
                            separatorIsOptional: Boolean = false)
            = parseTillEndValueList(prevLevel,
                                    { parseFullVersionConstraint(prevLevel, tokenType) },
                                    { token(CabalTokelTypes.COMMA) },
                                    separatorIsOptional,
                                    onOneLine = false
            )

    fun parseCompilerList(prevLevel: Int) = parseConstraintList(prevLevel, CabalTokelTypes.COMPILER, true)

    fun parseField(level: Int, key : String?, elemType: IElementType, parseValue : CabalParser.(Int) -> Boolean) = start(elemType) {
        if (parsePropertyKey(key) && token(CabalTokelTypes.COLON)) {
            skipNewLineBiggerLevel(level)

            (parseValue(level) && isLastBiggerLevel(level))
                    || parseInvalidTillSeparator(level, parseSeparator = { false }, onOneLine = false)
        }
        else false
    }

    fun parseInvalidField(level: Int) = start(CabalTokelTypes.INVALID_FIELD) {
        if (parseIdValue(CabalTokelTypes.NAME)) {
            token(CabalTokelTypes.COLON)
            skipAllBiggerLevelTill(level, parseSeparator = { false })
            true
        }
        else false
    }

    fun parseFieldFrom(level: Int, fields: Map<String, Pair<IElementType, CabalParser.(Int) -> Boolean>>) : Boolean {
        for (key in fields.keys) {
            if (parseField(level, key, fields[key]!!.first, fields[key]!!.second)) return true
        }
        return false
    }

    fun parseProperties(prevLevel: Int, parseFields: CabalParser.(Int) -> Boolean, canContainIf: Boolean): Boolean {

        fun parseSomeField(level: Int) = parseFields(level)
                                      || (canContainIf && parseIfElse(level, parseFields))
                                      || parseInvalidField(level)
                                      || parseInvalidLine()

        var currentLevel : Int? = null
        while (!builder.eof()) {
            val level = nextLevel() ?: return false
            if (((currentLevel == null) || (level != currentLevel)) && (level <= prevLevel)) {
                return true                                                                       //sections without any field is allowed
            }
            else if ((currentLevel == null) && (level > prevLevel)) {
                currentLevel = level
            }
            skipNewLineBiggerLevel(prevLevel)
            if ((currentLevel != null) && (level != currentLevel) && (level > prevLevel)) {
                parseInvalidTillSeparator(currentLevel, parseSeparator = { false }, onOneLine = false)
            }
            else {
                parseSomeField(level)
            }
        }
        return true
    }

    fun parseSectionType(name: String) = start(CabalTokelTypes.SECTION_TYPE) {
        matchesIgnoreCase(CabalTokelTypes.ID, name)
    }

    fun parseSectionName() = start(CabalTokelTypes.NAME) {
        skipFreeLineTill({ false });
        true
    }

    fun parseRepoKinds() = (parseIdValue(CabalTokelTypes.REPO_KIND) && parseIdValue(CabalTokelTypes.REPO_KIND)) || true

    fun parseExactSection(level: Int, key: String, parseAfterInfo: CabalParser.(Int) -> Boolean, parseBody: (Int) -> Boolean)
                                                                                                     = start(SECTION_TYPES[key]!!) {
        if (parseSectionType(key)) {
            (parseAfterInfo(level) && isLastOnThisLine()) || parseInvalidLine()
            parseProperties(level, { parseBody(it) }, canContainIf = (key in BUILD_INFO_SECTIONS) || (key in IF_ELSE))
        }
        else false
    }

    fun parseTopSection(level: Int, key: String) = parseExactSection(level, key, SECTIONS[key]!!.first) {
        parseFieldFrom(it, SECTIONS[key]!!.second!!)
    }

    fun parseIfOrElse(level: Int, key: String, parseFields: CabalParser.(Int) -> Boolean) = parseExactSection(level, key, SECTIONS[key]!!.first) {
        parseFields(it)
    }

    fun parseIfElse(level: Int, parseFields: CabalParser.(Int) -> Boolean): Boolean {
        if (parseIfOrElse(level, "if", parseFields)) {
            if (nextLevel() == level) {
                val marker = builder.mark()
                skipNewLineBiggerLevel(level - 1)
                if (parseIfOrElse(level, "else", parseFields)) {
                    marker.drop()
                }
                else {
                    marker.rollbackTo()
                }
            }
            return true
        }
        return false
    }

    fun parseTopSection(level: Int): Boolean {
        for (key in TOP_SECTION_NAMES) {
            if (parseTopSection(level, key)) return true
        }
        return false
    }

    fun parseTopLevelField(firstIndent: Int) = parseFieldFrom(firstIndent, PKG_DESCR_FIELDS)

    fun parseInternal(root: IElementType): ASTNode {

        fun parseSomeField(level: Int) = parseTopLevelField(level)
                                      || parseTopSection(level)
                                      || parseInvalidField(level)

        val rootMarker = mark()
        val firstIndent = nextLevel() ?: 0
        while (!builder.eof()) {
            val nextIndent = nextLevel()
            if ((nextIndent == null) || (nextIndent == firstIndent)) {
                skipNewLineBiggerLevel(firstIndent - 1)
                if (!builder.eof()) (parseSomeField(firstIndent) || parseInvalidLine())
            }
            else {
                skipNewLineBiggerLevel(- 1)
                if (!builder.eof()) parseInvalidLine()
            }

        }
        rootMarker.done(root)
        return builder.treeBuilt
    }
}