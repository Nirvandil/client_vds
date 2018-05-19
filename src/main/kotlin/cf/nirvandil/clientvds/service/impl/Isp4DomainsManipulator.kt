package cf.nirvandil.clientvds.service.impl

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.model.DomainDescriptor
import cf.nirvandil.clientvds.util.ISP4_BIN_PATH
import cf.nirvandil.clientvds.util.ISP4_GET_USERS_COMMAND
import cf.nirvandil.clientvds.util.NO_USERS_MESSAGE
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import java.io.IOException

/**
 * Created by Vladimir Sukharev aka Nirvandil on 07.09.2016.
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
 * Concrete class for adding domains to servers under ISPmanager 4 Lite
 */
class Isp4DomainsManipulator(session: Session) : AbstractDomainsManipulator(session) {
    override val users: List<String>
        @Throws(IOException::class, JSchException::class, MainException::class)
        get() {
            val users = super.getCommandOutput(ISP4_GET_USERS_COMMAND)
            return if (!users.isEmpty()) users else throw MainException(NO_USERS_MESSAGE)
        }

    @Throws(IOException::class, JSchException::class)
    override fun addDomain(descriptor: DomainDescriptor): String {
        val (domain, owner, ip, phpMode, templatePath) = descriptor
        log.trace("Incoming params for adding: $domain, $ip, $owner, $phpMode, $templatePath")
        val php = if (phpMode.contains("CGI")) "phpcgi" else "phpmod"
        val commString = buildString {
            append("$ISP4_BIN_PATH -m ispmgr wwwdomain.edit domain=$domain alias=www.$domain " +
                    "docroot=auto owner=$owner admin=admin@$domain autosubdomain=asdnone ip=$ip php=$php sok=ok")
            if (templatePath.isNotBlank() && checkPathExist(templatePath))
                append(createCpTemplateCommand(owner, domain, templatePath))
        }
        log.debug("Command to run for add domain $commString")
        super.getCommandOutput(commString).forEach { answer ->
            with(answer) {
                log.debug(this)
                when {
                    contains("ERROR 2") -> return "ERROR 2"
                    contains("parsing") -> {
                        tryFixParsingError(this)
                        val out = super.getCommandOutput(commString)[0]
                        log.debug("Out after try for repair parsing is $out")
                        if (out.contains("parsing")) return "ERROR parsing"
                    }
                    contains("ERROR 8") -> return "ERROR 8"
                    contains("ERROR 9") -> return "ERROR 9"
                }
            }
        }
        return ""
    }

    @Throws(IOException::class, JSchException::class)
    override fun removeDomain(descriptor: DomainDescriptor): String {
        with(descriptor) {
            return super.getCommandOutput("$ISP4_BIN_PATH -m ispmgr wwwdomain.delete elid=$domain " +
                    "wwwdomain.delete.confirm elid=$domain sok=ok")[0]
        }
    }

    @Throws(IOException::class, JSchException::class)
    private fun tryFixParsingError(line: String) {
        with(line) {
            val fileToRemove = substring(indexOf("'") + 1, lastIndexOf("'"))
            log.debug("Removing path {}", fileToRemove)
            super.getCommandOutput("rm -f " + fileToRemove)
        }
    }

    override fun constructDomainPath(owner: String, domainName: String) = "/var/www/$owner/data/www/$domainName/"
}