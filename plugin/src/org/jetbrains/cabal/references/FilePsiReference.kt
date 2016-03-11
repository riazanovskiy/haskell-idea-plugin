package org.jetbrains.cabal.references

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.cabal.psi.Path

class FilePsiReference<T: Path>(element: T, resolveTo: PsiElement): PsiReferenceBase.Immediate<T>(element, element.getDefaultTextRange(), resolveTo) {

}
