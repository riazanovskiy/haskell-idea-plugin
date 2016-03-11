package org.jetbrains.grammar.analysis

import org.jetbrains.grammar.HaskellParser

/**
 * Created by atsky on 11/20/14.
 */
fun main(args: Array<String>) {
    val grammar = HaskellParser(null).getGrammar()

    for ((name, rule) in grammar) {
        rule.makeAnalysis(grammar);
            println("rule $name {")
            println("  can be empty: " + rule.canBeEmpty)
            println("  first: " + rule.first)
            println("}")

    }

}

/*
fun hasConflict(rule: Rule, variants: List<Variant>, index: Int): Boolean {
    var conflict = false;
    val terms = HashMap<Term, MutableList<Variant>>()
    for (variant in variants) {
        if (variant.terms.size == 0) {
            continue
        }
        val term = variant.terms[index]
        if (!terms.containsKey(term)) {
            terms[term] = ArrayList<Variant>()
        }
        terms[term].add(variant)
    }
    val allFirst = HashSet<List<IElementType>>()

    for ((term, variants) in terms) {
        val firsts = HashSet<List<IElementType>>()
        for (variant in variants) {
            firsts.addAll(variant.first!!)
        }

        for (f in firsts) {
            if (allFirst.contains(f)) {
                println("conflict: ${rule.name} - '${f}'")
                conflict = true
            }
        }
        allFirst.addAll(firsts)
    }

    return conflict
}

fun hasConflicts(rule: Rule): Boolean {
    return hasConflict(rule, rule.variants, 0)
}


*/