package cf.nirvandil.clientvds.service.impl

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.service.DomainsManipulator
import cf.nirvandil.clientvds.util.ASK_USER_CONTENT
import cf.nirvandil.clientvds.util.ASK_USER_EMPTY_MESSAGE
import cf.nirvandil.clientvds.util.ASK_USER_HEADER
import cf.nirvandil.clientvds.util.EXEC_SESSION
import cf.nirvandil.clientvds.util.LOGO_PATH
import cf.nirvandil.clientvds.util.OWNER_TITLE
import cf.nirvandil.clientvds.util.SUCCESS_CONTENT
import cf.nirvandil.clientvds.util.SUCCESS_HEADER
import cf.nirvandil.clientvds.util.SUCCESS_TITLE
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.ChoiceDialog
import javafx.scene.image.Image
import javafx.stage.Stage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.stream.Collectors.toList

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
 * This is abstract class, that implements some common futures for all
 * domains manipulators
 */
abstract class AbstractDomainsManipulator(private val session: Session) : DomainsManipulator {
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    private val channelExec: ChannelExec
        @Throws(JSchException::class)
        get() = session.openChannel(EXEC_SESSION) as ChannelExec

    @Throws(IOException::class, JSchException::class)
    fun getCommandOutput(command: String): List<String> {
        with(channelExec) {
            val inputReader = BufferedReader(InputStreamReader(inputStream))
            val errorReader = BufferedReader(InputStreamReader(errStream))
            setCommand(command)
            connect()
            return arrayListOf<String>().apply {
                addAll(getFromReader(inputReader))
                addAll(getFromReader(errorReader))
                disconnect()
            }
        }
    }

    fun createCpTemplateCommand(own: String, domain: String, templatePath: String): String {
        val destPath = constructDomainPath(own, domain)
        return " && shopt -s dotglob && \\cp -r $templatePath* $destPath && chown -R  $own:$own $destPath"
    }

    private fun getFromReader(reader: BufferedReader): List<String> = reader.lines().collect(toList())

    override fun reportDone() {
        val alert = Alert(INFORMATION)
        alert.title = SUCCESS_TITLE
        alert.contentText = SUCCESS_CONTENT
        alert.headerText = SUCCESS_HEADER
        val stage = alert.dialogPane.scene.window as Stage
        stage.icons.add(Image(this.javaClass.getResourceAsStream(LOGO_PATH)))
        alert.showAndWait()
    }

    @Throws(MainException::class)
    override fun askUserOfPanel(users: List<String>): String {
        val dialog = ChoiceDialog(users[0], users)
        dialog.title = OWNER_TITLE
        dialog.headerText = ASK_USER_HEADER
        dialog.contentText = ASK_USER_CONTENT
        val stage = dialog.dialogPane.scene.window as Stage
        stage.icons.add(Image(javaClass.getResourceAsStream(LOGO_PATH)))
        val result = dialog.showAndWait()
        return result.orElseThrow { MainException(ASK_USER_EMPTY_MESSAGE) }
    }

    @Throws(IOException::class, JSchException::class)
    fun checkPathExist(path: String): Boolean {
        return getCommandOutput("test -e $path && echo 0")[0].toInt() == 0
    }

    abstract fun constructDomainPath(owner: String, domainName: String): String
}
