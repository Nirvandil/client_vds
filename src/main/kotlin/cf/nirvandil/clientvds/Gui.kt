package cf.nirvandil.clientvds

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.model.Action
import cf.nirvandil.clientvds.model.ActionType
import cf.nirvandil.clientvds.model.ConnectDetails
import cf.nirvandil.clientvds.util.ADD
import cf.nirvandil.clientvds.util.CONNECT_ERR_MESSAGE
import cf.nirvandil.clientvds.util.DOMAINS_LIST_TOOLTIP
import cf.nirvandil.clientvds.util.DO_TOKEN_TOOLTIP
import cf.nirvandil.clientvds.util.DesktopApi
import cf.nirvandil.clientvds.util.FH_LINK_NAME
import cf.nirvandil.clientvds.util.FRIEND_URI
import cf.nirvandil.clientvds.util.HELP_IMAGE_PATH
import cf.nirvandil.clientvds.util.HELP_TOOLTIP
import cf.nirvandil.clientvds.util.IO_ERR_MESSAGE
import cf.nirvandil.clientvds.util.IP_PORT_HING
import cf.nirvandil.clientvds.util.IP_TOOLTIP
import cf.nirvandil.clientvds.util.LOCALHOST
import cf.nirvandil.clientvds.util.LOGO_PATH
import cf.nirvandil.clientvds.util.MAIN_WINDOW_NAME
import cf.nirvandil.clientvds.util.MESSAGE_EMPTY_FIELDS
import cf.nirvandil.clientvds.util.PASS_HINT
import cf.nirvandil.clientvds.util.PASS_TOOLTIP
import cf.nirvandil.clientvds.util.PATH_TEMPLATE_PROMPT
import cf.nirvandil.clientvds.util.PATH_TEMPLATE_TOOLTIP
import cf.nirvandil.clientvds.util.PHP_CGI
import cf.nirvandil.clientvds.util.PHP_CGI_TOOLTIP
import cf.nirvandil.clientvds.util.PHP_MODE
import cf.nirvandil.clientvds.util.PHP_MODE_TOOLTIP
import cf.nirvandil.clientvds.util.PORT_ERR_MESSAGE
import cf.nirvandil.clientvds.util.PORT_TOOLTIP
import cf.nirvandil.clientvds.util.PROGRESS_HINT
import cf.nirvandil.clientvds.util.REMOVE
import cf.nirvandil.clientvds.util.REMOVE_BUTTON_TOOLTIP
import cf.nirvandil.clientvds.util.CSS_STYLE_PATH
import cf.nirvandil.clientvds.util.TEMPLATE_HINT
import cf.nirvandil.clientvds.util.WIKI_HELP_URI
import com.jcraft.jsch.JSchException
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.PasswordField
import javafx.scene.control.ProgressBar
import javafx.scene.control.RadioButton
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.text.Text
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI

/**
 * Created by Vladimir Sukharev aka Nirvandil on 02.09.2016.
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
 * This is main point of GUI, that initializes all elements and sets
 * handlers for buttons. Also it's JavaFX Application start point.
 */

class Gui : Application() {
    private val log = LoggerFactory.getLogger(javaClass)
    private val root = GridPane()
    private val ipField = TextField()
    private val portField = TextField()
    private val passField = PasswordField()
    private val help = Button()
    private val pathTemplateField = TextField()
    private val tokenField = TextField()
    private val pathTemplateTooltip = Tooltip(PATH_TEMPLATE_TOOLTIP)
    private val phpToggle = ToggleGroup()
    private val phpCGI = RadioButton(PHP_CGI)
    private val phpMod = RadioButton(PHP_MODE)
    private val progressBar = ProgressBar(0.0)
    private val domainsArea = TextArea()
    private val hyperlink = Hyperlink(FH_LINK_NAME)
    private val addButton = Button(ADD)
    private val removeButton = Button(REMOVE)
    private val ipFieldTooltip = Tooltip(IP_TOOLTIP)
    private val portFieldTooltip = Tooltip(PORT_TOOLTIP)
    private val passFieldTooltip = Tooltip(PASS_TOOLTIP)
    private val domainsAreaTooltip = Tooltip(DOMAINS_LIST_TOOLTIP)
    private val phpCGITooltip = Tooltip(PHP_CGI_TOOLTIP)
    private val phpModTooltip = Tooltip(PHP_MODE_TOOLTIP)
    private val removeTooltip = Tooltip(REMOVE_BUTTON_TOOLTIP)
    private val tokenToolTip = Tooltip(DO_TOKEN_TOOLTIP)

    init {
        initIpField()
        initPortField()
        initHelp()
        initPassField()
        initPhpModToggle()
        initTemplatePathField()
        initProgressBar()
        initDomainsArea()
        initFriendHostingLink()
        initAddButton()
        initRemoveButton()
        initTokenField()
        initGridLayout()
    }

