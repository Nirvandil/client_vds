package cf.nirvandil.clientvds.exc

import cf.nirvandil.clientvds.util.ERROR_HEADER
import cf.nirvandil.clientvds.util.ERROR_TITLE
import cf.nirvandil.clientvds.util.LOGO_PATH
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.image.Image
import javafx.stage.Stage

/**
 * Created by Vladimir Sukharev aka Nirvandil on 04.09.2016.
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
 * Class for visualise occurs exception by showing dialog box
 */

class MainException(message: String, header: String = ERROR_HEADER) : RuntimeException(message) {

    private fun notifyError(error: String, header: String) {
        val alert = Alert(ERROR)
        alert.title = ERROR_TITLE
        alert.headerText = header
        alert.contentText = error
        val stage = alert.dialogPane.scene.window as Stage
        stage.icons.add(Image(this.javaClass.getResourceAsStream(LOGO_PATH)))
        alert.showAndWait()
    }

    init {
        notifyError(message, header)
    }
}
