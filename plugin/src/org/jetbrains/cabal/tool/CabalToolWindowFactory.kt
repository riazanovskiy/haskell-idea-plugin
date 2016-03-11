package org.jetbrains.cabal.tool


import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import org.jetbrains.cabal.CabalInterface
import org.jetbrains.cabal.CabalPackageShort
import org.jetbrains.haskell.icons.HaskellIcons
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeNode


class CabalToolWindowFactory() : ToolWindowFactory {
    private var toolWindow: ToolWindow? = null
    private var packages: JTree? = null
    private var project: Project? = null
    private var treeModel: DefaultTreeModel? = null

    class PackageData(val text : String, val installed : Boolean)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.project = project
        this.toolWindow = toolWindow
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory!!.createContent(createToolWindowPanel(), "", false)
        toolWindow.contentManager!!.addContent(content)
    }

    private fun createToolWindowPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(getToolbar(), BorderLayout.PAGE_START)

        val packagesList = CabalInterface(project!!).getPackagesList()
        val installedPackagesList = CabalInterface(project!!).getInstalledPackagesList()

        treeModel = DefaultTreeModel(getTree(packagesList, installedPackagesList, ""))
        val tree = Tree(treeModel)
        tree.cellRenderer = TreeCellRenderer { tree, value, selected, expanded, leaf, row, hasFocus ->
            val userObject = (value as DefaultMutableTreeNode).userObject ?: return@TreeCellRenderer JLabel()
            val packageData = userObject as PackageData
            val label = JLabel(packageData.text)
            if (packageData.installed) {
                label.foreground = Color(0, 140, 0)
            }
            label
        }

        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val path = tree.getPathForLocation(e.x, e.y) ?: return;
                    val pathArray = path.path

                    val packageName = pathArray[1] as DefaultMutableTreeNode
                    val packageVersion: DefaultMutableTreeNode? = if (pathArray.size == 3) {
                        (pathArray[2] as DefaultMutableTreeNode)
                    } else {
                        null
                    }

                    val menu = JPopupMenu();
                    menu.add(JMenuItem(object: AbstractAction("Install") {
                        override fun actionPerformed(e: ActionEvent) {
                            install((packageName.userObject as PackageData).text,
                                    (packageVersion?.userObject as PackageData?)?.text)
                        }

                    }))
                    menu.show(tree, e.x, e.y);
                }
            }
        })
        tree.isRootVisible = false;
        packages = tree

        panel.add(JBScrollPane(packages), BorderLayout.CENTER)
        return panel
    }

    fun getTree(packagesList: List<CabalPackageShort>,
                installedPackagesList: List<CabalPackageShort>,
                text: String): TreeNode {
        val root = DefaultMutableTreeNode()
        for (pkg in packagesList) {
            if (text != "" && !pkg.name.capitalize().contains(text.capitalize())) {
                continue
            }

            val installed = installedPackagesList.firstOrNull { it.name == pkg.name }
            val pkgNode = DefaultMutableTreeNode(PackageData(pkg.name, installed != null))
            for (version in pkg.availableVersions) {
                val installedVersions = installed?.availableVersions ?: listOf()
                pkgNode.add(DefaultMutableTreeNode(PackageData(version, installedVersions.contains(version))))
            }
            root.add(pkgNode)

        }

        return root;
    }

    fun updateTree(text: String) {
        val packagesList = CabalInterface(project!!).getPackagesList()
        val installedPackagesList = CabalInterface(project!!).getInstalledPackagesList()
        treeModel!!.setRoot(getTree(packagesList, installedPackagesList, text))
    }

    fun install(packageName: String, packageVersion: String?) {
        val cmd = if (packageVersion == null) {
            packageName
        } else {
            packageName + "-" + packageVersion
        }
        CabalInterface(project!!).install(cmd)
    }

    private fun getToolbar(): JComponent {
        val panel = JPanel()

        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)


        val group = DefaultActionGroup()
        group.add(UpdateAction())

        val actionToolBar = ActionManager.getInstance()!!.createActionToolbar("CabalTool", group, true)!!

        panel.add(actionToolBar.component!!)


        val searchTextField = SearchTextField()
        searchTextField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent?) {
                updateTree(searchTextField.text!!)
            }

        })


        panel.add(searchTextField)
        return panel
    }

    inner final class UpdateAction : AnAction("Update",
            "Update packages list",
            HaskellIcons.UPDATE) {


        override fun actionPerformed(e: AnActionEvent?) {
            CabalInterface(project!!).update()
        }
    }
}