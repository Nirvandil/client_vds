package cf.nirvandil.clientvds;

import com.jcraft.jsch.JSchException;

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
 * This interface declares methods, that must contain object for
 * manipulating domains (adding, removing them) and run command on server.
 */
interface DomainsManipulator {
    String addDomain(String domain, String ip, String own, String phpMod, String templatePath) throws IOException, JSchException, MainException;

    String removeDomain(String domain, String owner) throws IOException, JSchException, MainException;

    String askUserOfPanel(List<String> users) throws IOException, JSchException, MainException;

    List<String> getCommandOutput(final String command) throws IOException, JSchException, MainException;

    List<String> getUsers() throws IOException, JSchException, MainException;

    boolean checkPathExist(String path) throws IOException, JSchException, MainException;

    void reportDone();

}
