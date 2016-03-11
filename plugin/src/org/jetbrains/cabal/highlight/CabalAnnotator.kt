package org.jetbrains.cabal.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.cabal.psi.Checkable
import org.jetbrains.cabal.psi.PropertyKey
import org.jetbrains.cabal.psi.SectionType
import org.jetbrains.cabal.psi.SingleValueField

class CabalAnnotator() : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder): Unit {
        fun keyword(e : PsiElement) {
            holder.createInfoAnnotation(e, null)?.textAttributes = CabalHighlighter.CABAL_PROPERTY
        }

        fun handle(errMsg: ErrorMessage?) {
            if (errMsg == null) return
            if (errMsg.isAfterNode) {
                val endOffset = errMsg.place.textRange!!.endOffset
                val place = TextRange(endOffset - 1, endOffset)
                if (errMsg.severity == "error")   holder.createErrorAnnotation(place, errMsg.text)
                if (errMsg.severity == "warning") holder.createWarningAnnotation(place, errMsg.text)
            }
            else {
                if (errMsg.severity == "error") holder.createErrorAnnotation(errMsg.place, errMsg.text)
                if (errMsg.severity == "warning") holder.createWarningAnnotation(errMsg.place, errMsg.text)
            }
        }

        if (element is Checkable) {
            (element.check()).forEach { handle(it) }
        }
        if (element is SingleValueField) {
            handle(element.checkUniqueness())
        }
        if ((element is PropertyKey) || (element is SectionType)) {
            keyword(element)
        }
    }
}
