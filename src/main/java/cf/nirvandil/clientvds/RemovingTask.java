package cf.nirvandil.clientvds;

import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 25.09.16.
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
 * Task for removing domains
 */
class RemovingTask extends AddingTask {
    RemovingTask(final List<String> domains, final String ip, final DomainsManipulator domainsManipulator, final String owner,
                 final String phpMod, final String templatePath) {
        super(domains, ip, domainsManipulator, owner, phpMod, templatePath);
    }

    @Override
    @SneakyThrows
    protected Map<String, String> call() {
        final int fullWork = domains.size();
        int done = 0;
        domainsManipulator.getUsers();
        //Into map we will put any errors that occurs while adding domain
        final Map<String, String> result = new HashMap<>();
        for (final String domain : domains) {
            final String returnCode = domainsManipulator.removeDomain(domain, owner);
            done += 1;
            updateProgress(done, fullWork);
            Thread.sleep(100);
            if (!returnCode.equals("")) {
                String message = "";
                if (returnCode.contains("doesn't exist")) {
                    if (returnCode.contains("DOMAIN"))
                        message = "Похоже, что домен " + domain + " не существует в \nпанели управления";
                    else
                        message = "Похоже, что пользователь " + owner + " не существует в \nпанели управления";
                }
                result.put(domain, message);
            }
        }
        return result;
    }
}
