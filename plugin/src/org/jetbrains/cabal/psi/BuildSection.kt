package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode


open class BuildSection(node: ASTNode): Section(node) {

    private fun <F : MultiValueField, V : PropertyValue> getFieldsValues(fieldT: Class<F>, valueT: Class<V>): List<V>
            = getFields(fieldT).flatMap { it.getValues(valueT) }

    fun getHsSourceDirs(): List<Path> = getFieldsValues(HsSourceDirsField::class.java, Path::class.java)

    fun getIncludeDirs() : List<Path> = getFieldsValues(IncludeDirsField::class.java, Path::class.java)

    fun getBuildDepends(): List<FullVersionConstraint>
            = getFieldsValues(BuildDependsField::class.java, FullVersionConstraint::class.java)

}