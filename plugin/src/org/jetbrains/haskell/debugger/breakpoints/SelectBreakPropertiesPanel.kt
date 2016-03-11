package org.jetbrains.haskell.debugger.breakpoints

//import org.jetbrains.haskell.debugger.protocol.BreakListForLineCommand
import com.intellij.openapi.ui.ComboBox
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel
import org.jetbrains.haskell.debugger.HaskellDebugProcess
import org.jetbrains.haskell.debugger.parser.BreakInfo
import org.jetbrains.haskell.debugger.utils.HaskellUtils
import org.jetbrains.haskell.debugger.utils.UIUtils
import java.awt.GridLayout
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Panel with additional breakpoint settings (make right click on breakpoint to see it)
 *
 * @author Habibullin Marat
 */
class SelectBreakPropertiesPanel : XBreakpointCustomPropertiesPanel<XLineBreakpoint<XBreakpointProperties<out Any?>>>() {
    private val PANEL_LABEL: String = "Select breakpoint:"
    private val DEBUG_NOT_STARTED_ITEM: String = "start debug process to enable"
    private val breaksComboBox: ComboBox = ComboBox(DefaultComboBoxModel(arrayOf(DEBUG_NOT_STARTED_ITEM)))
    private val mainPanel: JPanel = JPanel(GridLayout(1, 0))

    init {
        UIUtils.addLabeledControl(mainPanel, 0, PANEL_LABEL, breaksComboBox)
        breaksComboBox.isEnabled = false
    }

    private var debugManager: XDebuggerManager? = null
    private var debugProcess: HaskellDebugProcess? = null
    private var breaksList: ArrayList<BreakInfo>? = ArrayList()
    private var lastSelectedIndex: Int? = null

    override fun getComponent(): JComponent = mainPanel

    /**
     * Called when one press 'Done' button in breakpoint's context menu. Saves user selection and resets breakpoint if needed
     */
    override fun saveTo(breakpoint: XLineBreakpoint<XBreakpointProperties<out Any?>>) {
        if(debuggingInProgress()) {
            val selectedIndex = breaksComboBox.selectedIndex
            if (selectedIndex != lastSelectedIndex && debugProcess != null) {
                breakpoint.putUserData(HaskellLineBreakpointHandler.INDEX_IN_BREAKS_LIST_KEY, selectedIndex)
                val moduleName = HaskellUtils.getModuleName(debugManager!!.currentSession!!.project, breakpoint.sourcePosition!!.file)
                debugProcess?.removeBreakpoint(moduleName, HaskellUtils.zeroBasedToHaskellLineNumber(breakpoint.line))
                debugProcess?.addBreakpointByIndex(moduleName, breaksList!![selectedIndex].breakIndex, breakpoint)
            }
        }
    }

    /**
     * Called on every right click on breakpoint. Fills combo box with available breaks info
     */
    override fun loadFrom(breakpoint: XLineBreakpoint<XBreakpointProperties<out Any?>>) {
        getUserData(breakpoint)
        fillComboBox()
    }

    private fun getUserData(breakpoint: XLineBreakpoint<XBreakpointProperties<out Any?>>) {
        val project = breakpoint.getUserData(HaskellLineBreakpointHandler.PROJECT_KEY)
        if(project != null) {
            debugManager = XDebuggerManager.getInstance(project)
            val justDebugProcess = debugManager?.currentSession?.debugProcess
            if(justDebugProcess != null) {
                debugProcess = justDebugProcess as HaskellDebugProcess
            } else {
                debugProcess = null
            }
        }
        breaksList = breakpoint.getUserData(HaskellLineBreakpointHandler.BREAKS_LIST_KEY)
        lastSelectedIndex = breakpoint.getUserData(HaskellLineBreakpointHandler.INDEX_IN_BREAKS_LIST_KEY)
    }

    private fun fillComboBox() {
        breaksComboBox.removeAllItems()
        if(debuggingInProgress() && (breaksList as ArrayList<BreakInfo>).isNotEmpty()) {
            for (breakEntry in breaksList as ArrayList<BreakInfo>) {
                breaksComboBox.addItem(breakEntry.srcSpan.spanToString())
            }
            breaksComboBox.selectedIndex = lastSelectedIndex as Int
            breaksComboBox.isEnabled = true
        } else {
            breaksComboBox.addItem(DEBUG_NOT_STARTED_ITEM)
            breaksComboBox.isEnabled = false
        }
    }

    private fun debuggingInProgress(): Boolean {
        return debugManager?.currentSession != null
    }
}