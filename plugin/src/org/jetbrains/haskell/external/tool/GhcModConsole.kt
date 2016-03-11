package org.jetbrains.haskell.external.tool

import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.project.Project

/**
 * Created by atsky on 06/01/15.
 */
class GhcModConsole(val project: Project) : ProjectComponent {
    var editor : EditorEx? = null

    override fun getComponentName(): String = "GhcModConsole"

    override fun initComponent() {
    }

    override fun disposeComponent() {
    }

    override fun projectOpened() {
        editor = ConsoleViewUtil.setupConsoleEditor(project, false, false);
        editor!!.settings.isUseSoftWraps = true
    }

    override fun projectClosed() {
        val editorFactory = EditorFactory.getInstance()
        editorFactory.releaseEditor(editor!!)
    }

    companion object {
        fun getInstance(project : Project) =
            project.getComponent(GhcModConsole::class.java)!!

    }

    fun append(text: String, type : MessageType) {
        ApplicationManager.getApplication().invokeLater({
            val document = editor!!.document

            val msgStart = document.textLength
            document.insertString(document.textLength, text)
            val layer = HighlighterLayer.CARET_ROW + 1

            val attributes = EditorColorsManager.getInstance()
                    .globalScheme.getAttributes(type.key);

            editor?.markupModel?.addRangeHighlighter(
                    msgStart,
                    document.textLength,
                    layer,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE)

            val line = document.lineCount - 1
            editor?.scrollingModel?.scrollTo(LogicalPosition(line, 0), ScrollType.MAKE_VISIBLE)

        });
    }

    enum class MessageType(val key : TextAttributesKey) {
        INFO(ConsoleViewContentType.SYSTEM_OUTPUT_KEY),
        INPUT(ConsoleViewContentType.USER_INPUT_KEY),
        OUTPUT(ConsoleViewContentType.NORMAL_OUTPUT_KEY)
    }
}