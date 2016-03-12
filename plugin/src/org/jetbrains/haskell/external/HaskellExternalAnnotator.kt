package org.jetbrains.haskell.external

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.xml.util.XmlStringUtil
import org.jetbrains.haskell.config.HaskellSettings
import org.jetbrains.haskell.util.LineColPosition
import org.jetbrains.haskell.util.copyFile
import org.jetbrains.haskell.util.getRelativePath
import org.json.simple.JSONArray
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import java.util.regex.Pattern

class HaskellExternalAnnotator() : ExternalAnnotator<PsiFile, List<ErrorMessage>>() {

    override fun collectInformation(file: PsiFile): PsiFile {
        return file
    }

    fun copyContent(basePath: VirtualFile, destination: File) {
        if (!destination.exists()) {
            destination.mkdir()
        }
        val localFileSystem = LocalFileSystem.getInstance()!!

        val destinationFiles = HashSet(destination.list()!!.toList())

        for (child in basePath.children!!) {
            destinationFiles.remove(child.name)
            if (child.name.equals(".idea")) {
                continue
            }
            if (child.name.equals("dist")) {
                continue
            }
            if (child.name.equals(".buildwrapper")) {
                continue
            }
            val destinationFile = File(destination, child.name)
            if (child.isDirectory) {
                copyContent(child, destinationFile)
            } else {
                val childTime = child.modificationStamp
                val document = FileDocumentManager.getInstance().getCachedDocument(child)
                if (document != null) {
                    val stream = ByteArrayInputStream(document.text.toByteArray(child.charset));
                    copyFile(stream, destinationFile)
                } else {
                    val destinationTime = localFileSystem.findFileByIoFile(destinationFile)?.modificationStamp
                    if (destinationTime == null || childTime > destinationTime) {
                        copyFile(child.inputStream!!, destinationFile)
                    }
                }
            }
        }
        for (file in destinationFiles) {
            if (file.endsWith(".hs")) {
                File(destination, file).delete()
            }
        }
    }

    fun getResultFromGhcModi(psiFile: PsiFile,
                             baseDir: VirtualFile,
                             file: VirtualFile): List<ErrorMessage> {
        ApplicationManager.getApplication()!!.invokeAndWait({ FileDocumentManager.getInstance().saveAllDocuments() }, ModalityState.any())


        val ghcModi = psiFile.project.getComponent(GhcModi::class.java)!!

        val relativePath = getRelativePath(baseDir.path, file.path)

        val result = ghcModi.runCommand("check $relativePath")

        val errors = ArrayList<ErrorMessage>()

        for (resultLine in result) {
            val matcher = Pattern.compile("(.*):(\\d*):(\\d*):(.*)").matcher(resultLine)
            if (matcher.find()) {
                val path = matcher.group(1)!!
                val line = Integer.parseInt(matcher.group(2)!!)
                val col = Integer.parseInt(matcher.group(3)!!)
                val msg = matcher.group(4)!!.replace("\u0000", "\n").trimStart()
                val severity = if (msg.startsWith("Warning")) {
                    ErrorMessage.Severity.Warning
                } else {
                    ErrorMessage.Severity.Error
                }
                if (relativePath == path) {
                    errors.add(ErrorMessage(msg, path, severity, line, col, line, col))
                }
            }
        }

        return errors
    }

    fun getResultFromBuidWrapper(psiFile: PsiFile,
                                 moduleContent: VirtualFile,
                                 file: VirtualFile): List<ErrorMessage> {
        ApplicationManager.getApplication()!!.invokeAndWait({ FileDocumentManager.getInstance().saveAllDocuments() }, ModalityState.any())

        copyContent(moduleContent, File(moduleContent.canonicalPath!!, ".buildwrapper"))

        val out = BuildWrapper.init(psiFile).build1(file)
        if (out != null) {
            val errors = out[1] as JSONArray

            return errors.map {
                ErrorMessage.fromJson(it!!)
            }
        }
        return listOf()
    }

    fun getProjectBaseDir(psiFile: PsiFile): VirtualFile? {
        return psiFile.project.baseDir
    }

    override fun doAnnotate(psiFile: PsiFile?): List<ErrorMessage> {
        val file = psiFile!!.virtualFile ?: return listOf()

        val baseDir = getProjectBaseDir(psiFile) ?: return listOf()

        if (!(HaskellSettings.getInstance().state.useGhcMod!!)) {
            return listOf();
        }
        if (!file.isInLocalFileSystem) {
            return listOf()
        }
        return getResultFromGhcModi(psiFile, baseDir, file)
    }


    override fun apply(file: PsiFile, annotationResult: List<ErrorMessage>, holder: AnnotationHolder) {
        for (error in annotationResult) {

            val start = LineColPosition(error.line, error.column).getOffset(file)
            val end = LineColPosition(error.eLine, error.eColumn).getOffset(file)


            val element = file.findElementAt(start)
            val textRange = if (element != null) {
                element.textRange
            } else {
                TextRange(start, end)
            }

            val text = XmlStringUtil.wrapInHtml("<pre>" + XmlStringUtil.escapeString(error.text) + "</pre>")

            val severity = when (error.severity) {
                ErrorMessage.Severity.Error -> HighlightSeverity.ERROR
                ErrorMessage.Severity.Warning -> HighlightSeverity.WARNING
            }
            val annotation = holder.createAnnotation(severity, textRange, error.text, text);
        }
    }
}
