package cf.nirvandil.clientvds.tasks

import cf.nirvandil.clientvds.model.DomainData
import cf.nirvandil.clientvds.service.DomainsManipulator
import cf.nirvandil.clientvds.service.impl.DigitalOceanClientImpl
import javafx.concurrent.Task
import lombok.SneakyThrows

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
 *
 *
 * This is task for adding domains to server.
 */
open class AddingTask(protected val data: DomainData,
                      protected val domainsManipulator: DomainsManipulator) : Task<Map<String, List<String>>>() {
    private val digitalOceanClient = DigitalOceanClientImpl()

    @SneakyThrows
    override fun call(): Map<String, List<String>> {
        val fullWork = data.domains.size.toLong()
        var done = 0L
        domainsManipulator.users
        //Into map we will put any errors that occurs while adding domain
        val result = HashMap<String, MutableList<String>>()
        data.domains.forEach { domain ->
            val returnCode = domainsManipulator.addDomain(domain, data.ip, data.owner, data.phpMode, data.templatePath)
            val digitalOceanAnswer = if (data.token.isNotBlank()) digitalOceanClient.addDomain(domain, data.ip, data.token) else ""
            updateProgress(++done, fullWork)
            Thread.sleep(100)
            if (returnCode.isNotEmpty()) {
                val message = when (returnCode) {
                    "ERROR 2" -> "Похоже, что домен $domain уже существует в \nпанели управления"
                    "exist" -> "Похоже, что домен $domain уже существует в \nпанели управления"
                    "ERROR limit" -> "Превышено ограничение на число доменов для указанного пользователя"
                    "ERROR 8" -> "Указанному пользователю запрещено использовать РНР в этом режиме"
                    "ERROR 9" -> "Похоже, что домен " + domain + " уже существует на FriendDNS!\n" +
                            "(или иная ошибка внеших DNS)"
                    "ERROR parsing" -> "Ошибка при синтаксическом разборе файла .passwd," +
                            "и автоматическое исправление не удалось.\nНеобходимо попробовать добавить домен " +
                            "вручную, после чего удалить файл, на который укажет ISPmanager 4."
                    else -> ""
                }
                if (digitalOceanAnswer.isNotEmpty()) {
                    val oceanMessage = "Ошибка добавления $domain на Digital Ocean (возможно, токен устарел или домен уже существует)"
                    putCarefully(result, oceanMessage, domain)
                }
                putCarefully(result, message, domain)
            }
        }
        return result
    }

    internal fun putCarefully(target: MutableMap<String, MutableList<String>>, message: String, domain: String) {
        when {
            target.containsKey(domain) -> target[domain]?.add(message)
            else -> target[domain] = arrayListOf(message)
        }
    }
}
