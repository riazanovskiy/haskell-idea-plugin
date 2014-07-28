package org.jetbrains.haskell.debugger.frames

import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XCompositeNode
import org.jetbrains.debugger.VariableView
import org.jetbrains.debugger.VariableContextBase
import org.jetbrains.debugger.EvaluateContext
import org.jetbrains.debugger.DebuggerViewSupport
import org.jetbrains.debugger.values.PrimitiveValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.xdebugger.frame.XValueChildrenList
import org.jetbrains.debugger.VariableImpl
import org.jetbrains.debugger.values.ValueType
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import org.jetbrains.haskell.debugger.utils.HaskellUtils
import org.jetbrains.haskell.debugger.HaskellDebugProcess
import org.jetbrains.haskell.debugger.parser.LocalBinding
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Condition
import java.util.ArrayList
import org.jetbrains.haskell.debugger.parser.HsTopStackFrameInfo
import org.jetbrains.haskell.debugger.parser.HsFilePosition
import com.intellij.ui.ColoredTextContainer
import com.intellij.icons.AllIcons
import com.intellij.xdebugger.XDebuggerBundle

public abstract class HsStackFrame(protected val debugProcess: HaskellDebugProcess,
                                   val filePosition: HsFilePosition?) : XStackFrame() {
    class object {
        private val STACK_FRAME_EQUALITY_OBJECT = Object()
    }
    override fun getEqualityObject(): Any? = STACK_FRAME_EQUALITY_OBJECT

    private val _sourcePosition =
            if(filePosition != null) {
                XDebuggerUtil.getInstance()!!.createPosition(
                        LocalFileSystem.getInstance()?.findFileByIoFile(File(filePosition.filePath)),
                        HaskellUtils.haskellLineNumberToZeroBased(filePosition.startLine))
            }
            else null
    override fun getSourcePosition(): XSourcePosition? = _sourcePosition

    /**
     * Returns evaluator (to use 'Evaluate expression' and other such tools)
     */
    override fun getEvaluator(): XDebuggerEvaluator? = HsDebuggerEvaluator(debugProcess.debugger)

    /**
     * Makes stack frame appearance customization in frames list. Sets function name, source file name and part of code
     * (span) that this frame represents
     */
    override fun customizePresentation(component: ColoredTextContainer) {
        val position = getSourcePosition()
        if (position != null) {
            component.append(position.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setSourceSpan(component, position)
            component.setIcon(AllIcons.Debugger.StackFrame);
        } else {
            component.append(XDebuggerBundle.message("invalid.frame") ?: "<invalid frame>",
                                                     SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
    }

    /**
     * Sets the bounds of code in source file this frame represents. Format is similar to one in ghci:
     * one line span: "<line number> : <start symbol number> - <end symbol number>"
     * multiline span: "(<start line number>,<start symbol number>) - (<end line number>,<end symbol number>)"
     */
    private fun setSourceSpan(component: ColoredTextContainer, position: XSourcePosition) {
        if (filePosition != null) {
            val srcSpan: String
            if (filePosition.startLine != filePosition.endLine) {
                srcSpan = ":(" + filePosition.startLine + "," + filePosition.startSymbol + ")-(" +
                        filePosition.endLine + "," + filePosition.endSymbol + ")"
            } else {
                srcSpan = ":" + filePosition.startLine +
                        ":" + filePosition.startSymbol + "-" + filePosition.endSymbol
            }
            component.append(srcSpan, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else {
            component.append(":" + (position.getLine() + 1), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    protected fun setChildrenToNode(node: XCompositeNode, bindings: ArrayList<LocalBinding>) {
        val list = XValueChildrenList()
        for (binding in bindings) {
            list.add(HsDebugValue(binding))
        }
        node.addChildren(list, true)
    }
}
