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
 * Concrete class for adding domains to servers under ISPmanager 4 Lite
 */
class Isp4DomainsManipulator extends AbstractDomainsManipulator {
    Isp4DomainsManipulator(final Session session) {
        super(session);
    }

    @Override
    public String addDomain(final String domain, final String ip, final String own, String phpMod, String templatePath)
            throws IOException, JSchException {
        if (phpMod.contains("CGI"))
            phpMod = "phpcgi";
        else phpMod = "phpmod";
        final ChannelExec channel = super.getChannelExec();
        final BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        String commString = "/usr/local/ispmgr/sbin/mgrctl -m ispmgr wwwdomain.edit" +
                " domain=" + domain + " alias=www." + domain + " docroot=auto owner=" + own + " admin=admin@" + domain +
                " autosubdomain=asdnone ip=" + ip + " php=" + phpMod + " sok=ok";
        // Handle template for site TODO: move it to interface/abstract class
        if (templatePath.length() > 1 && checkPathExist(templatePath)) {
            String destPath = constructDomainPath(own, domain);
            String templateCopyCommand = " && shopt -s dotglob && " + "\\cp -r " + templatePath + "* "
                    + destPath + " && chown -R " + own + ":" + own + " " + destPath;
            commString += templateCopyCommand;
        }
        channel.setCommand(commString);
        channel.connect();
        final String state = in.readLine();
        if (state != null && state.contains("ERROR 2")) {
            channel.disconnect();
            return "ERROR 2";
        } else if (state != null && state.contains("parsing")) {
            final String fileToRemove = state.substring(state.indexOf("'") + 1, state.lastIndexOf("'"));
            handleParsingError(fileToRemove);
            final String out = getCommandOutput(commString).get(0);
            if (out.contains("parsing")) {
                channel.disconnect();
                return "ERROR parsing";
            }
            channel.disconnect();
        } else if (state != null && state.contains("ERROR 8")) {
            channel.disconnect();
            return "ERROR 8";
        } else if (state != null && state.contains("ERROR 9")) {
            channel.disconnect();
            return "ERROR 9";
        } else channel.disconnect();
        return "";
    }

    @Override
    public List<String> getUsers() throws IOException, JSchException, MainException {
        final List<String> users = getCommandOutput("/usr/local/ispmgr/sbin/mgrctl user | cut -f 2 -d '='| cut -f 1 -d ' '");
        if (users.isEmpty())
            throw new MainException("Похоже, что Вы не добавили ни одного пользователя в панель управления!");
        else return users;
    }

    @Override
    public String removeDomain(final String domain, final String owner) throws IOException, JSchException {
        //Return just first line of output
        return getCommandOutput("/usr/local/ispmgr/sbin/mgrctl -m ispmgr wwwdomain.delete elid=" + domain +
                " wwwdomain.delete.confirm elid=" + domain + " sok=ok").get(0);
    }

    private void handleParsingError(final String path) throws IOException, JSchException {
        getCommandOutput("rm -f " + path);
    }

    @Override
    public String constructDomainPath(String owner, String domainName) {
        return "/var/www/" + owner + "/data/www/" + domainName + "/";
    }
}
