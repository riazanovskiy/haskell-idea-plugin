package org.jetbrains.haskell.debugger

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import org.jetbrains.haskell.fileType.HaskellFileType

class HaskellDebuggerEditorsProvider : XDebuggerEditorsProvider() {

    override fun createDocument(project: Project,
                                text: String,
                                sourcePosition: XSourcePosition?,
                                mode: EvaluationMode): Document {
        if(sourcePosition != null) {
            val hsPsiFile = PsiFileFactory.getInstance(project)!!.createFileFromText(sourcePosition.file.name,
                    HaskellFileType.INSTANCE,
                    text)
            val hsDocument = PsiDocumentManager.getInstance(project)!!.getDocument(hsPsiFile)
            if(hsDocument != null) {
                return hsDocument
            }
        }
        return EditorFactory.getInstance()!!.createDocument(text)
    }

    override fun getFileType(): FileType = HaskellFileType.INSTANCE
}