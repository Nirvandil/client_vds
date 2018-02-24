package cf.nirvandil.clientvds.util

import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.util.*

/**
 * Created by Vladimir Sukharev aka Nirvandil on 04.09.16.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * This class is used for platform-dependent opening hyperlinks in
 * external browser
 */
object DesktopApi {
    private val log = LoggerFactory.getLogger(javaClass)
    private val os: EnumOS
        get() {
            val osName = System.getProperty("os.name").toLowerCase()
            return when {
                osName.contains("win") -> EnumOS.WINDOWS
                osName.contains("mac") -> EnumOS.MACOS
                osName.contains("SOLARIS") -> EnumOS.SOLARIS
                osName.contains("sunos") -> EnumOS.SOLARIS
                osName.contains("LINUX") -> EnumOS.LINUX
                else -> if (osName.contains("unix")) EnumOS.LINUX else EnumOS.UNKNOWN
            }
        }

    fun browse(uri: URI) {
        if (!browseDESKTOP(uri)) {
            openSystemSpecific(uri.toString())
        }
    }

    private fun openSystemSpecific(what: String) {
        val os = os
        if (os.isLinux) {
            if (runCommand("kde-open", "%s", what)) return
            if (runCommand("gnome-open", "%s", what)) return
            if (runCommand("xdg-open", "%s", what)) return
        }
        if (os.isMac && runCommand("open", "%s", what)) return
        if (os.isWindows) runCommand("explorer", "%s", what)
    }

    private fun browseDESKTOP(uri: URI): Boolean {
        try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                logErr("BROWSE is not supported.")
                return false
            }
            Desktop.getDesktop().browse(uri)
            return true
        } catch (t: Throwable) {
            logErr("Error using desktop browse.", t)
            return false
        }
    }

    private fun runCommand(command: String, args: String, file: String): Boolean {
        val parts = prepareCommand(command, args, file)
        try {
            val p = Runtime.getRuntime().exec(parts) ?: return false
            return try {
                if (p.exitValue() == 0) {
                    logErr("Process ended immediately.")
                    false
                } else {
                    logErr("Process crashed.")
                    false
                }
            } catch (itse: IllegalThreadStateException) {
                true
            }
        } catch (e: IOException) {
            logErr("Error running command.", e)
            return false
        }
    }

    private fun prepareCommand(command: String, args: String?, file: String): Array<String> {
        with(ArrayList<String>()) {
            add(command)
            args?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                    ?.mapTo(this) { s -> String.format(s, file).trim { it <= ' ' } }
            return toTypedArray()
        }
    }

    private fun logErr(msg: String, t: Throwable) {
        log.error(msg)
        log.error("{}", t)
    }

    private fun logErr(msg: String) {
        log.error(msg)
    }

    private enum class EnumOS {
        LINUX, MACOS, SOLARIS, UNKNOWN, WINDOWS;
        val isLinux: Boolean
            get() = this == LINUX || this == SOLARIS
        val isMac: Boolean
            get() = this == MACOS
        val isWindows: Boolean
            get() = this == WINDOWS
    }
}