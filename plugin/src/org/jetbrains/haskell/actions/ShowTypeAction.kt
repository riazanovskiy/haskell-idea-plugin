package org.jetbrains.haskell.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.haskell.external.GhcModi
import org.jetbrains.haskell.util.LineColPosition
import org.jetbrains.haskell.util.getRelativePath
import java.util.regex.Pattern

/**
 * Created by atsky on 5/30/14.
 */
class ShowTypeAction : AnAction() {

    data class TypeInfo(
        val startLine: Int,
        val startCol : Int,
        val endLine: Int,
        val endCol : Int,
        val aType : String
    )

    fun typeInfoFromString(str : String) : TypeInfo? {
        val matcher = Pattern.compile("(\\d+) (\\d+) (\\d+) (\\d+) \"(.*)\"").matcher(str)
        if (matcher.matches()) {
            return TypeInfo(
                    Integer.parseInt(matcher.group(1)!!),
                    Integer.parseInt(matcher.group(2)!!),
                    Integer.parseInt(matcher.group(3)!!),
                    Integer.parseInt(matcher.group(4)!!),
                    matcher.group(5)!!)
        }
        return null
    }

    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            return
        }
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        if (editor == null || psiFile == null) {
            return
        }

        val offset = editor.caretModel.offset;
        val selectionStartOffset = editor.selectionModel.selectionStart
        val selectionEndOffset = editor.selectionModel.selectionEnd
        val range = if (selectionStartOffset != selectionEndOffset) {
            Pair(selectionStartOffset, selectionEndOffset)
        } else {
            val element = psiFile.findElementAt(offset)

            val textRange = element?.textRange ?: return
            Pair(textRange.startOffset, textRange.endOffset)
        }

        ApplicationManager.getApplication()!!.invokeAndWait({ FileDocumentManager.getInstance().saveAllDocuments() }, ModalityState.any())

        val start = LineColPosition.fromOffset(psiFile, range.first)!!
        val end = LineColPosition.fromOffset(psiFile, range.second)!!

        val lineColPosition = LineColPosition.fromOffset(psiFile, range.first)!!

        val ghcModi = psiFile.project.getComponent(GhcModi::class.java)!!
        val basePath = psiFile.project.basePath!!
        val relativePath = getRelativePath(basePath, psiFile.virtualFile!!.path)

        val line = lineColPosition.myLine
        val column = lineColPosition.myColumn
        val cmd = "type $relativePath $line $column"

        val list = ghcModi.runCommand(cmd)

        val result = list.map { typeInfoFromString(it) }.filterNotNull()
        val typeInfo = result.firstOrNull {
            it.startLine == start.myLine &&
            it.startCol == start.myColumn
        }
        if (typeInfo != null) {
            HintManager.getInstance()!!.showInformationHint(editor, typeInfo.aType)
        } else {
            HintManager.getInstance()!!.showInformationHint(editor, "can't calculate type")
        }
    }


}