    private fun initIpField() {
        ipField.tooltip = ipFieldTooltip
        ipField.promptText = LOCALHOST
    }

    private fun initPortField() {
        portField.tooltip = portFieldTooltip
        portField.maxWidth = 60.0
        portField.promptText = "3333"
    }

    private fun initTokenField() {
        tokenField.tooltip = tokenToolTip
        tokenField.promptText = "Oauth token"
    }

    private fun initHelp() {
        help.graphic = ImageView(Image(this.javaClass.getResourceAsStream(HELP_IMAGE_PATH)))
        help.tooltip = Tooltip(HELP_TOOLTIP)
        help.setOnAction { DesktopApi.browse(URI(WIKI_HELP_URI)) }
    }

    private fun initPassField() {
        passField.tooltip = passFieldTooltip
    }

    private fun initPhpModToggle() {
        phpCGI.tooltip = phpCGITooltip
        phpMod.tooltip = phpModTooltip
        phpCGI.toggleGroup = phpToggle
        phpMod.toggleGroup = phpToggle
        phpCGI.isSelected = true
    }

    private fun initTemplatePathField() {
        pathTemplateField.tooltip = pathTemplateTooltip
        pathTemplateField.promptText = PATH_TEMPLATE_PROMPT
    }

    private fun initProgressBar() {
        progressBar.minWidth = 180.0
    }

    private fun initDomainsArea() {
        domainsArea.tooltip = domainsAreaTooltip
        domainsArea.promptText = "example.ru"
    }

    private fun initFriendHostingLink() {
        hyperlink.setOnAction { DesktopApi.browse(URI(FRIEND_URI)) }
        hyperlink.tooltip = Tooltip(FRIEND_URI)
    }

    private fun initAddButton() {
        addButton.onAction = createButtonHandler()
    }

    private fun initRemoveButton() {
        removeButton.tooltip = removeTooltip
        removeButton.onAction = createButtonHandler()
    }

    private fun initGridLayout() {
        with(root) {
            hgap = 8.0
            vgap = 8.0
            padding = Insets(5.0, 5.0, 5.0, 5.0)
            styleClass.add("pane")
            add(Text(IP_PORT_HING), 0, 0)
            add(ipField, 1, 0)
            add(portField, 2, 0)
            add(Text(PASS_HINT), 0, 1)
            add(passField, 1, 1)
            add(phpCGI, 2, 1)
            add(phpMod, 3, 1)
            add(Text(PROGRESS_HINT), 0, 2)
            add(progressBar, 1, 2)
            add(Text(TEMPLATE_HINT), 0, 3)
            add(pathTemplateField, 1, 3, 2, 1)
            add(tokenField, 1, 4, 2, 1)
            add(domainsArea, 0, 5, 3, 1)
            add(help, 3, 0)
            add(hyperlink, 0, 6, 2, 1)
            add(removeButton, 2, 6)
            add(addButton, 3, 6)
        }
    }

    private fun createButtonHandler(): EventHandler<ActionEvent> {
        return EventHandler {
            if (arrayOf(ipField.text, passField.text, portField.text).any { it.isBlank() })
                throw MainException(MESSAGE_EMPTY_FIELDS)
            try {
                val coordinator = Coordinator(ConnectDetails(ipField.text, portField.text.toInt(), passField.text), progressBar)
                var templatePath = pathTemplateField.text
                if (!templatePath.endsWith("/")) templatePath += "/"
                val actionType = ActionType.fromString((it.source as Button).text)!!
                val action = Action(actionType,
                        domainsArea.text, (phpToggle.selectedToggle as RadioButton).text, templatePath, tokenField.text)
                coordinator.runAction(action)
            } catch (ioe: IOException) {
                throw MainException(IO_ERR_MESSAGE)
            } catch (jSchException: JSchException) {
                throw MainException(CONNECT_ERR_MESSAGE)
            } catch (nfe: NumberFormatException) {
                throw MainException(PORT_ERR_MESSAGE)
            } catch (me: MainException) {
                log.error("{}", me)
            }
        }
    }

    override fun start(primaryStage: Stage) {
        val scene = Scene(root)
        // Set "resizable" by binding width of window to domainsArea
        scene.widthProperty().addListener { _, _, newValue -> domainsArea.minWidth = newValue.toDouble() - 220 }
        scene.heightProperty().addListener { _, _, newValue -> domainsArea.minHeight = newValue.toDouble() - 220 }
        scene.stylesheets.add(CSS_STYLE_PATH)
        primaryStage.title = MAIN_WINDOW_NAME
        primaryStage.scene = scene
        primaryStage.icons.add(Image(this.javaClass.getResourceAsStream(LOGO_PATH)))
        primaryStage.setOnCloseRequest { System.exit(0) }
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(Gui::class.java, *args)
}