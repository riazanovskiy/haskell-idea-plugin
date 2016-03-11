package org.jetbrains.haskell.sdk

import com.intellij.openapi.projectRoots.SdkAdditionalData
import org.jdom.Element
import org.jetbrains.haskell.util.OSUtil


class HaskellSdkAdditionalData(cabalPath: String?,
                                      cabalLibPath: String?) : SdkAdditionalData, Cloneable {


    private var myCabalPath: String? = cabalPath
    private var myCabalDataPath: String? = cabalLibPath


    override fun clone() : Any {
        return super.clone()
    }

    fun save(element: Element): Unit {
        if (myCabalPath != null) {
            element.setAttribute(CABAL_PATH, myCabalPath!!)
        }
        if (myCabalDataPath != null) {
            element.setAttribute(CABAL_DATA_PATH, myCabalDataPath!!)
        }
    }

    fun getCabalPath(): String {
        return if (myCabalPath == null) "" else myCabalPath!!
    }

    fun getCabalDataPath(): String {
        return if (myCabalDataPath == null) "" else myCabalDataPath!!
    }

    fun setCabalPath(cabalPath: String?): Unit {
        this.myCabalPath = cabalPath
    }


    companion object {
        private val CABAL_PATH = "cabal_path"
        private val CABAL_DATA_PATH = "cabal_data_path"

        fun getDefaultCabalPath() : String {
            return "/usr/bin/cabal"
        }

        fun getDefaultCabalDataPath() : String {
            return OSUtil.getCabalData()
        }

        fun load(element: Element): HaskellSdkAdditionalData {
            val data = HaskellSdkAdditionalData(null, null)
            data.myCabalPath = element.getAttributeValue(CABAL_PATH) ?: getDefaultCabalPath()
            data.myCabalDataPath = element.getAttributeValue(CABAL_DATA_PATH) ?: getDefaultCabalDataPath()
            return data
        }

    }

}
