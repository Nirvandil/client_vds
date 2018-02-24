package cf.nirvandil.clientvds.service.impl

import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import lombok.SneakyThrows

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
 * Domains manipulator for adding or deleting domains on servers
 * under VestaCP.
 */
class VestaDomainsManipulator(session: Session) : AbstractDomainsManipulator(session) {

    override val users: List<String>
        @SneakyThrows
        get() = super.getCommandOutput("/bin/ls /usr/local/vesta/data/users/")

    @SneakyThrows
    override fun addDomain(domain: String, ip: String, own: String, phpMod: String, templatePath: String): String {
        log.trace("Incoming params for adding: $domain, $ip, $own, $phpMod, $templatePath")
        val vestaPath = "/usr/local/vesta"
        val vestaVar = "VESTA=$vestaPath"
        var commStr = "$vestaVar $vestaPath/bin/v-add-web-domain $own $domain $ip ; " +
                "$vestaVar $vestaPath/bin/v-add-dns-domain $own $domain $ip"
        if (!phpMod.contains("CGI"))
            commStr += " ; $vestaVar /usr/local/vesta/bin/v-change-web-domain-tpl $own $domain default YES"
        if (templatePath.length > 1 && checkPathExist(templatePath)) {
            commStr += "; rm -f /home/$own/web/$domain/public_html/*"
            commStr = addCpTemplateCommand(commStr, own, domain, templatePath)
        }
        log.debug(commStr)
        getCommandOutput(commStr).forEach { answer ->
            log.debug("Output from command: $answer")
            if (answer.contains("exist")) return "exist"
        }
        return ""
    }

    @Throws(IOException::class, JSchException::class)
    override fun removeDomain(domain: String, owner: String): String {
        log.info("Removing domain $domain from VESTA")
        val command = "VESTA=/usr/local/vesta /usr/local/vesta/bin/v-delete-web-domain $owner $domain; " +
                "VESTA=/usr/local/vesta /usr/local/vesta/bin/v-delete-dns-domain $owner $domain"
        log.debug(command)
        val output = getCommandOutput(command)
        return if (output.isEmpty()) "" else output[0]
    }

    override fun constructDomainPath(owner: String, domainName: String) = "/home/$owner/web/$domainName/public_html/"
}