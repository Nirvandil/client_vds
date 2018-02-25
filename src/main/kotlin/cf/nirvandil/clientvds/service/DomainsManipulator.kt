package cf.nirvandil.clientvds.service

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.model.DomainDescriptor
import com.jcraft.jsch.JSchException

import java.io.IOException

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
 *
 * This interface declares methods, that must contain object for
 * manipulating domains (adding, removing them) and run command on server.
 */
interface DomainsManipulator {

    val users: List<String>
        @Throws(IOException::class, JSchException::class, MainException::class)
        get

    @Throws(IOException::class, JSchException::class, MainException::class)
    fun addDomain(descriptor: DomainDescriptor): String

    @Throws(IOException::class, JSchException::class, MainException::class)
    fun removeDomain(descriptor: DomainDescriptor): String

    @Throws(IOException::class, JSchException::class, MainException::class)
    fun askUserOfPanel(users: List<String>): String

    fun reportDone()

}
