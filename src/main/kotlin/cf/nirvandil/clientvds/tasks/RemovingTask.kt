package cf.nirvandil.clientvds.tasks

import cf.nirvandil.clientvds.model.DomainData
import cf.nirvandil.clientvds.service.DomainsManipulator
import cf.nirvandil.clientvds.service.impl.DigitalOceanClientImpl
import lombok.SneakyThrows

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
 *
 *
 * Task for removing domains
 */
class RemovingTask(data: DomainData,
                   domainsManipulator: DomainsManipulator) : AddingTask(data, domainsManipulator) {
    private val digitalOceanClient = DigitalOceanClientImpl()

    @SneakyThrows
    override fun call(): Map<String, List<String>> {
        val fullWork = data.domains.size.toLong()
        var done = 0L
        domainsManipulator.users
        //Into map we will put any errors that occurs while adding domain
        val result = HashMap<String, MutableList<String>>()
        data.domains.forEach { domain ->
            val returnCode = domainsManipulator.removeDomain(domain, data.owner)
            var digitalAnswer = ""
            if (data.token.isNotBlank()) {
                digitalAnswer = digitalOceanClient.removeDomain(domain, data.token)
            }
            updateProgress(++done, fullWork)
            Thread.sleep(100)
            if (returnCode.isNotEmpty()) {
                var message = ""
                if (returnCode.contains("doesn't exist")) {
                    when {
                        returnCode.contains("domain") -> message = "Похоже, что домен $domain не существует в \nпанели управления"
                        returnCode.contains("user") -> message = "Похоже, что пользователь ${data.owner} не существует в \nпанели управления"
                    }
                }
                result[domain] = arrayListOf(message)
            }
            if (digitalAnswer.isNotEmpty()) {
                val oceanMessage = "Невозможно удалить " + domain + " с Digital Ocean, возможно,\n " +
                        "домена не существует или некорректный ключ API."
                super.putCarefully(result, oceanMessage, domain)
            }
        }
        return result
    }
}
