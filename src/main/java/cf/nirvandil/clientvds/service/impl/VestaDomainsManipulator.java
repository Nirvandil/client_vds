package cf.nirvandil.clientvds.service.impl;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.SneakyThrows;
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
 * Domains manipulator for adding or deleting domains on servers
 * under VestaCP.
 */
@Slf4j
public class VestaDomainsManipulator extends AbstractDomainsManipulator {
    public VestaDomainsManipulator(final Session session) {
        super(session);
    }

    @Override
    @SneakyThrows
    public String addDomain(final String domain, final String ip, final String owner, final String phpMod, String templatePath) {
        log.trace("Incoming params for adding: ", domain, ip, owner, phpMod, templatePath);
        String commString = "VESTA=/usr/local/vesta /usr/local/vesta/bin/v-add-web-domain " +
                owner + " " + domain + " " + ip;
        commString += " ; VESTA=/usr/local/vesta /usr/local/vesta/bin/v-add-dns-domain " +
                owner + " " + domain + " " + ip;
        //If not CGI -> as module
        if (!phpMod.contains("CGI")) {
            commString += " ; VESTA=/usr/local/vesta /usr/local/vesta/bin/v-change-web-domain-tpl " +
                    owner + " " + domain + " default " + "YES";
        }
        if (templatePath.length() > 1 && checkPathExist(templatePath)) {
            String destPath = constructDomainPath(owner, domain);
            String templateCopyCommand = " && shopt -s dotglob && " + "\\cp -r " + templatePath + "* "
                    + destPath + " && chown -R " + owner + ":" + owner + " " + destPath;
            commString += "; rm -f /home/" + owner + "/web/" + domain + "/public_html/*";
            commString += templateCopyCommand;
        }
        log.debug(commString);
        List<String> commandOutput = getCommandOutput(commString);
        for (String answer : commandOutput) {
            log.debug(answer);
            if (answer != null && answer.contains("exist")) {
                return "exist";
            }
        }
        return "";
    }

    @Override
    @SneakyThrows
    public List<String> getUsers() {
        return getCommandOutput("/bin/ls /usr/local/vesta/data/users/");
    }

    @Override
    public String removeDomain(final String domain, final String owner) throws IOException, JSchException {
        log.info("Removing domain {} from VESTA", domain);
        String command = "VESTA=/usr/local/vesta " +
                "/usr/local/vesta/bin/v-delete-web-domain " + owner + " " + domain + "; VESTA=/usr/local/vesta " +
                "/usr/local/vesta/bin/v-delete-dns-domain " + owner + " " + domain;
        log.debug(command);

        List<String> output = getCommandOutput(command);
        if (output.isEmpty()) {
            return "";
        }
        return output.get(0);
    }

    @Override
    public String constructDomainPath(String owner, String domainName) {
        return "/home/" + owner + "/web/" + domainName + "/public_html/";
    }
}
