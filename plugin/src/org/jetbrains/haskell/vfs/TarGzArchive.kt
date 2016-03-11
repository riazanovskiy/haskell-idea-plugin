package org.jetbrains.haskell.vfs

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by atsky on 12/12/14.
 */
class TarGzArchive(val file : File) {
    val filesList : List<String>

    init {
        val bin = BufferedInputStream(FileInputStream(file))
        val gzIn = GzipCompressorInputStream(bin);


        val tarArchiveInputStream = TarArchiveInputStream(gzIn)

        var file = ArrayList<String>()

        while (true) {
            val entry = tarArchiveInputStream.nextTarEntry ?: break;

            file.add(entry.name)
        }
        filesList = file;
        bin.close()
    }
}