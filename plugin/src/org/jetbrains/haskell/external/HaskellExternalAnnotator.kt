package org.jetbrains.haskell.external

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nullable
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.module.ModuleUtilCore
import org.json.simple.JSONArray
import java.io.File
import org.jetbrains.haskell.util.copyFile
import org.json.simple.JSONObject
import org.jetbrains.haskell.util.LineColPosition
import com.intellij.openapi.vfs.LocalFileSystem

public class HaskellExternalAnnotator() : ExternalAnnotator<PsiFile, List<ErrorMessage>>() {

    override fun collectInformation(file: PsiFile): PsiFile {
        return file
    }

    public fun getRelativePath(base: String, path: String): String {
        val bpath = File(base).getCanonicalPath()
        val fpath = File(path).getCanonicalPath()

        if (fpath.startsWith(bpath)) {
            return fpath.substring(bpath.length() + 1)
        } else {
            throw RuntimeException("Base path " + base + "is wrong to " + path);
        }
    }

    fun copyContent(basePath: VirtualFile, destination: File) {
        if (!destination.exists()) {
            destination.mkdir()
        }
        val localFileSystem = LocalFileSystem.getInstance()!!

        for (child in basePath.getChildren()!!) {
            if (child.getName().equals(".buildwrapper")) {
                continue
            }
            val destinationFile = File(destination, child.getName())
            if (child.isDirectory()) {
                copyContent(child, destinationFile)
            } else {
                val childTime = child.getModificationStamp()
                val destinationTime = localFileSystem.findFileByIoFile(destinationFile)?.getModificationStamp()
                if (destinationTime == null || childTime > destinationTime) {
                    copyFile(File(child.getPath()), destinationFile)
                }
            }
        }
    }

    fun getModuleContentDir(file : PsiFile) : VirtualFile {
        val module = ModuleUtilCore.findModuleForPsiElement(file)
        return module!!.getModuleFile()!!.getParent()!!
    }

    override fun doAnnotate(psiFile: PsiFile?): List<ErrorMessage> {
        val file = psiFile!!.getVirtualFile()
        if (file == null) {
            return listOf()
        }

        val moduleContent = getModuleContentDir(psiFile)

        copyContent(moduleContent, File(moduleContent.getCanonicalPath()!!, ".buildwrapper"))

        val cabals = moduleContent.getChildren()!!.filter { it.getName().endsWith(".cabal") }
        val cabal = cabals.head!!.getPath()

        val buildWrapper = BuildWrapper(moduleContent.getPath(), cabal)

        val path = getRelativePath(moduleContent.getPath(), file.getPath())
        val out = buildWrapper.build1(path)
        if (out != null) {
            val errors = out.get(1) as JSONArray

            return errors.map {
                ErrorMessage.fromJson(it!!)
            }
        }
        return listOf()
    }


    override fun apply(file: PsiFile, annotationResult: List<ErrorMessage>?, holder: AnnotationHolder) {
        val moduleContent = getModuleContentDir(file)
        val relativePath = getRelativePath(moduleContent.getPath(), file.getVirtualFile()!!.getPath())

        for (error in annotationResult!!) {
            if (relativePath == error.file) {
                val start = LineColPosition(error.line, error.column).getOffset(file)
                val end = LineColPosition(error.eLine, error.eColumn).getOffset(file)
                holder.createErrorAnnotation(TextRange(start, end), error.text);
            }
        }
    }
}
