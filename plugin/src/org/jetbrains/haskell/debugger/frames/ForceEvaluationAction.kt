package org.jetbrains.haskell.debugger.frames

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import org.jetbrains.haskell.debugger.HaskellDebugProcess

/**
 * Determines action performed when user select 'Force evaluation' in context menu of variable in current frame.
 * This action is called by IDEA, so class is registered in plugin.xml
 *
 * @author Habibullin Marat
 */
class ForceEvaluationAction(): XDebuggerTreeActionBase() {
    override fun perform(node: XValueNodeImpl?, nodeName: String, actionEvent: AnActionEvent?) {
        if(node == null || actionEvent == null) {
            return
        }
        val debugProcess = tryGetDebugProcess(actionEvent) ?: return
        forceSetValue(node, debugProcess)
    }

    private fun tryGetDebugProcess(actionEvent: AnActionEvent): HaskellDebugProcess? {
        val project = actionEvent.project ?: return null
        val debuggerManager = XDebuggerManager.getInstance(project) ?: return null
        val session = debuggerManager.currentSession ?: return null
        return session.debugProcess as HaskellDebugProcess
    }

    private fun forceSetValue(node: XValueNodeImpl, debugProcess: HaskellDebugProcess) {
        val hsDebugValue = node.valueContainer as HsDebugValue
        debugProcess.forceSetValue(hsDebugValue.binding)
        if(hsDebugValue.binding.value != null) {
            node.setPresentation(null, XRegularValuePresentation(hsDebugValue.binding.value as String, hsDebugValue.binding.typeName), false)
        }
    }
}