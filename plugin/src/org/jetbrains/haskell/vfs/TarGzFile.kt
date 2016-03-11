package org.jetbrains.haskell.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.util.*

/**
 * Created by atsky on 09/05/14.
 */
class TarGzFile(val archiveFile: VirtualFile,
                       val myPath: String) : VirtualFile() {

    var isInit = false;
    var myData: ByteArray? = null
    val myChildren = ArrayList<String>()

    fun doInit(): Boolean {
        if (isInit) {
            return true
        }
        val archiveIns = archiveFile.inputStream
        val bin = BufferedInputStream(archiveIns)
        val gzIn = GzipCompressorInputStream(bin);


        val tarArchiveInputStream = TarArchiveInputStream(gzIn)

        while (true) {
            val entry = tarArchiveInputStream.nextTarEntry ?: break;
            val entryName = entry.name ?: ""
            if (myPath == entryName) {
                myData = readToArray(tarArchiveInputStream)
            } else if (entryName.startsWith(myPath)) {
                val name = entryName.substring(myPath.length)
                if (!name.substring(0, name.length - 1).contains("/")) {
                    myChildren.add(name)
                }
            }
        }
        gzIn.close()
        isInit = true
        return (myData != null)
    }

    fun readToArray(ins: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()

        var nRead: Int
        val data = ByteArray(16384)

        while (true) {
            nRead = ins.read(data, 0, data.size)
            if (nRead == -1) {
                break;
            }
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    override fun getName(): String {
        val str = if (isDirectory) {
            myPath.substring(0, myPath.length - 1)
        } else {
            myPath
        }
        val indexOf = str.lastIndexOf('/')
        return myPath.substring(indexOf + 1)
    }

    override fun getFileSystem(): VirtualFileSystem = CabalVirtualFileSystem.INSTANCE

    override fun getPath(): String =
            archiveFile.path + "!" + myPath

    override fun isWritable() = false

    override fun isDirectory() = myPath.last() == '/'

    override fun isValid() = true

    override fun getParent(): VirtualFile? {
        val str = if (isDirectory) {
            myPath.substring(0, myPath.length - 1)
        } else {
            myPath
        }
        val indexOf = str.lastIndexOf('/')
        if (indexOf == -1) {
            return null
        }
        return TarGzFile(archiveFile, str.substring(0, indexOf + 1))
    }

    override fun getChildren(): Array<VirtualFile>? {
        doInit()
        val files : List<VirtualFile> = myChildren.map { TarGzFile(archiveFile, myPath + it) }
        return files.toTypedArray()
    }

    override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
        throw UnsupportedOperationException()
    }

    override fun contentsToByteArray(): ByteArray {
        doInit()
        return myData!!;
    }

    override fun getTimeStamp(): Long {
        return archiveFile.timeStamp
    }

    override fun getModificationStamp(): Long {
        return archiveFile.modificationStamp
    }

    override fun getLength(): Long {
        doInit()
        return myData!!.size.toLong()
    }

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {
        throw UnsupportedOperationException()
    }

    override fun getInputStream(): InputStream? {
        doInit()
        return ByteArrayInputStream(myData!!)
    }

}