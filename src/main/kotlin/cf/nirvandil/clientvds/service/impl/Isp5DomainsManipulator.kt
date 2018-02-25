package cf.nirvandil.clientvds.service.impl

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.util.ISP5_GET_USERS_COMMAND
import cf.nirvandil.clientvds.util.NOT_SUPPORTED
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
 * Concrete class for adding domains to servers under ISPmanager 5 Lite
 */
class Isp5DomainsManipulator(session: Session) : AbstractDomainsManipulator(session) {
    override val users: List<String>
        @Throws(IOException::class, JSchException::class, MainException::class)
        get() {
            val users = getCommandOutput(ISP5_GET_USERS_COMMAND)
            return if (users.isEmpty())
                throw MainException(NO_USERS_MESSAGE)
            else
                users
        }

    @Throws(IOException::class, JSchException::class)
    override fun addDomain(domain: String, ip: String, own: String, phpMod: String, templatePath: String): String {
        log.trace("Incoming params for adding: $domain, $ip, $own, $phpMod, $templatePath")
        val php = if (phpMod.contains("cgi")) "php_mode_cgi" else "php_mode_mod"
        val commString = buildString {
            append("/usr/local/mgr5/sbin/mgrctl -m ispmgr webdomain.edit name=$domain alias=www.$domain " +
                    "docroot=auto owner=$own email=admin@$domain ip=$ip php=on php_mode=$php sok=ok")
            if (templatePath.isNotBlank() && checkPathExist(templatePath))
                append(createCpTemplateCommand(own, domain, templatePath))
        }
        log.debug(commString)
        getCommandOutput(commString).forEach { answer ->
            with(answer) {
                log.debug(this)
                return when {
                    contains("ERROR exists") -> "ERROR 2"
                    contains("ERROR limit") -> "ERROR limit"
                    else -> ""
                }
            }
        }
        return ""
    }

    override fun removeDomain(domain: String, owner: String): String {
        throw MainException(NOT_SUPPORTED)
    }

    override fun constructDomainPath(owner: String, domainName: String) = "/var/www/$owner/data/www/$domainName/"
}