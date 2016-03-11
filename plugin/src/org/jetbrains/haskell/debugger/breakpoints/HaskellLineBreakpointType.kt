package org.jetbrains.haskell.debugger.breakpoints

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.util.Processor
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel
import org.jetbrains.haskell.debugger.HaskellDebugProcess
import org.jetbrains.haskell.debugger.HaskellDebuggerEditorsProvider
import org.jetbrains.haskell.debugger.config.DebuggerType
import org.jetbrains.haskell.debugger.config.HaskellDebugSettings
import org.jetbrains.haskell.debugger.utils.UIUtils
import org.jetbrains.haskell.fileType.HaskellFileType

class HaskellLineBreakpointType():
        XLineBreakpointTypeBase (HaskellLineBreakpointType.ID, HaskellLineBreakpointType.TITLE, HaskellDebuggerEditorsProvider())
{
    companion object {
        val ID: String = "haskell-line-breakpoint"
        private val TITLE: String = "Haskell breakpoints"
    }

    private var selectBreakpointPanel: XBreakpointCustomPropertiesPanel<XLineBreakpoint<XBreakpointProperties<out Any?>>>? = null

    /**
     * Checks if specified line with number {@code lineNumber} can be used for setting breakpoint on it
     */
    override fun canPutAt(file: VirtualFile, lineNumber: Int, project: Project): Boolean {
        if(file.fileType == HaskellFileType.INSTANCE) {
            val canStopAtLine = checkLineInSourceFile(file, lineNumber, project)
            if(canStopAtLine) {
                return debuggerIsNotBusy(project)
            }
        }
        return false
    }

    /**
     * Creates panel in breakpoint's context menu with list of available breakpoints to set on the line
     */
    override fun createCustomPropertiesPanel(): XBreakpointCustomPropertiesPanel<XLineBreakpoint<XBreakpointProperties<out Any?>>>? {
        if(HaskellDebugSettings.getInstance().state.debuggerType == DebuggerType.REMOTE) {
            if(selectBreakpointPanel == null) {
                selectBreakpointPanel = SelectBreakPropertiesPanel()
            }
            return selectBreakpointPanel
        }
        return null
    }

    private fun checkLineInSourceFile(file: VirtualFile, lineNumber: Int, project: Project): Boolean {
        var canStopAtLine: Boolean = false
        val currentDoc: Document? = FileDocumentManager.getInstance().getDocument(file)
        if (currentDoc != null) {
            XDebuggerUtil.getInstance()?.iterateLine(project, currentDoc, lineNumber, Processor<com.intellij.psi.PsiElement> { psiElement ->
                if (psiElement is PsiWhiteSpace || psiElement is PsiComment) {
                    return@Processor true
                }
                canStopAtLine = true
                false
            })
        }
        return canStopAtLine
    }

    private fun debuggerIsNotBusy(project: Project): Boolean {
        val debugProcess = XDebuggerManager.getInstance(project)?.currentSession?.debugProcess as? HaskellDebugProcess
        if (debugProcess != null && !debugProcess.isReadyForNextCommand()) {
            UIUtils.notifyCommandInProgress()
            return false
        }
        return true
    }
}