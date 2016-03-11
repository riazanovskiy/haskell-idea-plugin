package org.jetbrains.grammar

import org.jetbrains.grammar.dumb.LazyLLParser
import org.jetbrains.grammar.dumb.Rule
import org.jetbrains.haskell.parser.CachedTokens
import org.jetbrains.haskell.parser.getCachedTokens
import org.jetbrains.haskell.parser.lexer.HaskellLexer
import org.jetbrains.haskell.parser.newLexerState
import java.io.*

/**
 * Created by atsky on 15/11/14.
 */
fun main(args : Array<String>) {
    val path = File("./data")
    val filter = FilenameFilter { dir, name -> name.endsWith("Test.hs") }
    for (file in path.listFiles(filter)) {
        val name = file.name
        parseFile(file, File(path, name.substring(0, name.length - 3) + "_tree.txt"))
    }
}


fun parseFile(inFile : File, outFile : File) {
    val data = readData(inFile)

    val lexer = HaskellLexer()
    lexer.start(data)

    val stream = PrintStream(outFile)
    val cachedTokens = getCachedTokens(lexer, stream)

    printTokens(cachedTokens)

    val grammar = HaskellParser(null).getGrammar()

    HaskellParser(null).findFirst(grammar)

    //evaluateManyTimes(cachedTokens, grammar)

    val parser = LazyLLParser(grammar, cachedTokens)
    parser.writeLog = true;
    val tree = parser.parse()
    stream.println(tree?.prettyPrint(0))
    stream.close()
}

private fun printTokens(cachedTokens: CachedTokens) {
    var state = newLexerState(cachedTokens)
    while (state.getToken() != null) {
        println(state.getToken());
        state = state.next()
    }
}

private fun evaluateManyTimes(cachedTokens: CachedTokens, grammar: MutableMap<String, Rule>) {
    val start = System.currentTimeMillis()
    for (i in 1..200) {
        val parser = LazyLLParser(grammar, cachedTokens)
        parser.parse()
    }
    val time = System.currentTimeMillis() - start
    println("time = $time")
}

fun readData(file: File): String {
    val reader = BufferedReader(FileReader(file))
    return reader.readText()
}