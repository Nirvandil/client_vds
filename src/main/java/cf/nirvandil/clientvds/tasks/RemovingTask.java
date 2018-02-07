package cf.nirvandil.clientvds.tasks;

import cf.nirvandil.clientvds.service.DigitalOceanClient;
import cf.nirvandil.clientvds.service.DomainsManipulator;
import cf.nirvandil.clientvds.service.impl.DigitalOceanClientImpl;
import lombok.SneakyThrows;

import java.util.ArrayList;
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
public class RemovingTask extends AddingTask {
    private final DigitalOceanClient digitalOceanClient = new DigitalOceanClientImpl();

    public RemovingTask(final List<String> domains, final String ip, final DomainsManipulator domainsManipulator, final String owner,
                        final String phpMod, final String templatePath, final String token) {
        super(domains, ip, domainsManipulator, owner, phpMod, templatePath, token);
    }

    @Override
    @SneakyThrows
    protected Map<String, List<String>> call() {
        final int fullWork = domains.size();
        int done = 0;
        domainsManipulator.getUsers();
        //Into map we will put any errors that occurs while adding domain
        final Map<String, List<String>> result = new HashMap<>();
        for (final String domain : domains) {
            final String returnCode = domainsManipulator.removeDomain(domain, owner);
            String digitalAnswer = "";
            if (!token.isEmpty()) {
                digitalAnswer = digitalOceanClient.removeDomain(domain, token);
            }
            done += 1;
            updateProgress(done, fullWork);
            Thread.sleep(100);
            if (!returnCode.isEmpty()) {
                List<String> errors = new ArrayList<>();
                String message = "";
                if (returnCode.contains("doesn't exist")) {
                    if (returnCode.contains("domain"))
                        message = "Похоже, что домен " + domain + " не существует в \nпанели управления";
                    else if (returnCode.contains("user"))
                        message = "Похоже, что пользователь " + owner + " не существует в \nпанели управления";
                }
                errors.add(message);
                result.put(domain, errors);
            }
            if (!digitalAnswer.isEmpty()) {
                String oceanMessage = "Невозможно удалить " + domain + " с Digital Ocean, возможно,\n " +
                        "домена не существует или некорректный ключ API.";
                if (result.containsKey(domain)) {
                    super.putCarefully(result, oceanMessage, domain);
                }
            }
        }
        return result;
    }
}
