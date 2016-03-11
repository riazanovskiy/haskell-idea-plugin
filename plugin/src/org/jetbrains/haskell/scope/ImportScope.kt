package org.jetbrains.haskell.scope

import org.jetbrains.haskell.psi.*

class ImportScope(val import : Import) {


    fun <A : Declaration> filterDeclarations(list : List<A>) : List<A> {
        val moduleExports = import.getModuleExports()
        if (moduleExports != null) {
            if (import.hasHiding()) {

            } else {

            }
        }
        return list
    }

    fun getSignatureDeclarations() : List<SignatureDeclaration> {
        val module = import.findModule()
        if (module != null) {
            return filterDeclarations(module.getSignatureDeclarationsList())
        }
        return listOf()
    }

    fun getDataDeclarations() : List<DataDeclaration> {
        val module = import.findModule()
        if (module != null) {
            return filterDeclarations(module.getDataDeclarationList())
        }
        return listOf()
    }

    fun getTypeSynonym() : List<TypeSynonym> {
        val module = import.findModule()
        if (module != null) {
            return filterDeclarations(module.getTypeSynonymList())
        }
        return listOf()
    }

}