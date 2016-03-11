package org.jetbrains.haskell.sdk

import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import org.jdom.Element
import org.jetbrains.haskell.icons.HaskellIcons
import org.jetbrains.haskell.util.GHCUtil
import org.jetbrains.haskell.util.GHCVersion
import org.jetbrains.haskell.util.ProcessRunner
import java.io.File
import java.io.FileFilter
import java.util.*
import javax.swing.Icon

class HaskellSdkType() : SdkType("GHC") {
//    override fun createAdditionalDataConfigurable(p0: SdkModel, p1: SdkModificator): AdditionalDataConfigurable? {
//        throw UnsupportedOperationException()
//    }

    class SDKInfo(val sdkPath : File) {
        val ghcHome: File
        val version: GHCVersion = GHCUtil.getVersion(sdkPath.name)

        init {
            ghcHome = if (SystemInfo.isMac && sdkPath.absolutePath.contains("GHC.framework")) {
                File(sdkPath, "usr")
            } else {
                sdkPath
            }
        }
    }

    override fun suggestHomePath(): String? {
        val versions: List<File>
        if (SystemInfo.isLinux) {
            val versionsRoot = File("/usr/lib")
            if (!versionsRoot.isDirectory) {
                return null
            }
            versions = (versionsRoot.listFiles({ dir, name -> name.toLowerCase().startsWith("ghc") && File(dir, name).isDirectory })?.toList() ?: listOf())
        } else if (SystemInfo.isWindows) {
            var progFiles = System.getenv("ProgramFiles(x86)")
            if (progFiles == null) {
                progFiles = System.getenv("ProgramFiles")
            }
            if (progFiles == null)
                return null
            val versionsRoot = File(progFiles, "Haskell Platform")
            if (!versionsRoot.isDirectory)
                return progFiles
            versions = versionsRoot.listFiles()?.toList() ?: listOf()
        } else if (SystemInfo.isMac) {
            val macVersions = ArrayList<File>()
            val versionsRoot = File("/Library/Frameworks/GHC.framework/Versions/")
            if (versionsRoot.isDirectory) {
                macVersions.addAll(versionsRoot.listFiles()?.toList() ?: listOf())
            }
            val brewVersionsRoot = File("/usr/local/Cellar/ghc")
            if (brewVersionsRoot.isDirectory) {
                macVersions.addAll(brewVersionsRoot.listFiles()?.toList() ?: listOf())
            }
            versions = macVersions
        } else {
            return null
        }
        val latestVersion = getLatestVersion(versions) ?: return null

        return latestVersion.ghcHome.absolutePath
    }

    override fun isValidSdkHome(path: String?): Boolean {
        return checkForGhc(path!!)
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String?): String {
        val suggestedName: String
        if (currentSdkName != null && currentSdkName.length > 0) {
            suggestedName = currentSdkName
        } else {
            val versionString = getVersionString(sdkHome)
            if (versionString != null) {
                suggestedName = "GHC " + versionString
            } else {
                suggestedName = "Unknown"
            }
        }
        return suggestedName
    }

    override fun getVersionString(sdkHome: String?): String? {
        val versionString: String? = getGhcVersion(sdkHome)
        if (versionString != null && versionString.length == 0) {
            return null
        }

        return versionString
    }

    override fun createAdditionalDataConfigurable(p0: SdkModel, p1: SdkModificator): AdditionalDataConfigurable? {
        return HaskellSdkConfigurable();
    }

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {
        if (additionalData is HaskellSdkAdditionalData) {
            additionalData.save(additional)
        }
    }

    override fun loadAdditionalData(additional: Element?): SdkAdditionalData? {
        return null;//HaskellSdkAdditionalData.load(additional!!);
    }
    override fun getPresentableName(): String {
        return "GHC"
    }

    override fun getIcon(): Icon {
        return GHC_ICON
    }

    override fun getIconForAddAction(): Icon {
        return icon
    }

    override fun setupSdkPaths(sdk: Sdk) {
    }

    override fun isRootTypeApplicable(type: OrderRootType): Boolean {
        return false
    }

    companion object {

        val INSTANCE: HaskellSdkType = HaskellSdkType()
        private val GHC_ICON: Icon = HaskellIcons.HASKELL

        fun getBinDirectory(path: String) :  File {
            return File(path, "bin")

        }

        private fun getLatestVersion(sdkPaths: List<File>): SDKInfo? {
            val length = sdkPaths.size
            if (length == 0)
                return null
            if (length == 1)
                return SDKInfo(sdkPaths[0])
            val ghcDirs = ArrayList<SDKInfo>()
            for (name in sdkPaths) {
                ghcDirs.add(SDKInfo(name))
            }
            Collections.sort(ghcDirs, { d1, d2 -> d1.version.compareTo(d2.version) })
            return ghcDirs[ghcDirs.size - 1]
        }

        fun checkForGhc(path: String): Boolean {
            val bin = getBinDirectory(path)
            if (!bin.isDirectory)
                return false
            val children = bin.listFiles(FileFilter { f ->
                if (f.isDirectory)
                    return@FileFilter false
                "ghc".equals(FileUtil.getNameWithoutExtension(f), ignoreCase = true)
            })
            return children != null && children.size >= 1
        }

        fun getGhcVersion(homePath: String?): String? {
            if (homePath == null || !File(homePath).isDirectory) {
                return null
            }
            try {
                val cmd = getBinDirectory(homePath).absolutePath + File.separator + "ghc"
                return ProcessRunner(null).executeOrFail(cmd, "--numeric-version").trim()
            } catch (ex: Exception) {
                // ignore
            }

            return null
        }
    }
}
