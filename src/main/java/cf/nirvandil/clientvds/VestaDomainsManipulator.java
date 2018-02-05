package cf.nirvandil.clientvds;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
class VestaDomainsManipulator extends AbstractDomainsManipulator {
    VestaDomainsManipulator(final Session session) {
        super(session);
    }

    @Override
    public String addDomain(final String domain, final String ip, final String own, final String phpMod, String templatePath)
            throws IOException, JSchException {
        final ChannelExec channel = super.getChannelExec();
        final BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        String commString = "/usr/local/vesta/bin/v-add-web-domain " + own + " " + domain + " " + ip;
        commString += " ; /usr/local/vesta/bin/v-add-dns-domain " + own + " " + domain + " " + ip;
        //If not CGI -> as module
        if (!phpMod.contains("CGI")) {
            commString += " ; /usr/local/vesta/bin/v-change-web-domain-tpl " + own + " " + domain + " default " + "YES";
        }
        if (templatePath.length() > 1 && checkPathExist(templatePath)) {
            String destPath = constructDomainPath(own, domain);
            String templateCopyCommand = " && shopt -s dotglob && " + "\\cp -r " + templatePath + "* "
                    + destPath + " && chown -R " + own + ":" + own + " " + destPath;
            commString += templateCopyCommand;
        }
        channel.setCommand(commString);
        channel.connect();
        final String answer = in.readLine();
        if (answer != null && answer.contains("exist")) {
            channel.disconnect();
            return "exist";
        } else {
            channel.disconnect();
            return "";
        }
    }

    @Override
    public List<String> getUsers() throws IOException, JSchException {
        return getCommandOutput("/bin/ls /usr/local/vesta/data/users/");
    }

    @Override
    public String removeDomain(final String domain, final String owner) throws IOException, JSchException {
        return getCommandOutput("/usr/local/vesta/bin/v-delete-web-domain " + owner + " " + domain).get(0);
    }

    @Override
    public String constructDomainPath(String owner, String domainName) {
        return "/home/" + owner + "/web/" + domainName + "/public_html/";
    }
}
