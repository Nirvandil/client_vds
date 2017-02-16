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
 *
 * Concrete class for adding domains to servers under ISPmanager 5 Lite
 */
class Isp5DomainsManipulator extends AbstractDomainsManipulator
    {

        Isp5DomainsManipulator(final Session session)
            {
                super(session);
            }

        @Override
        public String addDomain(final String domain, final String ip, final String own, String phpMod, String templatePath)
                throws IOException, JSchException, MainException
            {
                if (phpMod.contains("cgi"))
                    phpMod = "php_mode_cgi";
                else
                    phpMod = "php_mode_mod";
                String commString = "/usr/local/mgr5/sbin/mgrctl -m ispmgr webdomain.edit" + " name=" + domain + " alias=www." + domain +
                        " docroot=auto " + "owner=" + own + " email=admin@" + domain + " ip=" + ip +
                        "php=on php_mode=" + phpMod + " sok=ok";
                final ChannelExec channel = super.getChannelExec();
                final BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                // Handle template for site
                if (templatePath.length() > 1 && checkPathExist(templatePath))
                {
                    String destPath = constructDomainPath(own, domain);
                    String templateCopyCommand = " && shopt -s dotglob && " + "\\cp -r " + templatePath + "* "
                            + destPath + " && chown -R " + own + ":" + own + " " + destPath;
                    commString += templateCopyCommand;
                }
                channel.setCommand(commString);
                channel.connect();
                final String answer = in.readLine();
                if (answer != null && answer.contains("ERROR exists"))
                    {
                        channel.disconnect();
                        return "ERROR 2";
                    }
                else if (answer != null && answer.contains("ERROR limit"))
                    {
                        channel.disconnect();
                        return "ERROR limit";
                    }
                else
                    {
                        channel.disconnect();
                        return "";
                    }
            }

        @Override
        public List<String> getUsers() throws IOException, JSchException, MainException
            {
                final List<String> users = getCommandOutput("/usr/local/mgr5/sbin/mgrctl -m ispmgr user | cut -f 2 -d '=' | cut -f1 -d ' '");
                if (users.isEmpty()) throw new MainException("Похоже, Вы не добавили ни одного пользователя в панель управления!");
                else return users;
            }

        @Override
        public String removeDomain(final String domain, final String owner) throws IOException, JSchException, MainException
            {
                return null;
            }
        @Override
        public String constructDomainPath(String owner, String domainName)
        {
            return "/var/www/" + owner +"/data/www/" + domainName + "/";
        }
    }