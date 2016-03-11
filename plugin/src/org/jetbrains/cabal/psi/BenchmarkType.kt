package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.parser.BENCH_TYPE_VALS

class BenchmarkType(node: ASTNode) : PropertyValue(node), RangedValue {
    override fun getAvailableValues(): List<String> {
        return BENCH_TYPE_VALS
    }
}
