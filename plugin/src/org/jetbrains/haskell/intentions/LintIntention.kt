package org.jetbrains.haskell.intentions


import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.haskell.fileType.HaskellFile
import org.jetbrains.haskell.psi.util.HaskellElementFactory

class LintIntention(val title: String, val originalElement: PsiElement?, val suggested: String) : IntentionAction {

    override fun getFamilyName() = "Apply lint"
    override fun getText() = title
    override fun startInWriteAction() = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) =
            file is HaskellFile

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        originalElement!!.replace(HaskellElementFactory.createExpressionFromText(project, suggested))
    }
}