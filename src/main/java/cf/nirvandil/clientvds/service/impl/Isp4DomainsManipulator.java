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
 * Concrete class for adding domains to servers under ISPmanager 4 Lite
 */
@Slf4j
public class Isp4DomainsManipulator extends AbstractDomainsManipulator {
    public Isp4DomainsManipulator(final Session session) {
        super(session);
    }

    @Override
    public String addDomain(final String domain, final String ip, final String owner, String phpMod, String templatePath)
            throws IOException, JSchException {
        log.trace("Incoming params for adding: ", domain, ip, owner, phpMod, templatePath);
        if (phpMod.contains("CGI")) {
            phpMod = "phpcgi";
        } else {
            phpMod = "phpmod";
        }
        String commString = "/usr/local/ispmgr/sbin/mgrctl -m ispmgr wwwdomain.edit" +
                " domain=" + domain + " alias=www." + domain + " docroot=auto owner=" + owner + " admin=admin@" + domain +
                " autosubdomain=asdnone ip=" + ip + " php=" + phpMod + " sok=ok";
        // Handle template for site TODO: move it to interface/abstract class
        if (templatePath.length() > 1 && checkPathExist(templatePath)) {
            String destPath = constructDomainPath(owner, domain);
            String templateCopyCommand = " && shopt -s dotglob && " + "\\cp -r " + templatePath + "* "
                    + destPath + " && chown -R " + owner + ":" + owner + " " + destPath;
            commString += templateCopyCommand;
        }
        List<String> commandOutput = super.getCommandOutput(commString);
        log.debug(commString);
        for (String state : commandOutput) {
            log.debug(state);
            if (state != null && state.contains("ERROR 2")) {
                return "ERROR 2";
            } else if (state != null && state.contains("parsing")) {
                final String fileToRemove = state.substring(state.indexOf("'") + 1, state.lastIndexOf("'"));
                handleParsingError(fileToRemove);
                final String out = super.getCommandOutput(commString).get(0);
                log.debug(out);
                if (out.contains("parsing")) {
                    return "ERROR parsing";
                }
            } else if (state != null && state.contains("ERROR 8")) {
                return "ERROR 8";
            } else if (state != null && state.contains("ERROR 9")) {
                return "ERROR 9";
            }
        }
        return "";
    }

    @Override
    public List<String> getUsers() throws IOException, JSchException, MainException {
        final List<String> users = super.getCommandOutput("/usr/local/ispmgr/sbin/mgrctl user | cut -f 2 -d '='| cut -f 1 -d ' '");
        if (users.isEmpty())
            throw new MainException("Похоже, что Вы не добавили ни одного пользователя в панель управления!");
        else return users;
    }

    @Override
    public String removeDomain(final String domain, final String owner) throws IOException, JSchException {
        //Return just first line of output
        return super.getCommandOutput("/usr/local/ispmgr/sbin/mgrctl -m ispmgr wwwdomain.delete elid=" + domain +
                " wwwdomain.delete.confirm elid=" + domain + " sok=ok").get(0);
    }

    private void handleParsingError(final String path) throws IOException, JSchException {
        log.debug("Removing path {}", path);
        super.getCommandOutput("rm -f " + path);
    }

    @Override
    public String constructDomainPath(String owner, String domainName) {
        return "/var/www/" + owner + "/data/www/" + domainName + "/";
    }
}
