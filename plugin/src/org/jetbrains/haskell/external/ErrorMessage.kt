package org.jetbrains.haskell.external

import org.json.simple.JSONObject

/**
 * Created by atsky on 17/05/14.
 */
class ErrorMessage(
    val text : String,
    val file : String,
    val severity : Severity,
    val line : Int,
    val column : Int,
    val eLine : Int,
    val eColumn : Int) {

    // Reflects BuildWrapper/Base.hs: BWNoteStatus
    enum class Severity { Error, Warning }

    override fun toString(): String {
        return "Error: $text\nin $file $line:$column-$eLine:$eColumn";
    }

    companion object {
        fun fromJson(a : Any) : ErrorMessage {
            val obj = a as JSONObject
            val text = obj["t"] as String
            val severity = obj["s"] as String
            val location = obj["l"] as JSONObject
            return ErrorMessage(
                text,
                location["f"] as String,
                if (severity == "Warning") Severity.Warning else Severity.Error,
                (location["l"] as Long).toInt(),
                (location["c"] as Long).toInt(),
                (location["el"] as Long).toInt(),
                (location["ec"] as Long).toInt()
            )
        }
    }
}