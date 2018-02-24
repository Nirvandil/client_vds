package cf.nirvandil.clientvds

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.model.Action
import cf.nirvandil.clientvds.model.ActionType
import cf.nirvandil.clientvds.model.ActionType.ADD_ACTION
import cf.nirvandil.clientvds.model.ActionType.REMOVE_ACTION
import cf.nirvandil.clientvds.model.ConnectDetails
import cf.nirvandil.clientvds.model.DomainData
import cf.nirvandil.clientvds.model.PanelType
import cf.nirvandil.clientvds.service.ManipulatorFactory
import cf.nirvandil.clientvds.service.impl.ValidatorImpl
import cf.nirvandil.clientvds.tasks.AddingTask
import cf.nirvandil.clientvds.tasks.RemovingTask
import cf.nirvandil.clientvds.util.EXEC_SESSION
import cf.nirvandil.clientvds.util.GET_PANEL_COMMAND
import cf.nirvandil.clientvds.util.LOGO_PATH
import cf.nirvandil.clientvds.util.SocketFactoryWithTimeout
import cf.nirvandil.clientvds.util.WARN
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.WARNING
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

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
 * Main logic class. Creates domains manipulator based on detected panel,
 * starts action for remove or add domains by create proper task and reports
 * status operation in the and.
 */
class Coordinator internal constructor(private val details: ConnectDetails,
                                       private val progressBar: ProgressBar) {
    private val validator = ValidatorImpl()

    private val sshClientSession: Session
        @Throws(JSchException::class, MainException::class)
        get() =
            JSch().getSession("root", details.ip, details.port).apply {
                setPassword(details.pass)
                setSocketFactory(SocketFactoryWithTimeout())
                setConfig("StrictHostKeyChecking", "no")
                connect()
            }

    @Throws(IOException::class, JSchException::class, MainException::class)
    fun runAction(action: Action) {
        val domains = validator.getValidDomains(action.domainsContent)
        val domainsManipulator = ManipulatorFactory.createManipulator(detectPanel(), sshClientSession)
        val owner = domainsManipulator.askUserOfPanel(domainsManipulator.users)
        val domainData = DomainData(domains, details.ip, owner, action.templatePath, action.phpMod, action.token)
        val task = when (action.type) {
            ADD_ACTION -> AddingTask(domainData, domainsManipulator)
            REMOVE_ACTION -> RemovingTask(domainData, domainsManipulator)
        }
        progressBar.progressProperty().bind(task.progressProperty())
        task.setOnSucceeded {
            if (!task.value.isEmpty()) {
                for ((domain, messages) in task.value) {
                    messages.filterNot { it.isBlank() }.forEach { message ->
                        val alert = Alert(WARNING)
                        val stage = alert.dialogPane.scene.window as Stage
                        stage.icons.add(Image(this.javaClass.getResourceAsStream(LOGO_PATH)))
                        alert.title = WARN
                        alert.headerText = "При обработке домена $domain возникло предупреждение!"
                        alert.contentText = message
                        alert.showAndWait()
                    }
                }
            }
            domainsManipulator.reportDone()
        }
        Thread(task).start()
    }

    @Throws(IOException::class, JSchException::class, MainException::class)
    private fun detectPanel(): PanelType {
        val channel = sshClientSession.openChannel(EXEC_SESSION) as ChannelExec
        val `in` = BufferedReader(InputStreamReader(channel.inputStream))
        channel.setCommand(GET_PANEL_COMMAND)
        channel.connect()
        val panel = `in`.readLine()
        channel.disconnect()
        return PanelType.valueOf(panel)
    }
}