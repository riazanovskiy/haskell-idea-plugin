package org.jetbrains.haskell.external.tool

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Created by atsky on 04/01/15.
 */
class GhcModToolWindowFactory() : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory!!.createContent(createToolWindowPanel(project), "", false)
        toolWindow.contentManager!!.addContent(content)
    }

    private fun createToolWindowPanel(project: Project): JComponent {
        val panel = JPanel(BorderLayout())
        val ghcMod = project.getComponent(GhcModConsole::class.java)!!
        panel.add(ghcMod.editor!!.component, BorderLayout.CENTER)
        return panel
    }


}