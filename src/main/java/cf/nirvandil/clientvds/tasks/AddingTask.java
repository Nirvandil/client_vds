package cf.nirvandil.clientvds.tasks;

import cf.nirvandil.clientvds.service.DigitalOceanClient;
import cf.nirvandil.clientvds.service.DomainsManipulator;
import cf.nirvandil.clientvds.service.impl.DigitalOceanClientImpl;
import javafx.concurrent.Task;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 08.09.16.
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
 * This is task for adding domains to server.
 */
public class AddingTask extends Task<Map<String, List<String>>> {
    final List<String> domains;
    final DomainsManipulator domainsManipulator;
    final String owner;
    private final String ip;
    private final String templatePath;
    private final String token;
    private final String phpMod;
    private final DigitalOceanClient digitalOceanClient = new DigitalOceanClientImpl();

    public AddingTask(final List<String> domains, final String ip, final DomainsManipulator domainsManipulator,
                      final String owner, final String phpMod, final String templatePath, final String token) {
        this.domains = domains;
        this.ip = ip;
        this.domainsManipulator = domainsManipulator;
        this.owner = owner;
        this.phpMod = phpMod;
        this.templatePath = templatePath;
        this.token = token;
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
            String returnCode = domainsManipulator.addDomain(domain, ip, owner, phpMod, templatePath);
            String digitalOceanAnswer = "";
            if (!token.isEmpty()) {
                digitalOceanAnswer = digitalOceanClient.addDomain(domain, ip, token);
            }
            done += 1;
            updateProgress(done, fullWork);
            Thread.sleep(100);
            if (!returnCode.isEmpty()) {
                String message = "";
                switch (returnCode) {
                    case "ERROR 2":
                        message = "Похоже, что домен " + domain + " уже существует в \nпанели управления";
                        break;
                    case "exist":
                        message = "Похоже, что домен " + domain + " уже существует в \nпанели управления";
                        break;
                    case "ERROR limit":
                        message = "Превышено ограничение на число доменов для указанного пользователя";
                        break;
                    case "ERROR 8":
                        message = "Указанному пользователю запрещено использовать РНР в этом режиме";
                        break;
                    case "ERROR 9":
                        message = "Похоже, что домен " + domain + " уже существует на FriendDNS!\n" +
                                "(или иная ошибка внеших DNS)";
                        break;
                    case "ERROR parsing":
                        message = "Ошибка при синтаксическом разборе файла .passwd," +
                                "и автоматическое исправление не удалось.\nНеобходимо попробовать добавить домен " +
                                "вручную, после чего удалить файл, на который укажет ISPmanager 4.";
                        break;
                }
                if (!digitalOceanAnswer.isEmpty()) {
                    if (result.containsKey(domain)) {
                        result.get(domain).add("Ошибка добавления " + domain + " на Digital Ocean (возможно, токен устарел или домен уже существует)!");
                    } else {
                        List<String> messages = new ArrayList<>();
                        messages.add("Ошибка добавления " + domain + " на Digital Ocean (возможно, токен устарел или домен уже существует)!");
                        result.put(domain, messages);
                    }

                }
                if (result.containsKey(domain)) {
                    result.get(domain).add(message);
                } else {
                    List<String> messages = new ArrayList<>();
                    messages.add(message);
                    result.put(domain, messages);
                }
            }
        }
        return result;
    }
}
