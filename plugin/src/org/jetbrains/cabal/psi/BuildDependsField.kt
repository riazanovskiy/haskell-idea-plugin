package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.CabalInterface
import org.jetbrains.cabal.highlight.ErrorMessage
import java.util.*

class BuildDependsField(node: ASTNode) : MultiValueField(node), Checkable {

    fun getPackageNames(): List<String> = getValues(FullVersionConstraint::class.java).map { it.getBaseName() }

    fun getConstraintsWithName(name: String): List<FullVersionConstraint>
            = getValues(FullVersionConstraint::class.java).filter { it.getBaseName().equals(name) }

    override fun check(): List<ErrorMessage> {
        val packageConstraints = getValues(FullVersionConstraint::class.java)
        val installedPackages  = CabalInterface(project).getInstalledPackagesList()
        var res = ArrayList<ErrorMessage>()
        for (constraint in packageConstraints) {
            val constrName = constraint.getBaseName()
            val sameConstraints = getConstraintsWithName(constraint.getBaseName())
            if (sameConstraints.size != 1) {
                sameConstraints.forEach { res.add(ErrorMessage(it, "dublicate package", "warning")) }
                continue
            }
            val installed = installedPackages.firstOrNull { it.name == constrName }
            if (installed == null) {
                res.add(ErrorMessage(constraint, "this package is not installed", "warning"))
                continue
            }
            val versionConstr = constraint.getConstraint() ?: continue
            if (!(installed.availableVersions.map({ versionConstr.satisfyConstraint(it) }).reduce { curr, next -> curr || next })) {
                res.add(ErrorMessage(versionConstr, "installed package's version does not satisfy this constraint", "warning"))
            }
        }
        return res
    }
}