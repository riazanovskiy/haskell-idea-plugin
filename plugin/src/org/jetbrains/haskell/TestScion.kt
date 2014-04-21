package org.jetbrains.haskell

import org.jetbrains.haskell.util.ProcessRunner
import java.io.OutputStreamWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.Inflater
import java.io.Writer
import java.io.Reader
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Created by atsky on 4/17/14.
 */
fun main(args : Array<String>) {
    val path = "/home/atsky/.cabal/bin/scion-browser"
    //val path = "/bin/cat"
    val processBuilder: ProcessBuilder = ProcessBuilder(path)
    val process = processBuilder.start()
    val streamWriter = OutputStreamWriter(process.getOutputStream()!!)
    val input: InputStream = process.getInputStream()!!
    val error = InputStreamReader(process.getErrorStream()!!)

    System.out.println(run("{ \"command\" : \"load-local-db\", \"filepath\" : \"local.db\", \"rebuild\" : true }\n", streamWriter, input, error))


    val result = run("{ \"command\" : \"get-declarations\", \"module\" : \"Data.Maybe\", \"db\" : \"_all\" }\n", streamWriter, input, error)


    System.out.println(result)
}


fun run(toRun : String, streamWriter : Writer, input: InputStream, error : Reader) : String {
    streamWriter.write(toRun)
    streamWriter.flush()

    while (input.available() == 0) {
        if (error.ready()) {
            val ch = error.read()
            if (ch != -1) {
                System.out.print(ch.toChar())
            }
        }
    }
    System.out.println()

    val len = input.available()
    val buff = ByteArray(len)
    input.read(buff)

    val inflater = Inflater()
    inflater.setInput(buff)

    val outCompressed = ByteArray(1024 * 1024)

    val size = inflater.inflate(outCompressed)

    return String(outCompressed, 0, size)
}