package cf.nirvandil.clientvds.service.impl;

import cf.nirvandil.clientvds.exc.MainException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

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
 * <p>
 * Concrete class for adding domains to servers under ISPmanager 5 Lite
 */
@Slf4j
public class Isp5DomainsManipulator extends AbstractDomainsManipulator {

    public Isp5DomainsManipulator(final Session session) {
        super(session);
    }

    @Override
    public String addDomain(final String domain, final String ip, final String owner, String phpMod, String templatePath)
            throws IOException, JSchException {
        log.trace("Incoming params for adding: ", domain, ip, owner, phpMod, templatePath);
        if (phpMod.contains("cgi"))
            phpMod = "php_mode_cgi";
        else
            phpMod = "php_mode_mod";
        String commString = "/usr/local/mgr5/sbin/mgrctl -m ispmgr webdomain.edit" + " name=" + domain + " alias=www." + domain +
                " docroot=auto " + "owner=" + owner + " email=admin@" + domain + " ip=" + ip +
                "php=on php_mode=" + phpMod + " sok=ok";
        // Handle template for site
        if (templatePath.length() > 1 && checkPathExist(templatePath)) {
            String destPath = constructDomainPath(owner, domain);
            String templateCopyCommand = " && shopt -s dotglob && " + "\\cp -r " + templatePath + "* "
                    + destPath + " && chown -R " + owner + ":" + owner + " " + destPath;
            commString += templateCopyCommand;
        }
        log.debug(commString);
        List<String> commandOutput = getCommandOutput(commString);
        for (String answer : commandOutput) {
            log.debug(answer);
            if (answer != null && answer.contains("ERROR exists")) {
                return "ERROR 2";
            } else if (answer != null && answer.contains("ERROR limit")) {
                return "ERROR limit";
            }
        }
        return "";
    }

    @Override
    public List<String> getUsers() throws IOException, JSchException, MainException {
        final List<String> users = getCommandOutput("/usr/local/mgr5/sbin/mgrctl -m ispmgr user | cut -f 2 -d '=' | cut -f1 -d ' '");
        if (users.isEmpty())
            throw new MainException("Похоже, Вы не добавили ни одного пользователя в панель управления!");
        else return users;
    }

    @Override
    public String removeDomain(final String domain, final String owner) {
        return null;
    }

    @Override
    public String constructDomainPath(String owner, String domainName) {
        return "/var/www/" + owner + "/data/www/" + domainName + "/";
    }
}